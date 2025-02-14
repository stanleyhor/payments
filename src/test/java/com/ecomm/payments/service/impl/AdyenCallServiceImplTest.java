package com.ecomm.payments.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.exception.AdyenClientFallbackException;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.CustomErrorResponse;
import com.ecomm.payments.model.adyen.Amount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class AdyenCallServiceImplTest {

    @InjectMocks
    AdyenCallServiceImpl adyenAuthServiceImpl;
    @Mock
    private RestTemplate restTemplate;
    private PaymentsConfig paymentsConfig;
    public static ObjectMapper objMapper;

    @BeforeEach
    void setUp() {
        objMapper = new ObjectMapper();
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        paymentsConfig = new PaymentsConfig();
        paymentsConfig.setPaymentsURL("http:localhost:");
        adyenAuthServiceImpl = new AdyenCallServiceImpl(restTemplate);
    }

    @Test
	void testCallAuthServiceReturnSuccessCode() {
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(AdyenAuthResponse.class)))
						.thenReturn(getJsonResponseEntity());
		var stringResponseEntity = adyenAuthServiceImpl.callService(mockedRequest(), "error");
		assertNotNull(stringResponseEntity);
	}

    @Test
	void testCallAuthServiceReturnErrorCode() throws JsonProcessingException {

		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(AdyenAuthResponse.class)))
						.thenThrow(new RestClientResponseException("Exception", 422, null, null,
								objMapper.writeValueAsBytes(new CustomErrorResponse()), null));
		Throwable exception = assertThrows(AdyenClientException.class,
				() -> adyenAuthServiceImpl.callService(mockedRequest(), "message"));
		assertNotNull(exception);
	}

    @Test
    void testFallbackAuthorizePaymentThrowCallNotPermittedException() {
        var adyenAuthServiceImpl = new AdyenCallServiceImpl(restTemplate);
        var circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        var circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        var callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        Throwable exception = assertThrows(AdyenClientFallbackException.class,
                () -> adyenAuthServiceImpl.fallbackAuthorizePayment(mockedRequest(), "error", callNotPermittedException));
        assertNotNull(exception);
    }

    @Test
	void testgetIdempotencyKey() {
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(AdyenAuthResponse.class)))
						.thenReturn(getJsonResponseEntity());
		var emptyResponse = adyenAuthServiceImpl.callService(new AdyenAuthRequest(), null);
		assertNotNull(emptyResponse);
	}

    @Test ///
    void testAdyenAuthRequestasNull() {
        var emptyResponse = adyenAuthServiceImpl.callService(null, "idem");
        assertThat(emptyResponse).isEqualTo(null);
    }

    private ResponseEntity<AdyenAuthResponse> getJsonResponseEntity() {
        return new ResponseEntity<>(new AdyenAuthResponse(), HttpStatus.OK);
    }

    private AdyenAuthRequest mockedRequest() {
        AdyenAuthRequest authRequest = new AdyenAuthRequest();
        Amount amt = new Amount();
        amt.setCurrency("USD");
        amt.setValue("7264");
        authRequest.setAmount(amt);
        return authRequest;
    }

    public String generateWithoutErrorCodeResponse() {
        String errorJson = "{\"status\":409,\"message\":\"request already processed or in progress\",\"errorType\":\"validation\"}";
        return errorJson;
    }

}
