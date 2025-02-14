package com.ecomm.payments.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.exception.DuplicateAuthorizationException;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.model.database.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

public class AuthorizationUtilsTest {

    private AuthorizationUtils authorizationUtils;

    @Mock
    private AuthRequestResponseMapper requestResponseMapper;

    @Mock
    private TransactionalDataMapper transactionalDataMapper;

    private PaymentsConfig paymentsConfig;

    @Mock
    private PaymentDetailsRequestResponseMapper authDetailsMapper;

    @BeforeEach
    void initialize() {
        MockitoAnnotations.openMocks(this);
        paymentsConfig = TestPaymentsConfig.getPaymentConfig();
        authorizationUtils = new AuthorizationUtils(requestResponseMapper, transactionalDataMapper, paymentsConfig, authDetailsMapper);
    }

    @Test
    void testDuplicateAuthorizationException() {

        var authRequest = AdyenTestUtils.getAuthRequest();

        authRequest.setOrderNumber("0011223345");
        authRequest.setIdempotencyKey("UID-1");
        var response = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);
        assertNull(response);

        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(mockPaymentHeader());
        assertThrows(DuplicateAuthorizationException.class, () -> authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest));

        Mockito.when(requestResponseMapper.buildAuthResponseFromHeader(any(), any(PaymentEventDTO.class)))
                .thenReturn(new ATGAuthResponse());
        authRequest.setOrderNumber("0011223344");
        response = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);
        assertNotNull(response);

        authRequest.setIdempotencyKey("123");
        response = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);
        assertNull(response);

        var paymentHeader2 = mockPaymentHeader();
        var paymentEvent1 = new PaymentEventDTO();
        var paymentEvents1 = paymentHeader2.get()
                .getPaymentEvents();
        paymentEvent1.setPaymentEventId("UUID");
        paymentHeader2.get()
                .setPaymentMethod("giftcard");
        paymentEvent1.setTxnType(TransactionType.AUTH);
        paymentEvent1.setTxnState("REDIRECT_SHOPPER");
        paymentEvents1.add(paymentEvent1);
        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(paymentHeader2);
        response = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);
        assertNull(response);

        var paymentHeader1 = mockPaymentHeader();
        var paymentEvents = paymentHeader1.get()
                .getPaymentEvents();
        var paymentEvent = new PaymentEventDTO();
        var paymentEvent2 = new PaymentEventDTO();
        paymentEvent.setPaymentEventId("UUID");
        paymentHeader1.get()
                .setPaymentMethod("giftcard");
        paymentEvent.setTxnType(TransactionType.VOID);
        paymentEvent2.setTxnType(TransactionType.AUTH);
        paymentEvents.add(paymentEvent);
        paymentEvents.add(paymentEvent2);
        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(paymentHeader1);
        response = authorizationUtils.checkExistingPaymentHeaderAvailable(authRequest);
        assertNull(response);

        Mockito.when(transactionalDataMapper.getPaymentHeader(any(String.class)))
                .thenReturn(Optional.of(new PaymentHeaderDTO()));
    }

    @Test
    void testAuthRetryFlag() {
        var authRequest = AdyenTestUtils.getAuthRequest();
        assertTrue(authorizationUtils.isAuthRetry(authRequest));
        authRequest.setReauth(false);
        authRequest.setRetroCharge(false);
        authRequest.setEditAddress(false);
        assertFalse(authorizationUtils.isAuthRetry(authRequest));
        authRequest.setReauth(true);
        assertTrue(authorizationUtils.isAuthRetry(authRequest));
        authRequest.setReauth(false);
        authRequest.setRetroCharge(true);
        assertTrue(authorizationUtils.isAuthRetry(authRequest));
        authRequest.setReauth(false);
        authRequest.setRetroCharge(false);
        authRequest.setEditAddress(true);
        assertTrue(authorizationUtils.isAuthRetry(authRequest));
    }

    @Test
    void testFindByPaymentReference() {
        var authRequest = AdyenTestUtils.getAuthRequest();
        var paymentEventDTO = authorizationUtils.findByPaymentReference(Optional.empty(), authRequest);
        assertFalse(paymentEventDTO.isPresent());
        paymentEventDTO = authorizationUtils.findByPaymentReference(mockPaymentHeader(), authRequest);
        assertFalse(paymentEventDTO.isPresent());
        authRequest.setIdempotencyKey("UID-1");
        paymentEventDTO = authorizationUtils.findByPaymentReference(mockPaymentHeader(), authRequest);
        assertTrue(paymentEventDTO.isPresent());
    }

    @Test
    void testProcessAndStoreAdyenAuthResponse() {
        var authRequest = AdyenTestUtils.getAuthRequest();
        var adyenAuthRequest = AdyenTestUtils.getAdyenAuthRequest();
        var adyenAuthResponse = AdyenResponseUtilTest.getAdyenAuthResponse();

        var authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNull(authResponse);

        Mockito.when(requestResponseMapper.convertToATGAuthResponse(any(AdyenAuthResponse.class), any(ATGAuthRequest.class)))
                .thenReturn(AdyenTestUtils.getAuthResponse());

        authRequest.setReauth(false);
        authRequest.setEditAddress(true);
        authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNotNull(authResponse);

        authRequest.setEditAddress(false);
        authRequest.setReauth(true);
        authRequest.getPaymentMethod()
                .setType("paypal");
        authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNotNull(authResponse);

        authRequest.setEditAddress(false);
        authRequest.setReauth(true);
        authRequest.getPaymentMethod()
                .setType("payPal");
        authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNotNull(authResponse);

        authRequest.getPaymentMethod()
                .setType("creditCard");
        authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNotNull(authResponse);

        authResponse = AdyenTestUtils.getAuthResponse();
        authResponse.setResultCode("Test");
        Mockito.when(requestResponseMapper.convertToATGAuthResponse(any(AdyenAuthResponse.class), any(ATGAuthRequest.class)))
                .thenReturn(authResponse);

        authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNotNull(authResponse);

        authRequest.setOrderNumber(null);
        authResponse = authorizationUtils.processAndStoreAdyenAuthResponse(authRequest, adyenAuthRequest, adyenAuthResponse);
        assertNotNull(authResponse);
    }

    @Test
    void testProcessAndStoreAdyenAuthDetailsResponse() {
        var paymentDetailsRequest = new PaymentDetailsRequest();
        var adyenDetailsResponse = AdyenTestUtils.getAdyenDetailsResponse();
        var detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNull(detailsResponse);

        var paymentDetailsResponse = AdyenTestUtils.getPaymentDetailsResponse();
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);

        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        Mockito.when(transactionalDataMapper.updateTransactionalData(any(PaymentDetailsResponse.class)))
                .thenReturn(mockPaymentHeader());
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);
    }

    @Test
    void testProcessAndStoreAdyenAuthInvalidDetailsResponse() {
        var paymentDetailsRequest = new PaymentDetailsRequest();
        var adyenDetailsResponse = new AdyenDetailsResponse();

        var paymentDetailsResponse = AdyenTestUtils.getPaymentDetailsResponse();
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);
        Mockito.when(transactionalDataMapper.updateTransactionalData(any(PaymentDetailsResponse.class)))
                .thenReturn(Optional.empty());
        var detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        Mockito.when(transactionalDataMapper.updateTransactionalData(any(PaymentDetailsResponse.class)))
                .thenReturn(Optional.empty());
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        var paymentHeaderSTO = mockPaymentHeader();
        paymentHeaderSTO.get()
                .setBillingAddress(null);

        Mockito.when(transactionalDataMapper.updateTransactionalData(any(PaymentDetailsResponse.class)))
                .thenReturn(paymentHeaderSTO);

        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        Mockito.when(transactionalDataMapper.updateTransactionalData(any(PaymentDetailsResponse.class)))
                .thenReturn(mockPaymentHeader());

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setBillingAddress(null);
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .set(0, null);
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .setPaymentStatus(null);
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        paymentDetailsResponse.getData()
                .setOrderPaymentDetails(null);
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

        paymentDetailsResponse.setData(null);
        Mockito.when(authDetailsMapper.convertDetailsResponse(any(AdyenDetailsResponse.class), any(PaymentDetailsRequest.class)))
                .thenReturn(paymentDetailsResponse);
        detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(paymentDetailsRequest, adyenDetailsResponse);
        assertNotNull(detailsResponse);

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

}
