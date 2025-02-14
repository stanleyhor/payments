package com.ecomm.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecomm.payments.exception.AuthorizationFailedException;
import com.ecomm.payments.exception.DuplicateAuthorizationException;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.AuthorizeResponse;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse;
import com.ecomm.payments.model.braintree.ClientTokenResponse;
import com.ecomm.payments.model.braintree.ClientTokenResponse.CreateClientToken;
import com.ecomm.payments.util.BTPaypalMapper;
import com.ecomm.payments.util.BTPaypalRepoWrapper;
import com.ecomm.payments.util.BTPaypalRequestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

class BTPaypalServiceImplTest {

    @Mock
    BTClientServiceImpl btClientServiceImpl;

    @Mock
    BTPaypalMapper btPaypalMapper;

    @Mock
    BTPaypalRepoWrapper btPaypalRepoWrapper;

    @InjectMocks
    BTPaypalServiceImpl btPaypalServiceImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void makeAuthCall() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();

        ClientGraphQlResponse successfulResponse = mock(ClientGraphQlResponse.class);

        when(btClientServiceImpl.makeAuthCall(authRequest, "AEO_US")).thenReturn(successfulResponse);
        when(successfulResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());

        ResponseEntity<Object> authResponse = btPaypalServiceImpl.makeAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

        authRequest.setPaymentMethod(null);
        authResponse = btPaypalServiceImpl.makeAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

        AuthorizationRequest authRequest1 = new AuthorizationRequest();
        when(btClientServiceImpl.makeAuthCall(new AuthorizationRequest(), "AEO_US")).thenReturn(null);

        assertThrows(AuthorizationFailedException.class, () -> btPaypalServiceImpl.makeAuthCall(authRequest1, "AEO_US"));

    }

    @Test
    void testMakeAuthCallFailed() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);

        when(btClientServiceImpl.makeAuthCall(authRequest, "AEO_US")).thenReturn(errorResponse);
        when(errorResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());
        when(errorResponse.isValid()).thenReturn(false);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        assertThrows(DuplicateAuthorizationException.class, () -> btPaypalServiceImpl.makeAuthCall(authRequest, "AEO_US"));

    }

    @Test
    void makePaypalAuthCall() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();

        ClientGraphQlResponse successfulResponse = mock(ClientGraphQlResponse.class);

        when(btClientServiceImpl.makePayPalAuthCall(authRequest, "AEO_US")).thenReturn(successfulResponse);
        when(successfulResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());

        ResponseEntity<Object> authResponse = btPaypalServiceImpl.makePayPalAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

        authRequest.setPaymentMethod(null);
        authResponse = btPaypalServiceImpl.makePayPalAuthCall(authRequest, "AEO_US");
        assertNotNull(authResponse);

        AuthorizationRequest authRequest1 = new AuthorizationRequest();
        when(btClientServiceImpl.makePayPalAuthCall(authRequest1, "AEO_US")).thenReturn(null);

        assertThrows(AuthorizationFailedException.class, () -> btPaypalServiceImpl.makePayPalAuthCall(authRequest1, "AEO_US"));

    }

    @Test
    void testMakePayPalAuthCallFailed() {
        AuthorizationRequest authRequest = new AuthorizationRequest();

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);

        when(btClientServiceImpl.makePayPalAuthCall(authRequest, "AEO_US")).thenReturn(errorResponse);
        when(errorResponse.toEntity(BTPaypalAuthResponse.class)).thenReturn(new BTPaypalAuthResponse());
        when(errorResponse.isValid()).thenReturn(false);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        assertDoesNotThrow(() -> btPaypalServiceImpl.makePayPalAuthCall(authRequest, "AEO_US"));

    }

    @Test
    void getPayPalClientTokenCall() {

        ClientGraphQlResponse successfulResponse = mock(ClientGraphQlResponse.class);

        when(btClientServiceImpl.makeCreateClientTokenCall("AEO_US")).thenReturn(successfulResponse);
        when(successfulResponse.toEntity(AuthorizeResponse.class)).thenReturn(new AuthorizeResponse());

        ResponseEntity<Object> clientToken = btPaypalServiceImpl.getPayPalClientToken("AEO_US");
        assertNotNull(clientToken);

        when(btClientServiceImpl.makeCreateClientTokenCall("AEO_US")).thenReturn(null);

        assertThrows(AuthorizationFailedException.class, () -> btPaypalServiceImpl.getPayPalClientToken("AEO_US"));

    }

    @Test
    void testGetPayPalClientTokenCallFailed() {

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);

        when(btClientServiceImpl.makeCreateClientTokenCall("AEO_US")).thenReturn(errorResponse);
        when(errorResponse.toEntity(ClientTokenResponse.class)).thenReturn(new ClientTokenResponse(new CreateClientToken("", "")));
        when(errorResponse.isValid()).thenReturn(false);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        assertThrows(DuplicateAuthorizationException.class, () -> btPaypalServiceImpl.getPayPalClientToken("AEO_US"));

    }

}
