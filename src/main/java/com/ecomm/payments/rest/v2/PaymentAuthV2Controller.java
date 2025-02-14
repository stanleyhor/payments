package com.ecomm.payments.rest.v2;

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
@RequestMapping("/payments/v2")
@Slf4j
@RequiredArgsConstructor
public class PaymentAuthV2Controller {

    @Qualifier("mockPaymentAuthorizationServiceImpl") private final PaymentAuthorizationService mockPaymentAuthorizationServiceImpl;

    @Qualifier("paymentAuthorizationV2ServiceImpl") private final PaymentAuthorizationService paymentAuthorizationV2ServiceImpl;

    @Qualifier("mockPaymentDetailsServiceImpl") private final PaymentDetailsService mockPaymentDetailsServiceImpl;

    @Qualifier("paymentDetailsV2ServiceImpl") private final PaymentDetailsService paymentDetailsV2ServiceImpl;

    private final PaymentsConfig config;

    /**
     * This method makes a V2 authorization call to adyen service
     * 
     * @param authRequest
     * @param siteId
     * @return authResponseEntity
     */
    @Trace(dispatcher = true,
            metricName = "payments/v2/auth#POST")
    @PostMapping(path = "/auth",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> authorize(@Valid @RequestBody ATGAuthRequest authRequest, @RequestHeader(required = false) String aeSite) {

        if (authRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        authRequest.setSiteId(aeSite);
        ATGAuthResponse authResponse;

        if (config.isUseMock()
                && mockPaymentAuthorizationServiceImpl != null) {
            log.info("PaymentAuthV2Controller.authorize() :: Calling mock service");
            try {
                Thread.sleep(config.getSleepTimeoutMillis());
                authResponse = mockPaymentAuthorizationServiceImpl.authorizePayment(authRequest);
            } catch (InterruptedException e) {
                log.error("PaymentAuthV2Controller.authorize() :: Thread interrupted :: returning null", e);
                authResponse = null;
                Thread.currentThread()
                        .interrupt();
            }
        } else if (!PaymentsConstants.AEO_MX.equalsIgnoreCase(aeSite)
                && Objects.nonNull(authRequest.getBillingAddress())
                && config.getEmailsToBypassAuthCall()
                        .contains(authRequest.getBillingAddress()
                                .getEmail())
                && mockPaymentAuthorizationServiceImpl != null) {
            log.info("PaymentAuthV2Controller.authorize() :: Calling mock service and Bypassing authorization call for orderNumber: {}, email: {} ",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_EMAIL, authRequest.getBillingAddress()
                            .getEmail()));
            authResponse = mockPaymentAuthorizationServiceImpl.authorizePayment(authRequest);
        } else {

            log.debug("PaymentAuthV2Controller.authorize() :: orderNumber: {}, siteId: {} and authorizationRequest: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                    v(PaymentsConstants.LOG_KEY_AUTHORIZATION_REQUEST, new Gson().toJson(authRequest)),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

            log.info("PaymentAuthV2Controller.authorize() :: orderNumber: {}, siteId: {}, paymentGroupId: {} and paymentMethod: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));

            authResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(authRequest);

            log.info("PaymentAuthV2Controller.authorize() :: orderNumber: {}, siteId: {}, resultCode: {} and authorizationResponse: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, authRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                    v(PaymentsConstants.LOG_KEY_RESULT_CODE, authResponse.getResultCode()),
                    v(PaymentsConstants.LOG_KEY_AUTHORIZATION_RESPONSE, new Gson().toJson(authResponse)),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, authRequest.getPaymentGroupId()),
                    v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(authRequest.getPaymentMethod())),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(authRequest.getChannel())),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(authRequest.getCheckoutType())));
        }
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    /**
     * This method makes a apple pay V2 authorization call to adyen service
     * 
     * @param applePayAuthRequest
     * @param siteId
     * @return authResponseEntity
     */
    @Trace(dispatcher = true,
            metricName = "payments/v2/auth/applePay#POST")
    @PostMapping(path = "/auth/applePay",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> authorizeApplePay(
            @Valid @RequestBody ATGAuthRequest applePayAuthRequest, @RequestHeader(required = false) String aeSite
    ) {

        if (null == applePayAuthRequest) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        applePayAuthRequest.setSiteId(aeSite);

        log.debug(
                "PaymentAuthV2Controller.authorizeApplePay() :: applePay payment authorize request with orderNumber: {}, siteId: {} and authorizationRequest: {} ",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, applePayAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_REQUEST, new Gson().toJson(applePayAuthRequest)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, applePayAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(applePayAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(applePayAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(applePayAuthRequest.getCheckoutType())));

        log.info(
                "PaymentAuthV2Controller.authorizeApplePay() :: applePay payment authorize request with orderNumber: {}, siteId: {}, paymentGroupId: {} and paymentMethod: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, applePayAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, applePayAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(applePayAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(applePayAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(applePayAuthRequest.getCheckoutType())));

        var authResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(applePayAuthRequest);

        log.info(
                "PaymentAuthV2Controller.authorizeApplePay() :: applePay payment authorize response with orderNumber: {}, siteId: {}, resultCode: {} and authorizeResponse: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, applePayAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                v(PaymentsConstants.LOG_KEY_RESULT_CODE, authResponse.getResultCode()),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_RESPONSE, new Gson().toJson(authResponse)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, applePayAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(applePayAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(applePayAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(applePayAuthRequest.getCheckoutType())));

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    /**
     * This method makes a paypal V2 authorization call to adyen service
     * 
     * @param paypalAuthRequest
     * @param siteId
     * @return authResponseEntity
     */
    @Trace(dispatcher = true,
            metricName = "payments/v2/auth/paypal#POST")
    @PostMapping(path = "/auth/paypal",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> authorizePaypal(
            @Valid @RequestBody ATGAuthRequest paypalAuthRequest, @RequestHeader(required = false) String aeSite
    ) {

        if (null == paypalAuthRequest) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        log.debug("PaymentAuthV2Controller.authorizePaypal() :: paypal payment authorize request with orderNumber: {}, siteId: {} and authorizeRequest: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, paypalAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_REQUEST, new Gson().toJson(paypalAuthRequest)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, paypalAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(paypalAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(paypalAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(paypalAuthRequest.getCheckoutType())));

        log.info(
                "PaymentAuthV2Controller.authorizePaypal() :: paypal payment authorize request with orderNumber: {}, siteId: {}, paymentGroupId: {} and paymentMethod: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, paypalAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, paypalAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(paypalAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(paypalAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(paypalAuthRequest.getCheckoutType())));

        paypalAuthRequest.setSiteId(aeSite);

        var paypalAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(paypalAuthRequest);

        log.info(
                "PaymentAuthV2Controller.authorizePaypal() :: paypal payment authorize response with orderNumber: {}, siteId: {}, resultCode: {} and authorizationResponse: {}",
                v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, paypalAuthRequest.getOrderNumber()), v(PaymentsConstants.SITE_ID, aeSite),
                v(PaymentsConstants.LOG_KEY_RESULT_CODE, paypalAuthResponse.getResultCode()),
                v(PaymentsConstants.LOG_KEY_AUTHORIZATION_RESPONSE, new Gson().toJson(paypalAuthResponse)),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, paypalAuthRequest.getPaymentGroupId()),
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, CommonUtils.getPaymentMethod(paypalAuthRequest.getPaymentMethod())),
                v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getChannelType(paypalAuthRequest.getChannel())),
                v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getCheckoutType(paypalAuthRequest.getCheckoutType())));

        return new ResponseEntity<>(paypalAuthResponse, HttpStatus.OK);
    }

    /**
     * This method will retrieve V2 authorization details from adyen service
     * 
     * @param detailsRequest
     * @return paymentDetailsResponse
     */
    @Trace(dispatcher = true,
            metricName = "payments/v2/details#POST")
    @PostMapping(path = "/details",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<PaymentDetailsResponse> fetchPaymentDetails(@Valid @RequestBody PaymentDetailsRequest detailsRequest) {

        if (detailsRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        PaymentDetailsResponse detailsResponse;
        if (config.isUseMock()
                && mockPaymentDetailsServiceImpl != null) {
            log.info("PaymentAuthV2Controller.fetchPaymentDetails() :: Calling mock service");
            try {
                Thread.sleep(config.getSleepTimeoutMillis());
                detailsResponse = mockPaymentDetailsServiceImpl.retrieveDetailsResponse(detailsRequest);
            } catch (InterruptedException e) {
                log.error("PaymentAuthV2Controller.fetchPaymentDetails() :: Thread interrupted :: returning null", e);
                detailsResponse = null;
                Thread.currentThread()
                        .interrupt();
            }
        } else {
            log.debug("PaymentAuthV2Controller.fetchPaymentDetails() :: orderNumber: {}, cartId: {} and detailsRequest: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()),
                    v(PaymentsConstants.LOG_KEY_DETAIL_REQUEST, new Gson().toJson(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getDetailsRequestChannelType(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getDetailsRequestCheckoutType(detailsRequest)));

            log.info("PaymentAuthV2Controller.fetchPaymentDetails() :: details request with orderNumber: {}, cartId: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getDetailsRequestChannelType(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getDetailsRequestCheckoutType(detailsRequest)));

            detailsResponse = paymentDetailsV2ServiceImpl.retrieveDetailsResponse(detailsRequest);

            log.debug("PaymentAuthV2Controller.fetchPaymentDetails() :: orderNumber: {}, cartId: {} and detailsResponse: {} ",
                    v(PaymentsConstants.LOG_KEY_ORDER_NUMBER, detailsRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()),
                    v(PaymentsConstants.LOG_KEY_DETAIL_RESPONSE, new Gson().toJson(detailsResponse)),
                    v(PaymentsConstants.LOG_KEY_CHANNEL, CommonUtils.getDetailsRequestChannelType(detailsRequest)),
                    v(PaymentsConstants.LOG_KEY_CHECKOUT_TYPE, CommonUtils.getDetailsRequestCheckoutType(detailsRequest)));
        }
        return new ResponseEntity<>(detailsResponse, HttpStatus.OK);
    }

}
