package com.ecomm.payments.service.impl.v2;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.service.PaymentAuthorizationService;
import com.ecomm.payments.service.v2.AdyenV2CallService;
import com.ecomm.payments.util.AuthRequestResponseMapper;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.CommonUtils;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentAuthorizationV2ServiceImpl implements PaymentAuthorizationService {

    private final AuthRequestResponseMapper requestResponseMapper;

    private final AdyenV2CallService adyenV2CallService;

    private final AuthorizationUtils authorizationUtils;

    @Override
    public ATGAuthResponse authorizePayment(ATGAuthRequest authRequest) {

        log.debug("ATGAuthRequest {} : {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                new Gson().toJson(authRequest, ATGAuthRequest.class));

        ATGAuthResponse authResponse = null;

        if (!authorizationUtils.isAuthRetry(authRequest)) {

            authResponse = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);
        }

        return Objects.isNull(authResponse) ? invokeAdyenV2Service(authRequest) : authResponse;
    }

    private ATGAuthResponse invokeAdyenV2Service(ATGAuthRequest authRequest) {

        ATGAuthResponse authorizationResponse = null;

        var adyenAuthRequest = requestResponseMapper.convertToAdyenAuthRequest(authRequest);

        var paymentType = null != authRequest.getPaymentMethod() ? authRequest.getPaymentMethod()
                .getType() : null;

        if (null != adyenAuthRequest) {
            String idempotencyKey = authRequest.getIdempotencyKey();
            log.info("PaymentAuthorizationV2ServiceImpl.invokeAdyenV2Service() :: AdyenAuthRequest {} : {}",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), new Gson().toJson(adyenAuthRequest, AdyenAuthRequest.class),
                    v(PaymentsConstants.LOG_KEY_IDEMPOTENCY_KEY, idempotencyKey),
                    v(PaymentsConstants.LOG_KEY_MERCHANT_ACCOUNT, adyenAuthRequest.getMerchantAccount()),
                    v(PaymentsConstants.LOG_KEY_REFERENCE, adyenAuthRequest.getReference()),
                    v(PaymentsConstants.LOG_KEY_WEBSTORE_ID, authRequest.getWebStoreId()), v(PaymentsConstants.LOG_KEY_CHANNEL, adyenAuthRequest.getChannel()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, paymentType), v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()));

            var adyenAuthResponseEntity = adyenV2CallService.authorize(adyenAuthRequest, idempotencyKey);

            if (null != adyenAuthResponseEntity
                    && null != adyenAuthResponseEntity.getBody()) {
                var adyenAuthResponse = adyenAuthResponseEntity.getBody();
                log.info("PaymentAuthorizationV2ServiceImpl.invokeAdyenV2Service() :: AdyenAuthResponse {} : {}",
                        v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), new Gson().toJson(adyenAuthResponse, AdyenAuthResponse.class),
                        v(PaymentsConstants.LOG_KEY_MERCHANT_ACCOUNT, adyenAuthRequest.getMerchantAccount()),
                        v(PaymentsConstants.LOG_KEY_WEBSTORE_ID, authRequest.getWebStoreId()),
                        v(PaymentsConstants.LOG_KEY_CHANNEL, adyenAuthRequest.getChannel()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, paymentType),
                        v(PaymentsConstants.LOG_KEY_SITE_ID, authRequest.getSiteId()),
                        v(PaymentsConstants.LOG_KEY_MERCHANT_REFERENCE, CommonUtils.getMerchantReference(adyenAuthRequest, adyenAuthResponse)),
                        v(PaymentsConstants.LOG_KEY_RESULT_CODE, CommonUtils.getResultCode(adyenAuthResponse)),
                        v(PaymentsConstants.LOG_KEY_FRAUD_RESULT_TYPE, CommonUtils.getFraudResultType(adyenAuthResponse)));

                authorizationResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
            }
        }
        return authorizationResponse;
    }

    @Override
    public ATGAuthResponse authorizeAplazoPayment(ATGAuthRequest authRequest) {
        log.error("PaymentAuthorizationV2ServiceImpl.authorizeAplazoPayment() :: invalid endpoint for aplazo request: {}", authRequest);
        return null;
    }

}
