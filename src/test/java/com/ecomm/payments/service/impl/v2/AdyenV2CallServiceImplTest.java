package com.ecomm.payments.service.impl.v2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.exception.AdyenClientFallbackException;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.CustomErrorResponse;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.util.AdyenResponseUtilTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.apache.logging.log4j.util.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class AdyenV2CallServiceImplTest {

    @InjectMocks
    private AdyenV2CallServiceImpl adyenV2CallServiceImpl;

    @Mock
    private RestTemplate adyenV2AuthRestTemplate;

    @Mock
    private PaymentsConfig paymentsConfig;

    public static ObjectMapper objMapper;

    @Mock
    private ClientErrorResponse clientErrorResponse;

    @BeforeEach
    void initialize() {
        MockitoAnnotations.openMocks(this);
        objMapper = new ObjectMapper();
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        paymentsConfig = TestPaymentsConfig.getPaymentConfig();
        adyenV2CallServiceImpl = new AdyenV2CallServiceImpl(adyenV2AuthRestTemplate, paymentsConfig);
    }

    @Test
	void testCallAuthServiceReturnSuccessCode() {
		when(adyenV2AuthRestTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(AdyenAuthResponse.class)))
				.thenReturn(getJsonResponseEntity());
		var responseEntity = adyenV2CallServiceImpl.authorize(mockedAdyenAuthRequest(),
				"c56b36f8-fc28-94ec-bfc4-9e12dee9b7b7");
		assertNotNull(responseEntity.getBody());
	}

    @Test
	void testCallAuthServiceReturnErrorCode() throws JsonProcessingException {

		when(adyenV2AuthRestTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(AdyenAuthResponse.class)))
				.thenThrow(new RestClientResponseException("Exception", 422, null, null,
						objMapper.writeValueAsBytes(new CustomErrorResponse()), null));
		Throwable exception = assertThrows(AdyenClientException.class, () -> adyenV2CallServiceImpl
				.authorize(mockedAdyenAuthRequest(), "c56b36f8-fc28-94ec-bfc4-9e12dee9b7b7"));
		assertNotNull(exception);
	}

    @Test
    void testfallbackAdyenV2AuthCallService() {
        var circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        var circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        var callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        Throwable exception = assertThrows(AdyenClientFallbackException.class, () -> adyenV2CallServiceImpl
                .fallbackAdyenV2AuthCallService(mockedAdyenAuthRequest(), "c56b36f8-fc28-94ec-bfc4-9e12dee9b7b7", callNotPermittedException));
        assertNotNull(exception);

        exception = assertThrows(AdyenClientFallbackException.class, () -> adyenV2CallServiceImpl.fallbackAdyenV2AuthCallService(mockedAdyenAuthRequest(),
                "c56b36f8-fc28-94ec-bfc4-9e12dee9b7b7", new InternalException("test")));

        assertNotNull(exception);
    }

    @Test
	void testIdempotencyKey() {
		when(adyenV2AuthRestTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
				ArgumentMatchers.any(HttpEntity.class), eq(AdyenAuthResponse.class)))
				.thenReturn(getJsonResponseEntity());
		var emptyResponse = adyenV2CallServiceImpl.authorize(new AdyenAuthRequest(), null);
		assertNotNull(emptyResponse);
	}

    @Test
    void getAdyenDetailsResponse() throws Exception {

        Mockito.when(adyenV2AuthRestTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(AdyenResponseUtilTest.getAdyenDetailsResponse()));

        var adyenDetailsResponse = adyenV2CallServiceImpl.retrieveAuthDetails(mockedAdyenDetailsRequest());

        assertNotNull(adyenDetailsResponse.getBody());

    }

    @Test
    void getAdyenDetailsErrorResponse() throws Exception {

        RestClientResponseException ex = new RestClientResponseException("", 422, null, null, objMapper.writeValueAsBytes(getClientErrorResponse()), null);

        clientErrorResponse = new Gson().fromJson(ex.getResponseBodyAsString(), ClientErrorResponse.class);
        Mockito.when(adyenV2AuthRestTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenThrow(ex);

        assertThrows(ClientException.class, () -> adyenV2CallServiceImpl.retrieveAuthDetails(mockedAdyenDetailsRequest()));

        assertNotNull(new ClientException(clientErrorResponse));
    }

    @Test
    void testFallbackAdyenV2AuthDetailsService() {

        clientErrorResponse = ClientErrorResponse.defaultErrorResponse;

        assertThrows(ClientException.class, () -> adyenV2CallServiceImpl.fallbackAdyenV2AuthDetailsService(mockedAdyenDetailsRequest(), null));

        assertNotNull(clientErrorResponse);

        var circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        var circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        var callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        assertThrows(ClientException.class,
                () -> adyenV2CallServiceImpl.fallbackAdyenV2AuthDetailsService(mockedAdyenDetailsRequest(), callNotPermittedException));
    }

    private AdyenAuthRequest mockedAdyenAuthRequest() {
        AdyenAuthRequest authRequest = new AdyenAuthRequest();
        Amount amt = new Amount();
        amt.setCurrency("USD");
        amt.setValue("7264");
        authRequest.setAmount(amt);
        return authRequest;
    }

    private AdyenDetailsRequest mockedAdyenDetailsRequest() {
        AdyenDetailsRequest detailsRequest = new AdyenDetailsRequest();
        return detailsRequest;
    }

    private ResponseEntity<AdyenAuthResponse> getJsonResponseEntity() {
        return new ResponseEntity<>(new AdyenAuthResponse(), HttpStatus.OK);
    }

    private ClientErrorResponse getClientErrorResponse() {
        ClientErrorResponse response = new ClientErrorResponse();
        response.setErrorCode("101");
        response.setErrorKey("error.checkout.placeOrder.paymentInfo.internalException");
        response.setMessage("Internal Server Error");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
    }

}
