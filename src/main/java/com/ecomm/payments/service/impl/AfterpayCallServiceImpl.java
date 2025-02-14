package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.afterpay.AfterpayAuthRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutRequest;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.service.AfterpayCallService;
import com.google.gson.Gson;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AfterpayCallServiceImpl implements AfterpayCallService {

    @Qualifier("afterpayServiceRestTemplate") private final RestTemplate afterpayServiceRestTemplate;

    private final PaymentsConfig paymentsConfig;

    @Override
    @CircuitBreaker(name = "checkoutAfterpayService",
            fallbackMethod = "fallbackCheckoutAfterpay")
    public ResponseEntity<String> createCheckout(AfterpayCheckoutRequest request, String siteId) {
        log.debug("AfterpayCallServiceImpl.createCheckout() :: request: {} ", new Gson().toJson(request));

        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = afterpayServiceRestTemplate.exchange(paymentsConfig.getAfterpayTokenEndPoint(), HttpMethod.POST,
                    new HttpEntity<>(request, getHeaders(siteId)), String.class);

        } catch (RestClientResponseException exception) {
            log.error("Exception while create checkout : {} ", exception.getResponseBodyAsString());
            var clientErrorResponse = new Gson().fromJson(exception.getResponseBodyAsString(), ClientErrorResponse.class);
            throw new ClientException(clientErrorResponse);
        }

        log.debug("AfterpayCallServiceImpl.createCheckout() :: response entity: {} ", new Gson().toJson(responseEntity));
        return responseEntity;
    }

    protected ResponseEntity<String> fallbackCheckoutAfterpay(AfterpayCheckoutRequest request, String siteId, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: fallback method triggered for {}",
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "AfterpayCallServiceImpl.createCheckout()"));
        }
        log.error("AfterpayCallServiceImpl POST Fallback method triggered for create checkout with request: {}, siteId: {} and exception : {}",
                new Gson().toJson(request), siteId, exception);
        ClientErrorResponse response = ClientErrorResponse.defaultErrorResponse;
        throw new ClientException(response);
    }

    @Override
    @CircuitBreaker(name = "authorizeAfterpayService",
            fallbackMethod = "fallbackAuthorizeAfterpay")
    public ResponseEntity<String> authorize(AfterpayAuthRequest request, String siteId) {
        log.debug("AfterpayCallServiceImpl.authorize() :: request: {} ", new Gson().toJson(request));

        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = afterpayServiceRestTemplate.exchange(paymentsConfig.getAfterpayAuthEndPoint(), HttpMethod.POST,
                    new HttpEntity<>(request, getHeaders(siteId)), String.class);

        } catch (RestClientResponseException exception) {
            log.error("Exception during authorize afterpay : {} ", exception.getResponseBodyAsString());
            var clientErrorResponse = new Gson().fromJson(exception.getResponseBodyAsString(), ClientErrorResponse.class);
            throw new ClientException(clientErrorResponse);
        }

        log.debug("AfterpayCallServiceImpl.authorize() :: response entity: {} ", new Gson().toJson(responseEntity));
        return responseEntity;
    }

    protected ResponseEntity<String> fallbackAuthorizeAfterpay(AfterpayAuthRequest request, String siteId, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: fallback method triggered for {}",
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "AfterpayCallServiceImpl.authorize()"));
        }
        if (exception instanceof ResourceAccessException
                && exception.getCause() instanceof SocketTimeoutException) {
            log.error("Connection Timeout during authorize afterpay payment with message {} and request {}", exception.getCause()
                    .getMessage(), new Gson().toJson(request));
            return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
        }
        log.error("AfterpayCallServiceImpl POST Fallback method triggered for authorize payment request: {}, siteId: {} with exception : {}",
                new Gson().toJson(request), siteId, exception);
        throw new ClientException(ClientErrorResponse.defaultErrorResponse);
    }

    @Override
    @CircuitBreaker(name = "reverseAuthAfterpayService",
            fallbackMethod = "fallbackReverseAuthAfterpay")
    public ResponseEntity<String> reverseAuth(AfterpayAuthRequest request, String siteId) {
        log.debug("AfterpayCallServiceImpl.reverseAuth() :: request: {} ", new Gson().toJson(request));

        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = afterpayServiceRestTemplate.exchange(new StringBuilder(paymentsConfig.getAfterpayReverseAuthEndPoint()).append(request.getToken())
                    .append(paymentsConfig.getAfterpayReverseAuthURI())
                    .toString(), HttpMethod.POST, new HttpEntity<>(request, getHeaders(siteId)), String.class);

        } catch (RestClientResponseException exception) {
            log.error("Exception during reverse auth afterpay : {} ", exception.getResponseBodyAsString());
            var clientErrorResponse = new Gson().fromJson(exception.getResponseBodyAsString(), ClientErrorResponse.class);
            throw new ClientException(clientErrorResponse);
        }

        log.debug("AfterpayCallServiceImpl.reverseAuth() :: response entity: {} ", new Gson().toJson(responseEntity));
        return responseEntity;
    }

    protected ResponseEntity<String> fallbackReverseAuthAfterpay(AfterpayAuthRequest request, String siteId, Exception exception) {
        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: fallback method triggered for {}",
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "AfterpayCallServiceImpl.reverseAuth()"));
        }
        if (exception instanceof ResourceAccessException
                && exception.getCause() instanceof SocketTimeoutException) {
            log.error("Connection Timeout during reverse auth of afterpay payment with message: {} and request: {}", exception.getCause()
                    .getMessage(), new Gson().toJson(request));
        }
        log.error("AfterpayCallServiceImpl POST Fallback method triggered for reverse auth payment request: {}, siteId: {} with exception: {}",
                new Gson().toJson(request), siteId, exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
    }

    private MultiValueMap<String, String> getHeaders(String siteId) {

        MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<>();
        headersMap.add(HttpHeaders.AUTHORIZATION, paymentsConfig.getAfterpaySiteIdAuthMap()
                .get(siteId));
        return headersMap;
    }

}
