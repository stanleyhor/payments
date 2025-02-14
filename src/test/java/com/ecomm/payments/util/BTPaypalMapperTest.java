package com.ecomm.payments.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;

import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
class BTPaypalMapperTest {

    private static PaymentsConfig paymentsConfig;

    @Autowired
    BTPaypalMapper btPaypalMapper;

    @BeforeEach
    void setup() {
        paymentsConfig = TestPaymentsConfig.getPaymentConfig();
        btPaypalMapper = new BTPaypalMapper(paymentsConfig);
    }

    @Test
    void prepareAuthRequest() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthRequest(authRequest, "AEO_US"));

        authRequest.setShippingAddress(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthRequest(authRequest, "AEO_US"));

        paymentsConfig.setSendPaypalPayeeEmail(true);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthRequest(authRequest, "AEO_US"));

    }

    @Test
    void prepareAuthResponse() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();
        BTPaypalAuthResponse btPaypalAuthResponse = BTPaypalRequestResponse.authroizationBTPaypalResponse();
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        btPaypalAuthResponse.getAuthorizePayPalAccount()
                .getTransaction()
                .setCustomer(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        btPaypalAuthResponse.getAuthorizePayPalAccount()
                .getTransaction()
                .getPaymentMethodSnapshot()
                .setPayer(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        btPaypalAuthResponse.getAuthorizePayPalAccount()
                .getTransaction()
                .setPaymentMethodSnapshot(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        btPaypalAuthResponse.getAuthorizePayPalAccount()
                .setTransaction(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        btPaypalAuthResponse.getAuthorizePayPalAccount()
                .setBillingAgreementWithPurchasePaymentMethod(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        btPaypalAuthResponse.setAuthorizePayPalAccount(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, null));

    }

    @Test
    void prepareAuthResponseForEmail() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();
        BTPaypalAuthResponse btPaypalAuthResponse = BTPaypalRequestResponse.authroizationBTPaypalResponse();
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

        authRequest.getBillingAddress()
                .setEmail(null);
        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse));

    }

    @Test
    void prepareAuthErrorResponse() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();

        ArrayList<ResponseError> responseErrors = new ArrayList<ResponseError>();
        ResponseError responseError = mock(ResponseError.class);
        responseErrors.add(responseError);

        ClientGraphQlResponse errorResponse = mock(ClientGraphQlResponse.class);
        when(errorResponse.getErrors()).thenReturn(responseErrors);

        assertDoesNotThrow(() -> btPaypalMapper.prepareAuthErrorResponse(authRequest, errorResponse));
    }

    @Test
    void prepareClientTokenRequest() {

        assertDoesNotThrow(() -> btPaypalMapper.prepareCreateClientTokenRequest("AEO_US"));

    }

}
