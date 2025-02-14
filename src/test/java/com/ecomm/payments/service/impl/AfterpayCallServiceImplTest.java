package com.ecomm.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.CustomErrorResponse;
import com.ecomm.payments.model.afterpay.AfterpayAuthRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutRequest;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

@ExtendWith(MockitoExtension.class)
public class AfterpayCallServiceImplTest {

    private AfterpayCallServiceImpl afterpayCallServiceImpl;

    @Mock
    private RestTemplate restTemplate;

    public static ObjectMapper objMapper;

    private PaymentsConfig paymentsConfig;

    @BeforeEach
    void setUp() {
        objMapper = new ObjectMapper();
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        paymentsConfig = TestPaymentsConfig.getPaymentConfig();
        afterpayCallServiceImpl = new AfterpayCallServiceImpl(restTemplate, paymentsConfig);
    }

    @Test
	void testCreateCheckout() throws JsonProcessingException {
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		var stringResponseEntity = afterpayCallServiceImpl.createCheckout(AfterpayCheckoutRequest.builder().build(),
				"AEO_US");
		assertNotNull(stringResponseEntity);

		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
				.thenThrow(new RestClientResponseException("Exception", 422, null, null,
						objMapper.writeValueAsBytes(new CustomErrorResponse()), null));
		Throwable exception = assertThrows(ClientException.class,
				() -> afterpayCallServiceImpl.createCheckout(AfterpayCheckoutRequest.builder().build(), "AEO_US"));
		assertNotNull(exception);
	}

    @Test
    void testFallbackCheckoutPayment() {
        var circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        var circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        var callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        Throwable exception = assertThrows(ClientException.class, () -> afterpayCallServiceImpl.fallbackCheckoutAfterpay(AfterpayCheckoutRequest.builder()
                .build(), "AEO_US", callNotPermittedException));
        assertNotNull(exception);

        var resourceAccessException = new ResourceAccessException("Connect timeout expection");
        exception = assertThrows(ClientException.class, () -> afterpayCallServiceImpl.fallbackCheckoutAfterpay(AfterpayCheckoutRequest.builder()
                .build(), "AEO_US", resourceAccessException));
        assertNotNull(exception);
    }

    @Test
	void testAuthorize() throws JsonProcessingException {
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		var stringResponseEntity = afterpayCallServiceImpl.authorize(AfterpayAuthRequest.builder().build(), "AEO_US");
		assertNotNull(stringResponseEntity);

		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
				.thenThrow(new RestClientResponseException("Exception", 422, null, null,
						objMapper.writeValueAsBytes(new CustomErrorResponse()), null));
		Throwable exception = assertThrows(ClientException.class,
				() -> afterpayCallServiceImpl.authorize(AfterpayAuthRequest.builder().build(), "AEO_US"));
		assertNotNull(exception);
	}

    @Test
    void testFallbackAuthorizePayment() {
        var circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        var circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        var callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        Throwable exception = assertThrows(ClientException.class, () -> afterpayCallServiceImpl.fallbackAuthorizeAfterpay(AfterpayAuthRequest.builder()
                .build(), "AEO_US", callNotPermittedException));
        assertNotNull(exception);

        var resourceAccessException = new ResourceAccessException("Connect timeout expection");
        exception = assertThrows(ClientException.class, () -> afterpayCallServiceImpl.fallbackAuthorizeAfterpay(AfterpayAuthRequest.builder()
                .build(), "AEO_US", resourceAccessException));
        assertNotNull(exception);

        resourceAccessException.initCause(new SocketTimeoutException("Socket Connect Timeout : connection request timeout"));
        assertDoesNotThrow(() -> afterpayCallServiceImpl.fallbackAuthorizeAfterpay(AfterpayAuthRequest.builder()
                .build(), "AEO_US", resourceAccessException));
    }

    @Test
	void testReverseAuth() throws JsonProcessingException {
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
				.thenReturn(new ResponseEntity<>("Invoked reverse Auth", HttpStatus.OK));
		var stringResponseEntity = afterpayCallServiceImpl.reverseAuth(AfterpayAuthRequest.builder().build(), "AEO_US");
		assertNotNull(stringResponseEntity);

		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
				.thenThrow(new RestClientResponseException("Exception", 422, null, null,
						objMapper.writeValueAsBytes(new CustomErrorResponse()), null));
		Throwable exception = assertThrows(ClientException.class,
				() -> afterpayCallServiceImpl.reverseAuth(AfterpayAuthRequest.builder().build(), "AEO_US"));
		assertNotNull(exception);
	}

    @Test
    void testFallbackReverseAuthAfterpay() {
        var circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        var circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        var callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        var response = afterpayCallServiceImpl.fallbackReverseAuthAfterpay(AfterpayAuthRequest.builder()
                .build(), "AEO_US", callNotPermittedException);
        assertNotNull(response);

        var resourceAccessException = new ResourceAccessException("Connect timeout expection");
        response = afterpayCallServiceImpl.fallbackReverseAuthAfterpay(AfterpayAuthRequest.builder()
                .build(), "AEO_US", resourceAccessException);
        assertNotNull(response);

        resourceAccessException.initCause(new SocketTimeoutException("Socket Connect Timeout : connection request timeout"));
        assertDoesNotThrow(() -> afterpayCallServiceImpl.fallbackReverseAuthAfterpay(AfterpayAuthRequest.builder()
                .build(), "AEO_US", resourceAccessException));
    }

}
