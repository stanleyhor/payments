package com.ecomm.payments.rest;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.service.PaymentAuthorizationService;
import com.ecomm.payments.service.PaymentDetailsService;
import com.ecomm.payments.util.CommonUtils;
import com.google.gson.Gson;
import com.newrelic.api.agent.Trace;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments/v1")
@Slf4j
@RequiredArgsConstructor
public class PaymentAuthController {

    private final PaymentsConfig paymentsConfig;

    @Qualifier("mockPaymentAuthorizationServiceImpl") private final PaymentAuthorizationService mockPaymentAuthorizationServiceImpl;

    @Qualifier("paymentAuthorizationServiceImpl") private final PaymentAuthorizationService paymentAuthorizationServiceImpl;

    @Qualifier("paymentDetailsServiceImpl") private final PaymentDetailsService paymentDetailsServiceImpl;

    @Qualifier("mockPaymentDetailsServiceImpl") private final PaymentDetailsService mockPaymentDetailsServiceImpl;

    @Trace(dispatcher = true,
            metricName = "payments/v1/auth#POST")
    @PostMapping(path = "/auth",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> authorizePayment(
            @Valid @RequestBody ATGAuthRequest authRequest, @RequestHeader(value = "aeSite",
                    required = false) String siteId
    ) {

        authRequest.setSiteId(siteId);
        ATGAuthResponse authResponse;
        if (paymentsConfig.isUseMock()
                && mockPaymentAuthorizationServiceImpl != null) {
            log.info("Calling mock service");
            try {
                Thread.sleep(paymentsConfig.getSleepTimeoutMillis());
                authResponse = mockPaymentAuthorizationServiceImpl.authorizePayment(authRequest);
            } catch (InterruptedException e) {
                log.error("Thread interrupted :: returning null", e);
                authResponse = null;
                Thread.currentThread()
                        .interrupt();
            }
        } else if (!PaymentsConstants.AEO_MX.equalsIgnoreCase(siteId)
                && Objects.nonNull(authRequest.getBillingAddress())
                && paymentsConfig.getEmailsToBypassAuthCall()
                        .contains(authRequest.getBillingAddress()
                                .getEmail())
                && mockPaymentAuthorizationServiceImpl != null) {
            log.info("PaymentAuthController.authorizePayment() :: Calling mock service and Bypassing authorization call for orderNumber {}, email {} ",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_EMAIL, authRequest.getBillingAddress()
                            .getEmail()));
            authResponse = mockPaymentAuthorizationServiceImpl.authorizePayment(authRequest);
        } else {

            log.debug("PaymentAuthController.authorizePayment() :: orderNumber: {}, siteId: {} and authorizationRequest: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                    v(PaymentsConstants.LOG_KEY_AUTHORIZATION_REQUEST, new Gson().toJson(authRequest)),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

            log.info("PaymentAuthController.authorizePayment() :: orderNumber: {}, siteId: {}, paymentGroupId: {} and paymentMethod: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

            authResponse = paymentAuthorizationServiceImpl.authorizePayment(authRequest);

            log.info("PaymentAuthController.authorizePayment() :: orderNumber: {}, siteId: {}, resultCode: {} and authorizationResponse: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                    v(PaymentsConstants.LOG_KEY_RESULT_CODE, authResponse.getResultCode()),
                    v(PaymentsConstants.LOG_KEY_AUTHORIZATION_RESPONSE, new Gson().toJson(authResponse)),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));
        }
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @Trace(dispatcher = true,
            metricName = "payments/v1/auth/applePay#POST")
    @PostMapping(path = "/auth/applePay",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> authorizeApplePay(
            @Valid @RequestBody ATGAuthRequest authRequest, @RequestHeader(value = "aeSite",
                    required = false) String siteId
    ) {

        if (null == authRequest) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        authRequest.setSiteId(siteId);

        log.debug(
                "PaymentAuthController.authorizeApplePay() :: applePay payment authorize request with orderNumber: {}, siteId: {} and authorizationRequest: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_REQUEST, new Gson().toJson(authRequest)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

        log.info(
                "PaymentAuthController.authorizeApplePay() :: applePay payment authorize request with orderNumber: {}, siteId: {}, paymentGroupId: {} and paymentMethod: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

        ATGAuthResponse authResponse = paymentAuthorizationServiceImpl.authorizePayment(authRequest);

        log.info(
                "PaymentAuthController.authorizeApplePay() :: applePay payment authorize response with orderNumber {}, siteId {}, resultCode {} and authorizationResponse {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_RESULT_CODE, authResponse.getResultCode()),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_RESPONSE, new Gson().toJson(authResponse)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @Trace(dispatcher = true,
            metricName = "payments/v1/auth/paypal#POST")
    @PostMapping(path = "/auth/paypal",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> authorizePaypal(
            @Valid @RequestBody ATGAuthRequest paypalAuthRequest, @RequestHeader(value = "aeSite",
                    required = false) String siteId
    ) {

        if (null == paypalAuthRequest) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        paypalAuthRequest.setSiteId(siteId);
        log.debug("PaymentAuthController.authorizePaypal() :: paypal payment authorize request with orderNumber {}, siteId {} and authorizationRequest: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, paypalAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_REQUEST, new Gson().toJson(paypalAuthRequest)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, paypalAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(paypalAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(paypalAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(paypalAuthRequest.getCheckoutType())));

        log.info(
                "PaymentAuthController.authorizePaypal() :: paypal payment authorize request with orderNumber {}, siteId {}, paymentGroupId {} and paymentMethod {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, paypalAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, paypalAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(paypalAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(paypalAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(paypalAuthRequest.getCheckoutType())));

        ATGAuthResponse paypalAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(paypalAuthRequest);

        log.info(
                "PaymentAuthController.authorizePaypal() :: paypal payment authorize response with orderNumber {}, siteId {}, resultCode {} and authorizationResponse {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, paypalAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_RESULT_CODE, paypalAuthResponse.getResultCode()),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_RESPONSE, new Gson().toJson(paypalAuthResponse)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, paypalAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(paypalAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(paypalAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(paypalAuthRequest.getCheckoutType())));

        return new ResponseEntity<>(paypalAuthResponse, HttpStatus.OK);
    }

    /**
     * This method will be used to make the details api call to adyen service.
     * 
     * @param detailsRequest - ATGDetailsRequest Object
     * @param headers        - HttpHeaders Object
     * @return paymentDetails - ATGDetailsResponse Object
     */
    @Trace(dispatcher = true,
            metricName = "payments/v1/details#POST")
    @PostMapping(path = "/details",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<PaymentDetailsResponse> paymentDetails(@Valid @RequestBody PaymentDetailsRequest detailsRequest) {
        PaymentDetailsResponse detailsResponse;
        if (paymentsConfig.isUseMock()
                && mockPaymentDetailsServiceImpl != null) {
            log.info("Calling mock service");
            try {
                Thread.sleep(paymentsConfig.getSleepTimeoutMillis());
                detailsResponse = mockPaymentDetailsServiceImpl.retrieveDetailsResponse(detailsRequest);
            } catch (InterruptedException e) {
                log.error("Thread interrupted :: returning null", e);
                detailsResponse = null;
                Thread.currentThread()
                        .interrupt();
            }
        } else {

            log.info("PaymentAuthController.paymentDetails() :: details request with orderNumber: {} and cartId: {}",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getDetailsRequestChannelType(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getDetailsRequestCheckoutType(detailsRequest)));

            log.debug("PaymentAuthController.paymentDetails() :: orderNumber: {}, cartId: {} and detailsRequest: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()),
                    v(PaymentsConstants.LOG_KEY_DETAIL_REQUEST, new Gson().toJson(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getDetailsRequestChannelType(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getDetailsRequestCheckoutType(detailsRequest)));

            detailsResponse = paymentDetailsServiceImpl.retrieveDetailsResponse(detailsRequest);

            log.debug("PaymentAuthController.paymentDetails() :: orderNumber: {}, cartId: {} and detailsResponse: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()),
                    v(PaymentsConstants.LOG_KEY_DETAIL_RESPONSE, new Gson().toJson(detailsResponse)),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getDetailsRequestChannelType(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getDetailsRequestCheckoutType(detailsRequest)));
        }
        return new ResponseEntity<>(detailsResponse, HttpStatus.OK);
    }

}
