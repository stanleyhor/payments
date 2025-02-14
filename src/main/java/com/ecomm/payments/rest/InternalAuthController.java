package com.ecomm.payments.rest;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.service.PaymentAuthorizationService;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/payments/v1")
@Slf4j
@RequiredArgsConstructor
public class InternalAuthController {

    private final PaymentsConfig paymentsConfig;

    @Qualifier("mockPaymentAuthorizationServiceImpl") private final PaymentAuthorizationService mockPaymentAuthorizationServiceImpl;

    @Qualifier("paymentAuthorizationServiceImpl") private final PaymentAuthorizationService paymentAuthorizationServiceImpl;

    @Trace(dispatcher = true,
            metricName = "internal/payments/v1/authorization#POST")
    @PostMapping(path = "/authorization",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json;charset=UTF-8")
    public ResponseEntity<ATGAuthResponse> internalAuthorization(
            @Valid @RequestBody ATGAuthRequest authRequest, @RequestHeader(value = "aeSite",
                    required = false) String siteId
    ) {

        authRequest.setSiteId(siteId);
        ATGAuthResponse authResponse;
        if (paymentsConfig.isUseMock()
                && mockPaymentAuthorizationServiceImpl != null) {
            log.info("Calling internal authorization details mock service");
            try {
                Thread.sleep(paymentsConfig.getSleepTimeoutMillis());
                authResponse = mockPaymentAuthorizationServiceImpl.authorizeAplazoPayment(authRequest);
            } catch (InterruptedException e) {
                log.error("Exception occured due to thread interrupted :: returning null", e);
                authResponse = null;
                Thread.currentThread()
                        .interrupt();
            }
        } else {

            authResponse = paymentAuthorizationServiceImpl.authorizeAplazoPayment(authRequest);
        }
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

}
