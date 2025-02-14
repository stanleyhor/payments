package com.ecomm.payments.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;

import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.PaypalAuthResponse;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.model.database.TransactionType;
import com.ecomm.payments.repository.PaymentHeaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BTPaypalRepoWrapperTest {

    @Mock
    private PaymentHeaderRepository headerRepository;

    private BTPaypalRepoWrapper btPaypalRepoWrapper;

    @BeforeEach
    public void setUp() {
        btPaypalRepoWrapper = new BTPaypalRepoWrapper(headerRepository);
    }

    @Test
    void storeTransactionalData() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();
        PaypalAuthResponse authResponse = BTPaypalRequestResponse.authroizationResponse();
        PaymentHeaderDTO paymentHeader = getPaymentHeader();

        Mockito.when(headerRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));
        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));

        Mockito.when(headerRepository.findById(anyString()))
                .thenReturn(Optional.of(paymentHeader));
        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));

        paymentHeader.setPaymentEvents(List.of(getPaymentEvent()));
        Mockito.when(headerRepository.findById(anyString()))
                .thenReturn(Optional.of(paymentHeader));
        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));

    }

    @Test
    void storeTransactionalDataWithNulls() {

        AuthorizationRequest authRequest = BTPaypalRequestResponse.authroizationRequest();
        PaypalAuthResponse authResponse = BTPaypalRequestResponse.authroizationResponse();
        PaymentHeaderDTO paymentHeader = getPaymentHeader();

        authRequest.setAmount(null);
        paymentHeader.setBillingAddress(null);
        Mockito.when(headerRepository.findById(anyString()))
                .thenReturn(Optional.of(paymentHeader));
        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));

        authResponse.setPayer(null);
        Mockito.when(headerRepository.findById(anyString()))
                .thenReturn(Optional.of(paymentHeader));
        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));

        authRequest.setBillingAddress(null);
        authResponse.setBillingAddress(null);
        Mockito.when(headerRepository.findById(anyString()))
                .thenReturn(Optional.of(paymentHeader));
        assertDoesNotThrow(() -> btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, "AEO_US"));

    }

    private PaymentHeaderDTO getPaymentHeader() {

        PaymentHeaderDTO paymentHeader = new PaymentHeaderDTO();
        paymentHeader.setLastModifiedBy("STS");
        paymentHeader.setCurrencyCode("USD");
        paymentHeader.setGatewayIndicator("gateway-indicator");
        paymentHeader.setPaymentDetails(new PaymentDetails());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setOrderNumber("3242323523532");
        paymentHeader.setPaymentHeaderId("ph0000094865");
        paymentHeader.setOrderRef("o3525353453");
        paymentHeader.setSiteId("AEO_US");
        paymentHeader.setState("Authorized");
        paymentHeader.setPaymentMethod("paypal");
        paymentHeader.setBillingAddress(getBillingAddress());

        return paymentHeader;

    }

    private PaymentEventDTO getPaymentEvent() {

        PaymentEventDTO paymentEvent = new PaymentEventDTO();
        paymentEvent.setPaymentEventId("234");
        paymentEvent.setTxnState("AUTHORIZED");
        paymentEvent.setTxnType(TransactionType.AUTH);
        paymentEvent.setAmount(new BigDecimal(10.00));
        paymentEvent.setPaymentReference("8526436739368038");
        paymentEvent.setMerchantAccountName("AE_MX_ECom");
        paymentEvent.setMerchantReference("3242323523532-pg121590693");
        paymentEvent.setErrorCode("200");
        paymentEvent.setErrorMessage("this is a test message");
        paymentEvent.setLastModifiedDate(LocalDateTime.now());
        paymentEvent.setEventDetails(new EventDetails());
        paymentEvent.setLastModifiedBy("default");

        return paymentEvent;

    }

    private BillingAddressDTO getBillingAddress() {

        BillingAddressDTO billingAddress = new BillingAddressDTO();

        billingAddress.setFirstName("Sudheer");
        billingAddress.setLastName("Guduru");
        billingAddress.setAddress1("123 Main Street");
        billingAddress.setAddress2("Apt 123");
        billingAddress.setEmail("test@test.com");
        billingAddress.setCity("Hyderabad");
        billingAddress.setCountry("India");
        billingAddress.setState("Telangana");
        billingAddress.setPostalCode("500072");

        return billingAddress;
    }

}
