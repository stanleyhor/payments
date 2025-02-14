package com.ecomm.payments.service.impl.v2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.service.v2.AdyenV2CallService;
import com.ecomm.payments.util.AdyenTestUtils;
import com.ecomm.payments.util.AuthRequestResponseMapper;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.TransactionalDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class PaymentAuthorizationV2ServiceImplTest {

    @Mock
    private AuthRequestResponseMapper requestResponseMapper;

    @InjectMocks
    private PaymentAuthorizationV2ServiceImpl paymentAuthorizationV2ServiceImpl;

    @Mock
    private TransactionalDataMapper transactionalDataMapper;

    @Mock
    private AdyenV2CallService adyenV2CallServiceImpl;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @BeforeEach
    void initialize() {
        MockitoAnnotations.openMocks(this);
        paymentAuthorizationV2ServiceImpl = new PaymentAuthorizationV2ServiceImpl(requestResponseMapper, adyenV2CallServiceImpl, authorizationUtils);
    }

    @Test
    void testAuthorizePayment() {

        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(false);
        var atgAuthRequest = AdyenTestUtils.getAuthRequest();
        var atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(atgAuthRequest);
        assertNull(atgAuthResponse);

        Mockito.when(requestResponseMapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AdyenAuthRequest());

        Mockito.when(adyenV2CallServiceImpl.authorize(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(null);

        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(atgAuthRequest);
        assertNull(atgAuthResponse);

        atgAuthRequest.setPaymentMethod(null);
        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(atgAuthRequest);
        assertNull(atgAuthResponse);

        Mockito.when(adyenV2CallServiceImpl.authorize(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(atgAuthRequest);
        assertNull(atgAuthResponse);

        Mockito.when(authorizationUtils.checkExistingPaymentHeaderAvailable(any(ATGAuthRequest.class)))
                .thenReturn(new ATGAuthResponse());
        Mockito.when(authorizationUtils.processAndStoreAdyenAuthResponse(any(ATGAuthRequest.class), any(AdyenAuthRequest.class), any(AdyenAuthResponse.class)))
                .thenReturn(new ATGAuthResponse());
        Mockito.when(adyenV2CallServiceImpl.authorize(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new ResponseEntity<AdyenAuthResponse>(new AdyenAuthResponse(), HttpStatus.OK));

        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);

        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(true);
        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);

        var paypalAuthRequest = AdyenTestUtils.getAdyenPaypalRequest();
        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(paypalAuthRequest);
        assertNotNull(atgAuthResponse);

        paypalAuthRequest.setReauth(true);
        atgAuthResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(paypalAuthRequest);
        assertNotNull(atgAuthResponse);
    }

    @Test
    void testApplePayInteracAuthResponse() {
        Mockito.when(requestResponseMapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AdyenAuthRequest());
        Mockito.when(adyenV2CallServiceImpl.authorize(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new ResponseEntity<AdyenAuthResponse>(AdyenTestUtils.getAdyenApplePayInteracAuthResponse(), HttpStatus.OK));
        Mockito.when(authorizationUtils.processAndStoreAdyenAuthResponse(any(ATGAuthRequest.class), any(AdyenAuthRequest.class), any(AdyenAuthResponse.class)))
                .thenReturn(AdyenTestUtils.getApplePayInteracAuthResponse());

        var authResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(AdyenTestUtils.getApplePayInteracAuthRequest());
        assertNotNull(authResponse);
    }

    @Test
    void testInvalidAdyenAuthResponse() {

        Mockito.when(requestResponseMapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AdyenAuthRequest());

        Mockito.when(adyenV2CallServiceImpl.authorize(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(null);

        var authResponse = paymentAuthorizationV2ServiceImpl.authorizePayment(AdyenTestUtils.getInvalidAuthRequest());
        assertNull(authResponse);
    }

    @Test
    void testAuthorizeAplazoPayment() {
        var authResponse = paymentAuthorizationV2ServiceImpl.authorizeAplazoPayment(new ATGAuthRequest());
        assertNull(authResponse);
    }

}
