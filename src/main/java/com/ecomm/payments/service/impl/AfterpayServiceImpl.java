package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.afterpay.AfterpayAuthRequest;
import com.ecomm.payments.model.afterpay.AfterpayAuthResponse;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.service.AfterpayCallService;
import com.ecomm.payments.service.AfterpayService;
import com.ecomm.payments.util.AfterpayRepoWrapper;
import com.ecomm.payments.util.AfterpayRequestResponseMapper;
import com.ecomm.payments.util.CommonUtils;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AfterpayServiceImpl implements AfterpayService {

    private final AfterpayCallService afterpayCallService;

    private final AfterpayRequestResponseMapper afterpayRequestResponseMapper;

    private final AfterpayRepoWrapper repoWrapper;

    @Override
    public AfterpayCheckoutResponse fetchToken(ATGAuthRequest authRequest) {

        log.debug("AfterpayServiceImpl.fetchToken() :: orderNumber: {}, siteId: {}, paymentMethod: {}, tokenRequest: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                new Gson().toJson(authRequest));

        var checkoutRequest = afterpayRequestResponseMapper.convertToCheckoutRequest(authRequest);

        log.info("AfterpayServiceImpl.fetchToken() :: orderNumber: {}, siteId: {}, paymentMethod: {}, createCheckoutRequest: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                new Gson().toJson(checkoutRequest));

        var responseEntity = afterpayCallService.createCheckout(checkoutRequest, authRequest.getSiteId());

        log.info("AfterpayServiceImpl.fetchToken() :: orderNumber: {}, siteId: {}, paymentMethod: {}, createCheckoutResponse: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                new Gson().toJson(responseEntity.getBody()));

        var response = afterpayRequestResponseMapper.buildCheckoutResponse(responseEntity.getBody(), authRequest.getOrderNumber());

        if (StringUtils.hasText(response.getToken())) {
            repoWrapper.saveCreateCheckoutDetails(authRequest, response);
            response.setResultCode(PaymentsConstants.REDIRECT_AFTERPAY);
        }

        log.debug("AfterpayServiceImpl.fetchToken() :: orderNumber: {}, siteId: {}, paymentMethod: {}, tokenResponse: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                new Gson().toJson(response));

        return response;
    }

    @Override
    public AfterpayAuthResponse authorizePayment(PaymentDetailsRequest detailsRequest, String siteId) {

        var authorizeResponse = checkExistingPaymentHeaderAvailable(detailsRequest, siteId);

        if (null == authorizeResponse) {

            var authorizeAfterpayRequest = afterpayRequestResponseMapper.convertToAuthorizeRequest(detailsRequest);

            log.info("AfterpayServiceImpl.authorizePayment() :: orderNumber: {}, siteId: {} with authorizeRequest: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                    new Gson().toJson(authorizeAfterpayRequest));

            var responseEntity = afterpayCallService.authorize(authorizeAfterpayRequest, siteId);

            if (responseEntity.getStatusCode()
                    .is5xxServerError()) {
                voidPayment(authorizeAfterpayRequest, siteId);
                throw new ClientException(ClientErrorResponse.defaultErrorResponse);
            }

            log.debug("AfterpayServiceImpl.authorizePayment() :: orderNumber: {}, siteId: {}, authorizeResponse: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                    new Gson().toJson(responseEntity.getBody()));

            authorizeResponse = new Gson().fromJson(responseEntity.getBody(), AfterpayAuthResponse.class);

            PaymentHeaderDTO paymentHeaderDTO = repoWrapper.updateAuthorizeDetails(detailsRequest, authorizeResponse);

            log.info("AfterpayServiceImpl.authorizePayment() :: orderNumber: {}, paymentMethod: {}, siteId: {}, authorizeResponse: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getSourceType(paymentHeaderDTO)), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                    new Gson().toJson(authorizeResponse));

        }

        return authorizeResponse;

    }

    private AfterpayAuthResponse checkExistingPaymentHeaderAvailable(PaymentDetailsRequest detailsRequest, String siteId) {

        var optPaymentHeader = repoWrapper.getPaymentHeaderByOrderNumber(detailsRequest.getOrderNumber());
        AfterpayAuthResponse afterpayAuthResponse = null;

        if (optPaymentHeader.isEmpty()) {
            log.error("AfterpayServiceImpl.checkExistingPaymentHeaderAvailable() :: "
                    + "No existing PaymentHeaderDTO record available for orderNumber: {}, siteId: {} with request: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                    new Gson().toJson(detailsRequest));

            afterpayAuthResponse = new AfterpayAuthResponse();
            afterpayAuthResponse.setHttpStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
            afterpayAuthResponse
                    .setMessage(new StringBuilder(PaymentsConstants.ERROR_PAYMENT_HEADER_DTO_NOT_AVAILABLE_MESSAGE).append(PaymentsConstants.SINGLE_SPACE)
                            .append(detailsRequest.getOrderNumber())
                            .toString());
            afterpayAuthResponse.setErrorCode(PaymentsConstants.INVALID_ORDER_NUMBER);
        }

        return afterpayAuthResponse;

    }

    private void voidPayment(AfterpayAuthRequest request, String siteId) {

        log.info("AfterpayServiceImpl.voidPayment() :: invoking reverse auth for orderNumber: {}, siteId: {}",
                v(CommonKeys.ORDER_NUMBER.key(), request.getMerchantReference()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        afterpayCallService.reverseAuth(request, siteId);

        repoWrapper.updateVoidDetails(request.getMerchantReference());

        log.info("AfterpayServiceImpl.voidPayment() :: update Void Payment Details for orderNumber: {}, siteId: {}",
                v(CommonKeys.ORDER_NUMBER.key(), request.getMerchantReference()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));
    }

}
