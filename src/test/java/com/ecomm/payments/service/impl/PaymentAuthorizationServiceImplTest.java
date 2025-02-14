package com.ecomm.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.adyen.Installments;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.util.AdyenResponseUtilTest;
import com.ecomm.payments.util.AdyenTestUtils;
import com.ecomm.payments.util.AuthRequestResponseMapper;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.TransactionalDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class PaymentAuthorizationServiceImplTest {

    @Mock
    private AuthRequestResponseMapper mapper;

    @Mock
    private PaymentAuthorizationServiceImpl paymentAuthorizationServiceImpl;

    @Mock
    private TransactionalDataMapper transactionalDataMapper;

    @Mock
    private AdyenCallServiceImpl adyenAuthServiceImpl;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @BeforeEach
    void initialize() throws AdyenClientException {
        paymentAuthorizationServiceImpl = new PaymentAuthorizationServiceImpl(mapper, transactionalDataMapper, adyenAuthServiceImpl, authorizationUtils);
    }

    @Test
    void getAdyenResponse() throws Exception {
        AdyenAuthResponse adyenMono = adyenAuthServiceImpl.callService(AdyenTestUtils.getAdyenAuthRequest(), "UID-1");
        assertNull(adyenMono);
    }

    @Test
    void getAdyenErrorResponse() throws IOException {
        assertNull(adyenAuthServiceImpl.callService(AdyenTestUtils.getAdyenAuthRequest(), "UID-1"));
    }

    @Test
    void getAdyenResponseTest() throws Exception {
        AdyenAuthResponse adyenMono = adyenAuthServiceImpl.callService(AdyenTestUtils.getAdyenAuthRequest(), null);
        assertNull(adyenMono);
    }

    @Test
    void testAdyenAuthorization() throws Exception {

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAdyenAuthRequest());

        Mockito.when(authorizationUtils.processAndStoreAdyenAuthResponse(any(ATGAuthRequest.class), any(AdyenAuthRequest.class), any(AdyenAuthResponse.class)))
                .thenReturn(AdyenTestUtils.getAuthResponse());

        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new AdyenAuthResponse());

        ATGAuthRequest atgAuthRequest = AdyenTestUtils.getAuthRequestForMX();
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(true);
        Mockito.when(authorizationUtils.checkExistingPaymentHeaderAvailable(any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAuthResponse());
        ATGAuthResponse atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);

        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(false);
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);

        atgAuthRequest.setOrderNumber(null);
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
    }

    @Test
    void testBuildAuthResponseFromHeader() {

        var authRequest = AdyenTestUtils.getAuthRequestForMX();
        authRequest.setReauth(false);
        authRequest.setRetroCharge(false);
        authRequest.setEditAddress(false);
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(false);
        authRequest.setOrderNumber("0011223344");

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAdyenAuthRequest());

        var authResponse = paymentAuthorizationServiceImpl.authorizePayment(authRequest);
        assertNull(authResponse);
    }

    @Test
    void testAdyenPaypalResponse() {

        ATGAuthRequest atgAuthRequest = AdyenTestUtils.getAuthRequestForMX();
        atgAuthRequest.setOrderNumber("1234567890");
        ATGAuthResponse atgMockedResponse = AdyenTestUtils.getAuthResponse();
        atgMockedResponse.setResultCode(PaymentsConstants.AUTHORIZED);
        atgMockedResponse.setMerchantReference(null);
        atgMockedResponse.setAmount(null);
        atgMockedResponse.setAdditionalData(null);

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAdyenAuthRequest());

        Mockito.when(authorizationUtils.processAndStoreAdyenAuthResponse(any(ATGAuthRequest.class), any(AdyenAuthRequest.class), any(AdyenAuthResponse.class)))
                .thenReturn(AdyenTestUtils.getAuthResponse());

        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new AdyenAuthResponse());

        ATGAuthResponse atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);

        atgAuthRequest = AdyenTestUtils.getAdyenPaypalRequest();
        atgMockedResponse.setResultCode(PaymentsConstants.AUTHORIZED);
        atgMockedResponse.setMerchantReference("");

        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);

        atgAuthRequest = AdyenTestUtils.getAuthRequestForMX();
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(true);
        atgAuthRequest.getPaymentMethod()
                .setType(null);
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
        atgAuthRequest.getPaymentMethod()
                .setType("");
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(false);
        atgAuthRequest.getPaymentMethod()
                .setType("payapal");
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(true);
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(true);
        atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
    }

    @Test
    void testCreditCardsInstallments() {
        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAdyenAuthRequest());
        Mockito.when(authorizationUtils.processAndStoreAdyenAuthResponse(any(ATGAuthRequest.class), any(AdyenAuthRequest.class), any(AdyenAuthResponse.class)))
                .thenReturn(AdyenTestUtils.getAuthResponse());
        ATGAuthRequest atgAuthRequest = AdyenTestUtils.getAuthRequestForMX();
        Installments installments = new Installments();
        installments.setValue(6);
        atgAuthRequest.setInstallments(installments);
        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new AdyenAuthResponse());
        ATGAuthResponse atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNotNull(atgAuthResponse);
    }

    @Test
    void testAuthorizeAplazoPayment() {

        var authRequest = AdyenTestUtils.getAuthRequestForMX();
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(true);
        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(mockPaymentHeader());
        Mockito.when(authorizationUtils.findByPaymentReference(any(), any(ATGAuthRequest.class)))
                .thenReturn(mockPaymentEvent());
        var authResponse = paymentAuthorizationServiceImpl.authorizeAplazoPayment(authRequest);
        assertNull(authResponse);

        authRequest.setReauth(false);
        authRequest.setRetroCharge(false);
        authRequest.setEditAddress(false);
        Mockito.when(authorizationUtils.isAuthRetry(any(ATGAuthRequest.class)))
                .thenReturn(false);
        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(mockPaymentHeader());
        var authResponse1 = paymentAuthorizationServiceImpl.authorizeAplazoPayment(authRequest);
        assertNull(authResponse1);

        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(Optional.empty());
        Mockito.when(authorizationUtils.findByPaymentReference(any(), any(ATGAuthRequest.class)))
                .thenReturn(Optional.empty());
        var authResponse2 = paymentAuthorizationServiceImpl.authorizeAplazoPayment(AdyenTestUtils.getAuthRequestForMX());
        assertNull(authResponse2);

    }

    @Test
    void getInvalidAtgResponse() throws Exception {

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAdyenAuthRequest());

        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(null);

        ATGAuthRequest atgAuthRequest = AdyenTestUtils.getInvalidAuthRequest();
        ATGAuthResponse atgAuthResponse = paymentAuthorizationServiceImpl.authorizePayment(atgAuthRequest);
        assertNull(atgAuthResponse);
    }

    @Test
    void testPopulateBillingEmail() {

        var paymentHeaderDTO = new PaymentHeaderDTO();
        paymentHeaderDTO.setBillingAddress(new BillingAddressDTO());

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AdyenAuthRequest());
        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(new AdyenAuthResponse());
        ATGAuthResponse atgMockedResponse = AdyenTestUtils.getAuthResponse();
        atgMockedResponse.setResultCode(PaymentsConstants.AUTHORIZED);

        Mockito.when(authorizationUtils.processAndStoreAdyenAuthResponse(any(ATGAuthRequest.class), any(AdyenAuthRequest.class), any(AdyenAuthResponse.class)))
                .thenReturn(AdyenTestUtils.getAuthResponse());

        atgMockedResponse = paymentAuthorizationServiceImpl.authorizePayment(AdyenTestUtils.getAdyenPaypalRequest());
        assertNotNull(atgMockedResponse);

        var adyenAuthResponse = AdyenResponseUtilTest.getAdyenAuthResponse();
        adyenAuthResponse.getAdditionalData()
                .setFraudResultType(PaymentsConstants.DEFAULT_FRAUD_RESULT_TYPE_GREEN);

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AdyenAuthRequest());
        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(adyenAuthResponse);

        atgMockedResponse = paymentAuthorizationServiceImpl.authorizePayment(AdyenTestUtils.getAdyenPaypalRequest());
        assertNotNull(atgMockedResponse);

        Mockito.when(mapper.convertToAdyenAuthRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AdyenAuthRequest());
        Mockito.when(adyenAuthServiceImpl.callService(any(AdyenAuthRequest.class), anyString()))
                .thenReturn(AdyenResponseUtilTest.getAdyenAuthResponse());

        atgMockedResponse = paymentAuthorizationServiceImpl.authorizePayment(AdyenTestUtils.getAdyenPaypalRequest());
        assertNotNull(atgMockedResponse);
    }

    private Optional<PaymentHeaderDTO> mockPaymentHeader() {
        var paymentHeader = new PaymentHeaderDTO();
        paymentHeader.setOrderNumber("0011223344");

        var paymentEvent = new PaymentEventDTO();
        paymentEvent.setPaymentEventId("UID-1");

        var paymentEvent1 = new PaymentEventDTO();
        paymentEvent1.setPaymentEventId("UID-2");
        paymentEvent1.setPaymentReference("UID-2");

        var paymentEvent2 = new PaymentEventDTO();
        paymentEvent2.setPaymentEventId("UID-1");
        paymentEvent2.setPaymentReference("UID-1");

        paymentHeader.getPaymentEvents()
                .add(paymentEvent);
        paymentHeader.getPaymentEvents()
                .add(paymentEvent1);
        paymentHeader.getPaymentEvents()
                .add(paymentEvent2);
        return Optional.of(paymentHeader);
    }

    private Optional<PaymentEventDTO> mockPaymentEvent() {

        var paymentEvent = new PaymentEventDTO();
        paymentEvent.setPaymentEventId("UID-1");
        paymentEvent.setPaymentReference("UID-2");

        return Optional.of(paymentEvent);
    }

}