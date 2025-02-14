package com.ecomm.payments.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.afterpay.AfterpayAuthResponse;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.model.afterpay.AfterpayOrderDetails;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.repository.PaymentHeaderRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AfterpayRepoWrapperTest {

    private AfterpayRepoWrapper afterpayRepoWrapper;

    @Mock
    private PaymentHeaderRepository headerRepository;

    private PaymentsConfig paymentsConfig;

    @BeforeEach
    public void setUp() {
        paymentsConfig = TestPaymentsConfig.getPaymentConfig();
        afterpayRepoWrapper = new AfterpayRepoWrapper(headerRepository, paymentsConfig);
    }

    @Test
    void testSaveCreateCheckoutDetails() {

        var request = RequestResponseUtilTest.getAfterpayAuthRequest();
        var checkoutResponse = new Gson().fromJson(RequestResponseUtilTest.getCheckoutResponseString(), AfterpayCheckoutResponse.class);
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> afterpayRepoWrapper.saveCreateCheckoutDetails(request, checkoutResponse));

        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(Optional.of(List.of()));
        assertDoesNotThrow(() -> afterpayRepoWrapper.saveCreateCheckoutDetails(request, checkoutResponse));

        var headerDTOListOptional = getPaymentHeaderDTOList();
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);

        assertDoesNotThrow(() -> afterpayRepoWrapper.saveCreateCheckoutDetails(request, checkoutResponse));

        request.setAmount(null);
        request.setCheckoutType(null);
        request.setBillingAddress(null);
        headerDTOListOptional.get()
                .get(0)
                .setBillingAddress(null);
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);

        assertDoesNotThrow(() -> afterpayRepoWrapper.saveCreateCheckoutDetails(request, checkoutResponse));
    }

    @Test
    void testUpdateAuthorizeDetails() {
        var authRequest = RequestResponseUtilTest.getAuthDetailsRequest();
        var authResponse = new Gson().fromJson(RequestResponseUtilTest.getAfterpayAuthResponseString(), AfterpayAuthResponse.class);

        var headerDTOListOptional = getPaymentHeaderDTOList();
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);
        var paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);
        assertNotNull(paymentHeaderDTO.getBillingAddress());
        var billingAddress = paymentHeaderDTO.getBillingAddress();
        assertNotNull(billingAddress.getFirstName());
        assertEquals("Volodymyr", billingAddress.getFirstName());
        assertNotNull(billingAddress.getLastName());
        assertEquals("Test", billingAddress.getLastName());
        assertFalse(paymentHeaderDTO.getPaymentEvents()
                .isEmpty());
        var paymentEvent = paymentHeaderDTO.getPaymentEvents()
                .get(0);
        assertNotNull(paymentEvent);
        assertNotNull(paymentEvent.getEventDetails());
        var eventDetails = paymentEvent.getEventDetails();
        assertNotNull(eventDetails.getAfterpayEventId());
        assertNotNull(eventDetails.getAfterpayExpiryDate());
        assertNotNull(authResponse.getInstallment());
        assertEquals(10.99, authResponse.getInstallment());

        authResponse.getOrderDetails()
                .setBilling(null);
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);
        assertNotNull(paymentHeaderDTO.getBillingAddress());
        billingAddress = paymentHeaderDTO.getBillingAddress();
        assertNotNull(billingAddress.getFirstName());
        assertEquals("Mark", billingAddress.getFirstName());
        assertNotNull(billingAddress.getLastName());
        assertEquals("Raj", billingAddress.getLastName());
    }

    @Test
    void testUpdateAuthorizeDetailsErrorCases() {
        var authRequest = RequestResponseUtilTest.getAuthDetailsRequest();
        var authResponse = new Gson().fromJson(RequestResponseUtilTest.getAfterpayAuthResponseString(), AfterpayAuthResponse.class);

        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(Optional.empty());
        var paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNull(paymentHeaderDTO);

        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(Optional.of(List.of()));
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNull(paymentHeaderDTO);

        authResponse.setEvents(null);
        var headerDTOListOptional = getPaymentHeaderDTOList();
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.setEvents(List.of());
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        headerDTOListOptional.get()
                .get(0)
                .setPaymentEvents(List.of());
        headerDTOListOptional.get()
                .get(0)
                .setPaymentDetails(null);
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

    }

    @Test
    void testGetAddressFromAfterpayResponse() {
        var authRequest = RequestResponseUtilTest.getAuthDetailsRequest();
        var authResponse = new Gson().fromJson(RequestResponseUtilTest.getAfterpayAuthResponseString(), AfterpayAuthResponse.class);
        var headerDTOListOptional = getPaymentHeaderDTOList();
        headerDTOListOptional.get()
                .get(0)
                .getPaymentDetails()
                .setCheckoutType("EXPRESS");
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(getPaymentHeaderDTOList());

        authResponse.getOrderDetails()
                .getBilling()
                .setName(null);
        authResponse.getOrderDetails()
                .getBilling()
                .setPhoneNumber(null);
        var paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getBilling()
                .setName("Test");
        authResponse.getOrderDetails()
                .getConsumer()
                .setPhoneNumber("6564561230");
        authResponse.getOrderDetails()
                .getBilling()
                .setCountryCode("MX");
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getConsumer()
                .setPhoneNumber("16564561230");
        authResponse.getOrderDetails()
                .getBilling()
                .setCountryCode("CA");
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getBilling()
                .setName("Test testing");
        authResponse.getOrderDetails()
                .getConsumer()
                .setPhoneNumber(null);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getBilling()
                .setCountryCode(null);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse = new AfterpayAuthResponse();
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse = new AfterpayAuthResponse();
        authResponse.setOrderDetails(new AfterpayOrderDetails());
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);
    }

    @Test
    void testGetFormatedPhoneNumber() {
        var authRequest = RequestResponseUtilTest.getAuthDetailsRequest();
        var authResponse = new Gson().fromJson(RequestResponseUtilTest.getAfterpayAuthResponseString(), AfterpayAuthResponse.class);
        var headerDTOListOptional = getPaymentHeaderDTOList();
        headerDTOListOptional.get()
                .get(0)
                .getPaymentDetails()
                .setCheckoutType("EXPRESS");
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);

        var paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getShipping()
                .setCountryCode("CA");
        authResponse.getOrderDetails()
                .getShipping()
                .setPhoneNumber(null);
        authResponse.getOrderDetails()
                .getConsumer()
                .setPhoneNumber("16564567678");
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getConsumer()
                .setPhoneNumber(null);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getShipping()
                .setCountryCode("MX");
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);

        authResponse.getOrderDetails()
                .getShipping()
                .setCountryCode(null);
        paymentHeaderDTO = afterpayRepoWrapper.updateAuthorizeDetails(authRequest, authResponse);
        assertNotNull(paymentHeaderDTO);
    }

    @Test
    void testUpdateVoidDetails() {

        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(Optional.empty());
        var paymentHeaderDTO = afterpayRepoWrapper.updateVoidDetails("0003240013");
        assertNull(paymentHeaderDTO);

        var headerDTOListOptional = getPaymentHeaderDTOList();
        Mockito.when(headerRepository.findByOrderNumber(anyString()))
                .thenReturn(headerDTOListOptional);
        paymentHeaderDTO = afterpayRepoWrapper.updateVoidDetails("0003240013");
        assertNotNull(paymentHeaderDTO);
        assertEquals(PaymentsConstants.CANCELLED, paymentHeaderDTO.getState());
        assertNotNull(paymentHeaderDTO.getPaymentEvents());
        assertTrue(!paymentHeaderDTO.getPaymentEvents()
                .isEmpty());
        assertNotNull(paymentHeaderDTO.getPaymentEvents()
                .get(0));
        assertEquals(PaymentsConstants.CANCELLED, paymentHeaderDTO.getPaymentEvents()
                .get(0)
                .getTxnState());

        headerDTOListOptional.get()
                .get(0)
                .setPaymentEvents(List.of());
        paymentHeaderDTO = afterpayRepoWrapper.updateVoidDetails("0003240013");
        assertNotNull(paymentHeaderDTO);
        assertEquals(PaymentsConstants.CANCELLED, paymentHeaderDTO.getState());
        assertNotNull(paymentHeaderDTO.getPaymentEvents());
        assertTrue(paymentHeaderDTO.getPaymentEvents()
                .isEmpty());

        headerDTOListOptional.get()
                .get(0)
                .setPaymentEvents(null);
        paymentHeaderDTO = afterpayRepoWrapper.updateVoidDetails("0003240013");
        assertNotNull(paymentHeaderDTO);
        assertEquals(PaymentsConstants.CANCELLED, paymentHeaderDTO.getState());
        assertNull(paymentHeaderDTO.getPaymentEvents());
    }

    private Optional<List<PaymentHeaderDTO>> getPaymentHeaderDTOList() {
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

        return Optional.of(List.of(paymentHeader));
    }

}
