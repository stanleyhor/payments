package com.ecomm.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.afterpay.AfterpayAuthRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.util.AfterpayRepoWrapper;
import com.ecomm.payments.util.AfterpayRequestResponseMapper;
import com.ecomm.payments.util.RequestResponseUtilTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AfterpayServiceImplTest {

    private AfterpayServiceImpl afterpayAuthServiceImpl;

    @Mock
    private AfterpayCallServiceImpl afterpayCallServiceImpl;

    @Mock
    private AfterpayRequestResponseMapper mapper;

    public static ObjectMapper objMapper;

    @Mock
    private AfterpayRepoWrapper repoWrapper;

    @BeforeAll
    static void setUp() throws IOException {
        objMapper = new ObjectMapper();
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @BeforeEach
    void initialize() throws AdyenClientException {
        afterpayAuthServiceImpl = new AfterpayServiceImpl(afterpayCallServiceImpl, mapper, repoWrapper);
    }

    @Test
    void testCheckoutAfterpay() {

        var atgAuthRequest = RequestResponseUtilTest.getAfterpayAuthRequest();

        Mockito.when(mapper.convertToCheckoutRequest(any(ATGAuthRequest.class)))
                .thenReturn(new AfterpayCheckoutRequest());
        Mockito.when(afterpayCallServiceImpl.createCheckout(any(AfterpayCheckoutRequest.class), any()))
                .thenReturn(new ResponseEntity<>(RequestResponseUtilTest.getCheckoutResponseString(), HttpStatus.OK));

        Mockito.when(mapper.buildCheckoutResponse(anyString(), anyString()))
                .thenReturn(new AfterpayCheckoutResponse());

        var response = afterpayAuthServiceImpl.fetchToken(atgAuthRequest);
        assertNotNull(response);
        assertEquals(null, response.getResultCode());

        var checkoutResponse = new Gson().fromJson(RequestResponseUtilTest.getCheckoutResponseString(), AfterpayCheckoutResponse.class);
        Mockito.when(mapper.buildCheckoutResponse(anyString(), anyString()))
                .thenReturn(checkoutResponse);

        response = afterpayAuthServiceImpl.fetchToken(atgAuthRequest);
        assertNotNull(response);
        assertEquals("REDIRECT_AFTERPAY", response.getResultCode());
    }

    @Test
    void testAuthorizePayment() {
        var authorizeRequest = RequestResponseUtilTest.getAuthDetailsRequest();
        var authResponse = RequestResponseUtilTest.getAfterpayAuthResponseString();

        var response = afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US");
        assertNotNull(response);
        assertEquals(422, response.getHttpStatusCode());
        assertEquals("Invalid Order Number", response.getErrorCode());

        Mockito.when(repoWrapper.getPaymentHeaderByOrderNumber(anyString()))
                .thenReturn(getPaymentHeaderDTO());

        Mockito.when(repoWrapper.updateAuthorizeDetails(any(), any()))
                .thenReturn(getPaymentHeaderDTO().get());

        Mockito.when(mapper.convertToAuthorizeRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(new AfterpayAuthRequest());

        Mockito.when(afterpayCallServiceImpl.authorize(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        assertDoesNotThrow(() -> afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US"));

        Mockito.when(repoWrapper.getPaymentHeaderByOrderNumber(anyString()))
                .thenReturn(Optional.of(new PaymentHeaderDTO()));

        PaymentHeaderDTO paymentHeaderDTO = getPaymentHeaderDTO().get();
        paymentHeaderDTO.setPaymentDetails(null);
        Mockito.when(repoWrapper.updateAuthorizeDetails(any(), any()))
                .thenReturn(paymentHeaderDTO);

        Mockito.when(afterpayCallServiceImpl.authorize(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(RequestResponseUtilTest.getAfterpayAuthErrorResponseString(), HttpStatus.OK));

        response = afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US");
        assertNotNull(response);

        Mockito.when(repoWrapper.getPaymentHeaderByOrderNumber(anyString()))
                .thenReturn(getPaymentHeaderDTO());

        Mockito.when(repoWrapper.updateAuthorizeDetails(any(), any()))
                .thenReturn(new PaymentHeaderDTO());

        Mockito.when(afterpayCallServiceImpl.authorize(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        response = afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US");
        assertNotNull(response);

        Mockito.when(repoWrapper.getPaymentHeaderByOrderNumber(anyString()))
                .thenReturn(getPaymentHeaderDTO());

        Mockito.when(repoWrapper.updateAuthorizeDetails(any(), any()))
                .thenReturn(null);

        Mockito.when(afterpayCallServiceImpl.authorize(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        response = afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US");
        assertNotNull(response);

    }

    @Test
    void testReverseAuthorizePayment() {
        var authorizeRequest = RequestResponseUtilTest.getAuthDetailsRequest();
        var authResponse = RequestResponseUtilTest.getAfterpayAuthResponseString();

        Mockito.when(repoWrapper.getPaymentHeaderByOrderNumber(anyString()))
                .thenReturn(getPaymentHeaderDTO());

        Mockito.when(mapper.convertToAuthorizeRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(new AfterpayAuthRequest());

        Mockito.when(afterpayCallServiceImpl.authorize(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(authResponse, HttpStatus.GATEWAY_TIMEOUT));

        Mockito.when(afterpayCallServiceImpl.reverseAuth(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThrows(ClientException.class, () -> afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US"));

        Mockito.when(afterpayCallServiceImpl.reverseAuth(any(AfterpayAuthRequest.class), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThrows(ClientException.class, () -> afterpayAuthServiceImpl.authorizePayment(authorizeRequest, "AEO_US"));
    }

    private Optional<PaymentHeaderDTO> getPaymentHeaderDTO() {

        var paymentHeader = new PaymentHeaderDTO();
        var paymentEvent = new PaymentEventDTO();
        paymentEvent.setTxnState(PaymentsConstants.AUTHORIZED);

        var eventDetails = new EventDetails();
        eventDetails.setAfterpayEventId("123-456-789");
        eventDetails.setAfterpayExpiryDate("2023-12-21T17:23:53.992Z");
        paymentEvent.setEventDetails(new EventDetails());

        paymentHeader.setGatewayIndicator(PaymentsConstants.GATEWAY_AFTERPAY);
        paymentHeader.getPaymentEvents()
                .add(paymentEvent);

        var billingAddress = new BillingAddressDTO();
        billingAddress.setFirstName("John");
        billingAddress.setLastName("Teller");
        paymentHeader.setBillingAddress(billingAddress);

        var paymentDetails = new PaymentDetails();
        paymentDetails.setSourceType("afterPay");
        paymentDetails.setAfterPayToken("002.3aj1fn056r94snl6obr6hbqq86qmqvscd03qirvldvmg32ql");
        paymentDetails.setAfterPayInstallmentAmount(10.99);
        paymentHeader.setPaymentDetails(paymentDetails);

        return Optional.of(paymentHeader);

    }

}
