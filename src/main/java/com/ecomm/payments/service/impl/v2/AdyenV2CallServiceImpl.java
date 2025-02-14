package com.ecomm.payments.service.impl.v2;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.exception.AdyenClientFallbackException;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.service.v2.AdyenV2CallService;
import com.google.gson.Gson;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdyenV2CallServiceImpl implements AdyenV2CallService {

    @Qualifier("adyenV2AuthRestTemplate") private final RestTemplate adyenV2AuthRestTemplate;

    private final PaymentsConfig paymentsConfig;

    @Override
    @CircuitBreaker(name = "adyenV2AuthCallService",
            fallbackMethod = "fallbackAdyenV2AuthCallService")
    public ResponseEntity<AdyenAuthResponse> authorize(AdyenAuthRequest convertedRequest, String idempotencyKey) {

        ResponseEntity<AdyenAuthResponse> responseEntity = null;
        String merchantOrderReference = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasText(idempotencyKey)) {
                headers.set(PaymentsConstants.IDEMPOTENCY_KEY, idempotencyKey);
            }
            merchantOrderReference = convertedRequest.getMerchantOrderReference();

            responseEntity = adyenV2AuthRestTemplate.exchange(PaymentsConstants.PAYMENTS, HttpMethod.POST, new HttpEntity<>(convertedRequest, headers),
                    AdyenAuthResponse.class);
        } catch (RestClientResponseException e) {

            log.error("Exception in AdyenV2AuthCallService for orderNumber {} and exception {} ", v(CommonKeys.ORDER_NUMBER.key(), merchantOrderReference),
                    e.getResponseBodyAsString());
            throw new AdyenClientException(e.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public ResponseEntity<AdyenAuthResponse> fallbackAdyenV2AuthCallService(AdyenAuthRequest convertedRequest, String idempotencyKey, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: fallback method triggered for {}",
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "AdyenV2CallServiceImpl.callService()"));
        }

        log.error("AdyenV2CallServiceImpl POST Fallback method triggered for auth call with orderNumber = {} idempotencyKey={}",
                v(CommonKeys.ORDER_NUMBER.key(), convertedRequest.getMerchantOrderReference()), idempotencyKey);

        throw new AdyenClientFallbackException(exception.getMessage());

    }

    @CircuitBreaker(name = "adyenV2AuthDetailsService",
            fallbackMethod = "fallbackAdyenV2AuthDetailsService")
    public ResponseEntity<AdyenDetailsResponse> retrieveAuthDetails(AdyenDetailsRequest adyenDetailsRequest) {

        ResponseEntity<AdyenDetailsResponse> responseEntity = null;
        try {
            responseEntity = adyenV2AuthRestTemplate.exchange(paymentsConfig.getPaymentsDetailsEndPoint(), HttpMethod.POST,
                    new HttpEntity<>(adyenDetailsRequest), AdyenDetailsResponse.class);
        } catch (RestClientResponseException e) {
            log.error("Exception while fetching authorization details using Adyen V2 service : {} ", e.getResponseBodyAsString());
            ClientErrorResponse clientErrorResponse = new Gson().fromJson(e.getResponseBodyAsString(), ClientErrorResponse.class);
            throw new ClientException(clientErrorResponse);
        }

        return responseEntity;
    }

    public ResponseEntity<AdyenDetailsResponse> fallbackAdyenV2AuthDetailsService(AdyenDetailsRequest adyenDetailsRequest, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: fallback method triggered for {}",
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "AdyenV2CallServiceImpl.retrieveDetailsResponse()"));
        }
        log.error("AdyenV2CallServiceImpl POST Fallback method triggered for details call with request paymentData = {} with exception : {}",
                adyenDetailsRequest.getPaymentData(), exception);
        ClientErrorResponse response = ClientErrorResponse.defaultErrorResponse;
        throw new ClientException(response);
    }

}
