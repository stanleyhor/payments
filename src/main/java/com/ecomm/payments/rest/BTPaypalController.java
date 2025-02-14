package com.ecomm.payments.rest;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.AuthorizeResponse;
import com.ecomm.payments.model.braintree.ClientTokenResponse;
import com.ecomm.payments.model.braintree.PaypalAuthResponse;
import com.ecomm.payments.service.BTPaypalService;
import com.google.gson.Gson;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("payments/v1/paypal/")
@Slf4j
@RequiredArgsConstructor
public class BTPaypalController {

    private final BTPaypalService btPaypalService;

    @Trace(dispatcher = true,
            metricName = "bt-payments-auth-payment")
    @Operation(summary = "BT Payment Paypal Auth Payment",
            description = "Make a BT paypal auth Payment call")
    @ApiResponses(value =
    { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "BT Payment Paypal Auth Payment successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthorizeResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400,422,503",
                description = "BT Payment Paypal Auth Payment failed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = AuthorizeResponse.class))) })
    @PostMapping("authpayment")
    public Object authorizePayment(@RequestBody AuthorizationRequest authRequest, @RequestHeader(value = "aeSite") String siteId) {

        log.info("BTPaypalController.authorizePayment() :: orderNumber: {}, paymentMethod: {}, siteId: {} ",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        return btPaypalService.makeAuthCall(authRequest, siteId);
    }

    @Trace(dispatcher = true,
            metricName = "bt-payments-auth-paypal")
    @Operation(summary = "BT Payment Paypal Auth",
            description = "Make a BT paypal auth call")
    @ApiResponses(value =
    { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "BT Payment Paypal Auth successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PaypalAuthResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400,422,503",
                description = "BT Payment Paypal Auth failed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = PaypalAuthResponse.class))) })
    @PostMapping("auth")
    public Object authorizePaypal(@RequestBody AuthorizationRequest authRequest, @RequestHeader(value = "aeSite") String siteId) {

        log.info("BTPaypalController.authorizePaypal() :: orderNumber: {}, paymentMethod: {}, siteId: {} Request: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                v(PaymentsConstants.LOG_KEY_SITE_ID, siteId), new Gson().toJson(authRequest));

        return btPaypalService.makePayPalAuthCall(authRequest, siteId);
    }

    @Trace(dispatcher = true,
            metricName = "bt-payments-clienttoken-paypal")
    @Operation(summary = "BT Payment Paypal Auth",
            description = "Make a BT paypal auth call")
    @ApiResponses(value =
    { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "BT Payment Paypal clientToken successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ClientTokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400,422,503",
                description = "BT Payment Paypal clientToken failed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ClientTokenResponse.class))) })
    @GetMapping("token")
    public Object getPayPalClientToken(@RequestHeader(value = "aeSite") String siteId) {

        log.info("BTPaypalController.getPayPalClientToken() :: siteId: {} ", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        return btPaypalService.getPayPalClientToken(siteId);
    }

}
