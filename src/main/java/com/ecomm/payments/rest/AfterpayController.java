package com.ecomm.payments.rest;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.afterpay.AfterpayAuthResponse;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.service.AfterpayService;
import com.google.gson.Gson;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments/v1/afterpay")
@Slf4j
@RequiredArgsConstructor
public class AfterpayController {

    private final AfterpayService afterpayService;

    @Trace(dispatcher = true,
            metricName = "afterpay-cashapp-payments-checkout")
    @Operation(summary = "Get Token",
            description = "Make a create checkout call")
    @ApiResponses(value =
    { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Get token for afterpay or cashApp pay successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AfterpayCheckoutResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400,422,503",
                description = "Get token for afterpay or cashApp pay failed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = AfterpayCheckoutResponse.class))) })
    @PostMapping(path = "/token",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<AfterpayCheckoutResponse> fetchToken(@RequestBody ATGAuthRequest authRequest, @RequestHeader(value = "aeSite") String siteId) {

        if (null == authRequest) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        authRequest.setSiteId(siteId);

        log.debug("AfterpayController.fetchToken() :: paymentMethod: {} fetch token request: {}", authRequest.getPaymentMethod()
                .getType(), new Gson().toJson(authRequest));

        log.info("AfterpayController.fetchToken() :: paymentMethod: {} fetch token request with orderNumber: {}, siteId: {}, paymentGroupId: {}",
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                v(CommonKeys.PAYMENT_GROUP_ID.key(), authRequest.getPaymentGroupId()));

        var response = afterpayService.fetchToken(authRequest);

        log.debug("AfterpayController.fetchToken() :: paymentMethod: {} fetch token response: {}", authRequest.getPaymentMethod()
                .getType(), new Gson().toJson(response));

        log.info("AfterpayController.fetchToken() :: paymentMethod: {} fetch token response with orderNumber: {}, siteId: {}, paymentGroupId: {}",
                v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, authRequest.getPaymentMethod()
                        .getType()),
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                v(CommonKeys.PAYMENT_GROUP_ID.key(), authRequest.getPaymentGroupId()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Trace(dispatcher = true,
            metricName = "afterpay-cashapp-payments-auth")
    @Operation(summary = "Authorize",
            description = "Make a authorize call")
    @ApiResponses(value =
    { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Authorize afterpay or cashApp pay successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AfterpayAuthResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400,422,503",
                description = "Authorize afterpay or cashApp pay failed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = AfterpayAuthResponse.class))) })
    @PostMapping(path = "/auth",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<AfterpayAuthResponse> authorizePayment(
            @RequestBody PaymentDetailsRequest detailsRequest, @RequestHeader(value = "aeSite") String siteId
    ) {

        if (null == detailsRequest) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        log.debug("AfterpayController.authorizePayment() :: authorize request: {}", new Gson().toJson(detailsRequest));

        log.info("AfterpayController.authorizePayment() :: authorize request with orderNumber: {}, siteId: {} and cartId: {}",
                v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()));

        var authorizeResponse = afterpayService.authorizePayment(detailsRequest, siteId);

        log.info("AfterpayController.authorizePayment() :: authorize response with orderNumber: {}, siteId: {} and cartId: {}",
                v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                v(PaymentsConstants.LOG_KEY_CART_ID, detailsRequest.getCartId()));

        log.debug("AfterpayController.authorizePayment() :: authorize response: {}", new Gson().toJson(authorizeResponse));

        return new ResponseEntity<>(authorizeResponse, HttpStatus.OK);
    }

}
