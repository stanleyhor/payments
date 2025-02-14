package com.ecomm.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.AuthorizeResponse;
import com.ecomm.payments.util.BTPaypalMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

class BTClientServiceImplTest {

    @Mock
    BTPaypalMapper btPaypalMapper;

    @Mock
    private HttpGraphQlClient httpGraphQlClient;

    @InjectMocks
    BTClientServiceImpl btClientServiceImpl;

    @Mock
    private GraphQlClient.RequestSpec requestSpec;

    @Mock
    Mono<ClientGraphQlResponse> clientGraphQlResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMakeAuthCall() {
        AuthorizationRequest authRequest = new AuthorizationRequest();

        ClientGraphQlResponse successfulResponse = mock(ClientGraphQlResponse.class);
        when(httpGraphQlClient.documentName(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variables(any())).thenReturn(requestSpec);
        when(requestSpec.execute()).thenReturn(Mono.just(successfulResponse));

        when(successfulResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());

        ClientGraphQlResponse authResponse = btClientServiceImpl.makeAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

        when(requestSpec.execute()).thenReturn(clientGraphQlResponse);

        when(clientGraphQlResponse.block()).thenReturn(null);

        authResponse = btClientServiceImpl.makeAuthCall(authRequest, "AEO_US");
        assertNull(authResponse);

    }

    @Test
    void testMakeAuthCallFailed() {
        AuthorizationRequest authRequest = new AuthorizationRequest();

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);

        when(errorResponse.isValid()).thenReturn(false);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        when(httpGraphQlClient.documentName(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variables(any())).thenReturn(requestSpec);
        when(requestSpec.execute()).thenReturn(Mono.just(errorResponse));

        ClientGraphQlResponse authResponse = btClientServiceImpl.makeAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

    }

    @Test
    void testFallbackMakeAuthCall() {

        AuthorizationRequest authRequest = new AuthorizationRequest();
        authRequest.setOrderNumber("order123");

        String siteId = "AEO_US";

        Exception exception = new Exception();
        assertThrows(ClientException.class, () -> btClientServiceImpl.fallbackMakeAuthCall(authRequest, siteId, exception));

        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreaker cb = CircuitBreaker.of("test", config);
        CallNotPermittedException e = CallNotPermittedException.createCallNotPermittedException(cb);

        assertThrows(ClientException.class, () -> btClientServiceImpl.fallbackMakeAuthCall(authRequest, siteId, e));

    }

    @Test
    void testMakePaypalAuthCall() {

        AuthorizationRequest authRequest = new AuthorizationRequest();

        ClientGraphQlResponse successfulResponse = mock(ClientGraphQlResponse.class);
        when(httpGraphQlClient.documentName(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variables(any())).thenReturn(requestSpec);
        when(requestSpec.execute()).thenReturn(Mono.just(successfulResponse));

        when(successfulResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());

        ClientGraphQlResponse authResponse = btClientServiceImpl.makePayPalAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

        when(requestSpec.execute()).thenReturn(clientGraphQlResponse);

        when(clientGraphQlResponse.block()).thenReturn(null);

        authResponse = btClientServiceImpl.makePayPalAuthCall(authRequest, "AEO_US");
        assertNull(authResponse);
    }

    @Test
    void testMakePayPalAuthCallFailed() {
        AuthorizationRequest authRequest = new AuthorizationRequest();

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);

        when(errorResponse.isValid()).thenReturn(false);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        when(httpGraphQlClient.documentName(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variables(any())).thenReturn(requestSpec);
        when(requestSpec.execute()).thenReturn(Mono.just(errorResponse));

        ClientGraphQlResponse authResponse = btClientServiceImpl.makePayPalAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

    }

    @Test
    void testFallbackMakePayPalAuthCall() {

        AuthorizationRequest authRequest = new AuthorizationRequest();
        authRequest.setOrderNumber("order123");

        String siteId = "AEO_US";

        Exception exception = new Exception();
        assertThrows(ClientException.class, () -> btClientServiceImpl.fallbackMakePayPalAuthCall(authRequest, siteId, exception));

        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreaker cb = CircuitBreaker.of("test", config);
        CallNotPermittedException e = CallNotPermittedException.createCallNotPermittedException(cb);

        assertThrows(ClientException.class, () -> btClientServiceImpl.fallbackMakePayPalAuthCall(authRequest, siteId, e));

    }

    @Test
    void testMakeCreateClientTokenCall() {

        ClientGraphQlResponse successfulResponse = mock(ClientGraphQlResponse.class);
        when(httpGraphQlClient.documentName(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variables(any())).thenReturn(requestSpec);
        when(requestSpec.execute()).thenReturn(Mono.just(successfulResponse));

        when(successfulResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());

        ClientGraphQlResponse clientTokenResponse = btClientServiceImpl.makeCreateClientTokenCall("AEO_US");
        assertNotNull(clientTokenResponse);

        when(requestSpec.execute()).thenReturn(clientGraphQlResponse);

        when(clientGraphQlResponse.block()).thenReturn(null);

        clientTokenResponse = btClientServiceImpl.makeCreateClientTokenCall("AEO_US");
        assertNull(clientTokenResponse);

    }

    @Test
    void testMakeCreateClientTokenCallFailed() {

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);

        when(errorResponse.isValid()).thenReturn(false);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        when(httpGraphQlClient.documentName(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variables(any())).thenReturn(requestSpec);
        when(requestSpec.execute()).thenReturn(Mono.just(errorResponse));

        ClientGraphQlResponse authResponse = btClientServiceImpl.makeCreateClientTokenCall("AEO_US");
        assertNotNull(authResponse);

    }

    @Test
    void testFallbackMakePayPalClientTokenCall() {

        String siteId = "AEO_US";

        Exception exception = new Exception();
        assertThrows(ClientException.class, () -> btClientServiceImpl.fallbackMakeCreateClientTokenCall(siteId, exception));

        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreaker cb = CircuitBreaker.of("test", config);
        CallNotPermittedException e = CallNotPermittedException.createCallNotPermittedException(cb);

        assertThrows(ClientException.class, () -> btClientServiceImpl.fallbackMakeCreateClientTokenCall(siteId, e));
    }

}
