package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.service.AdyenCallService;
import com.ecomm.payments.service.PaymentAuthorizationService;
import com.ecomm.payments.util.AuthRequestResponseMapper;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.CommonUtils;
import com.ecomm.payments.util.TransactionalDataMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentAuthorizationServiceImpl implements PaymentAuthorizationService {

    private final AuthRequestResponseMapper requestResponseMapper;

    private final TransactionalDataMapper transactionalDataMapper;

    @Qualifier("adyenCallServiceImpl") private final AdyenCallService adyenCallServiceImpl;

    private final AuthorizationUtils authorizationUtils;

    public ATGAuthResponse authorizePayment(ATGAuthRequest authRequest) {

        log.debug("ATGAuthRequest {} : {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                new Gson().toJson(authRequest, ATGAuthRequest.class));

        ATGAuthResponse authResponse = null;

        if (!authorizationUtils.isAuthRetry(authRequest)) {

            authResponse = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);

        }

        return Objects.isNull(authResponse) ? invokeAdyenCallService(authRequest) : authResponse;

    }

    private ATGAuthResponse invokeAdyenCallService(ATGAuthRequest authRequest) {

        AdyenAuthRequest convertedRequest = requestResponseMapper.convertToAdyenAuthRequest(authRequest);

        String idempotencyKey = authRequest.getIdempotencyKey();

        log.info("PaymentAuthorizationServiceImpl.invokeAdyenCallService() :: AdyenAuthRequest {} : {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), new Gson().toJson(convertedRequest, AdyenAuthRequest.class),
                v(PaymentsConstants.LOG_KEY_IDEMPOTENCY_KEY, idempotencyKey),
                v(PaymentsConstants.LOG_KEY_MERCHANT_ACCOUNT, convertedRequest.getMerchantAccount()),
                v(PaymentsConstants.LOG_KEY_REFERENCE, convertedRequest.getReference()), v(PaymentsConstants.LOG_KEY_WEBSTORE_ID, authRequest.getWebStoreId()),
                v(PaymentsConstants.LOG_KEY_CHANNEL, convertedRequest.getChannel()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()));

        AdyenAuthResponse adyenAuthResponse = adyenCallServiceImpl.callService(convertedRequest, idempotencyKey);

        log.info("PaymentAuthorizationServiceImpl.invokeAdyenCallService() :: AdyenAuthResponse {} : {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), new Gson().toJson(adyenAuthResponse, AdyenAuthResponse.class),
                v(PaymentsConstants.LOG_KEY_MERCHANT_ACCOUNT, convertedRequest.getMerchantAccount()),
                v(PaymentsConstants.LOG_KEY_WEBSTORE_ID, authRequest.getWebStoreId()), v(PaymentsConstants.LOG_KEY_CHANNEL, convertedRequest.getChannel()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()),
                v(PaymentsConstants.LOG_KEY_MERCHANT_REFERENCE, CommonUtils.getMerchantReference(convertedRequest, adyenAuthResponse)),
                v(PaymentsConstants.LOG_KEY_RESULT_CODE, CommonUtils.getResultCode(adyenAuthResponse)),
                v(PaymentsConstants.LOG_KEY_FRAUD_RESULT_TYPE, CommonUtils.getFraudResultType(adyenAuthResponse)));

        return authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, convertedRequest, adyenAuthResponse);
    }

    public ATGAuthResponse authorizeAplazoPayment(ATGAuthRequest authRequest) {

        log.debug("AplazoPaymentRequest {} : {}", new Gson().toJson(authRequest, ATGAuthRequest.class),
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()));

        var paymentHeader = transactionalDataMapper.getPaymentHeader(authRequest.getPaymentGroupId());
        var paymentEvent = authorizationUtils.findByPaymentReference(paymentHeader, authRequest);

        if (!authorizationUtils.isAuthRetry(authRequest)
                && paymentEvent.isPresent()) {
            log.info("PaymentAuthorizationServiceImpl.authorizeAplazoPayment() :: payment Header available for : {}",
                    new Gson().toJson(authRequest, ATGAuthRequest.class));
            return requestResponseMapper.buildAuthResponseFromHeader(paymentHeader, paymentEvent.get());
        } else {
            log.warn("PaymentAuthorizationServiceImpl.authorizeAplazoPayment() :: payment Header not available for : {}",
                    new Gson().toJson(authRequest, ATGAuthRequest.class));
            return requestResponseMapper.buildAplazoDeclinedResponse();
        }
    }

}