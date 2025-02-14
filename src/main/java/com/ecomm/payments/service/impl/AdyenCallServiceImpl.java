package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.exception.AdyenClientFallbackException;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.service.AdyenCallService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdyenCallServiceImpl implements AdyenCallService {

    private final RestTemplate restTemplate;

    @Override
    @CircuitBreaker(name = "authorizationEndPoint",
            fallbackMethod = "fallbackAuthorizePayment")
    public AdyenAuthResponse callService(AdyenAuthRequest convertedRequest, String idempotencyKey) {

        ResponseEntity<AdyenAuthResponse> response = null;
        String merchantOrderReference = null;

        try {

            HttpHeaders headers = new HttpHeaders();

            if (idempotencyKey != null) {
                headers.set(PaymentsConstants.IDEMPOTENCY_KEY, idempotencyKey);
            }

            if (convertedRequest != null) {
                merchantOrderReference = convertedRequest.getMerchantOrderReference();

                response = restTemplate.exchange(PaymentsConstants.PAYMENTS, HttpMethod.POST, new HttpEntity<>(convertedRequest, headers),
                        AdyenAuthResponse.class);
            }

        } catch (RestClientResponseException e) {

            log.error("Exception in AdyenAuth CallService for orderNumber: {} and exception: {} ", v(CommonKeys.ORDER_NUMBER.key(), merchantOrderReference),
                    e.getResponseBodyAsString());
            throw new AdyenClientException(e.getResponseBodyAsString());
        }

        return response != null ? response.getBody() : null;

    }

    public AdyenAuthResponse fallbackAuthorizePayment(AdyenAuthRequest convertedRequest, String idempotencyKey, CallNotPermittedException exception) {

        log.warn("circuit-breaker-open :: fallback method triggered for {}", v(PaymentsConstants.LOG_KEY_METHOD_NAME, "AdyenCallServiceImpl.callService()"));

        log.error("AdyenCallServiceImpl POST Fallback method triggered for auth call with orderNumber: {} and idempotencyKey: {}",
                v(CommonKeys.ORDER_NUMBER.key(), convertedRequest.getMerchantOrderReference()), idempotencyKey);

        throw new AdyenClientFallbackException(exception.getMessage());

    }

}
