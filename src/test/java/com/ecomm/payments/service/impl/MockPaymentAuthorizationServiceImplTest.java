package com.ecomm.payments.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.util.AdyenTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class MockPaymentAuthorizationServiceImplTest {

    private static final String AUTH_TOKEN = "RW5HKBXZTHBH7H65";

    private MockPaymentAuthorizationServiceImpl mockServiceImpl;

    @BeforeEach
    void initialize() throws Exception {
        mockServiceImpl = new MockPaymentAuthorizationServiceImpl(TestPaymentsConfig.getPaymentConfig());
    }

    @Test
    void testAuthorize() throws Exception {
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(getATGAuthRequest());

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
    }

    @Test
    void testAuthorizeWithNullInput() throws Exception {
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(null);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isNotBlank();
    }

    @Test
    void testAuthorizeWithNullAmount() throws Exception {

        ATGAuthRequest request = getATGAuthRequest();
        request.setAmount(null);
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isNotBlank();
    }

    @Test
    void testAuthorizeWithPaypal() throws Exception {

        ATGAuthRequest request = getATGAuthRequest();
        request.getPaymentMethod()
                .setType("paypal");
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);
        assertNotNull(atgAuthResponse.getAdditionalData());
        assertNotNull(atgAuthResponse.getAction());

        request.setBillingAddress(null);
        atgAuthResponse = mockServiceImpl.authorizePayment(request);
        assertNotNull(atgAuthResponse.getBillingAddress());

        request = null;
        atgAuthResponse = MockPaymentAuthorizationServiceImpl.buildPaypalATGAuthResponse(Optional.ofNullable(request));

        assertNotNull(atgAuthResponse);
    }

    @Test
    void testAuthorizeWithOxxo() throws Exception {

        ATGAuthRequest request = getATGAuthRequest();
        request.getPaymentMethod()
                .setType("oxxo");
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);
        assertThat(atgAuthResponse.getAdditionalData()).isNull();
        assertThat(atgAuthResponse.getAction()
                .getDownloadUrl()).isNotBlank();
    }

    @Test
    void testAuthorizeWithOxxoNullAmount() throws Exception {

        ATGAuthRequest request = getATGAuthRequest();
        request.getPaymentMethod()
                .setType("oxxo");
        request.setAmount(null);
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);

        assertThat(atgAuthResponse.getAction()
                .getTotalAmount()).isNotNull();
    }

    @Test
    void testAuthorizeOxxoWithNullInput() throws Exception {
        ATGAuthRequest input = null;
        ATGAuthResponse atgAuthResponse = MockPaymentAuthorizationServiceImpl.buildOxxoATGAuthResponse(Optional.ofNullable(input));
        assertThat(atgAuthResponse.getAction()
                .getTotalAmount()).isNotNull();
    }

    @Test
    void testAuthorizeAplazoPayment() {
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizeAplazoPayment(getATGAuthRequest());

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.DECLINED);
    }

    @Test
    void testAuthorizeVisaCC() {
        ATGAuthRequest request = getATGAuthRequest();
        request.getBillingAddress()
                .setEmail("aeprodcc_visa@ae.com");
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertThat(atgAuthResponse.getAdditionalData()
                .getCreditCardType()).isEqualTo("visa");
    }

    @Test
    void testAuthorizeMastercard() {
        ATGAuthRequest request = getATGAuthRequest();
        request.getBillingAddress()
                .setEmail("aeprodcc_mastercard@g3v8l0qg.mailosaur.net");
        request.setShopperInteraction(PaymentsConstants.ADYEN_SHOPPER_INTERACTION_CONTAUTH);
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertThat(atgAuthResponse.getAdditionalData()
                .getCreditCardType()).isEqualTo("masterCard");
    }

    @Test
    void testAuthorizeAmexCard() {
        ATGAuthRequest request = getATGAuthRequest();
        request.getBillingAddress()
                .setEmail("aeprodcc_amex@g3v8l0qg.mailosaur.net");
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertThat(atgAuthResponse.getAdditionalData()
                .getCreditCardType()).isEqualTo("americanExpress");
    }

    @Test
    void testAuthorizeDiscover() {
        ATGAuthRequest request = getATGAuthRequest();
        request.getBillingAddress()
                .setEmail("aeprodcc_discover@g3v8l0qg.mailosaur.net");
        ATGAuthResponse atgAuthResponse = mockServiceImpl.authorizePayment(request);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertThat(atgAuthResponse.getAdditionalData()
                .getCreditCardType()).isEqualTo("discover");
    }

    @Test
    void testAuthorizeApplePayPayment() {

        ATGAuthRequest request = null;
        ATGAuthResponse atgAuthResponse = mockServiceImpl.buildApplePayAuthResponse(Optional.ofNullable(request));
        assertNull(atgAuthResponse.getResultCode());

        atgAuthResponse = mockServiceImpl.authorizePayment(AdyenTestUtils.getApplePayAuthRequest());

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.AUTHORIZED);

        atgAuthResponse = mockServiceImpl.authorizePayment(AdyenTestUtils.getApplePayInteracAuthRequest());

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.SETTLED);
    }

    private ATGAuthRequest getATGAuthRequest() {

        ATGAuthRequest atgAuthRequest = new ATGAuthRequest();
        atgAuthRequest.setPaymentGroupId("0032433433");
        atgAuthRequest.setAtgProfileId("3242323523532");
        atgAuthRequest.setOrderNumber("3242323523532");
        atgAuthRequest.setIdempotencyKey("UID-1");

        Amount amount = new Amount();
        amount.setValue("1500");
        amount.setCurrency("MXN");
        atgAuthRequest.setAmount(amount);

        AtgPaymentMethod paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType("creditcard");
        paymentMethod.setEncryptedCardNumber("test_4111111111111111");
        paymentMethod.setEncryptedExpiryMonth("test_03");
        paymentMethod.setEncryptedExpiryYear("test_2030");
        paymentMethod.setEncryptedSecurityCode("test_737");
        paymentMethod.setStoredPaymentMethodId(AUTH_TOKEN);
        atgAuthRequest.setPaymentMethod(paymentMethod);

        AtgBillingAddress billingAddress = new AtgBillingAddress();
        billingAddress.setAddress1("22");
        billingAddress.setAddress2("Hot Metal st");
        billingAddress.setFirstName("Test");
        billingAddress.setLastName("User");
        billingAddress.setPhoneNumber("+14005885236");
        billingAddress.setPostalCode("15220");
        billingAddress.setState("PA");
        billingAddress.setCity("Pittsburgh");
        billingAddress.setCountry("US");
        atgAuthRequest.setBillingAddress(billingAddress);

        atgAuthRequest.setStorePaymentMethod(true);
        atgAuthRequest.setShopperInteraction(PaymentsConstants.ADYEN_SHOPPER_INTERACTION_ECOMMERCE);
        atgAuthRequest.setReturnUrl("www.ae.com");

        return atgAuthRequest;
    }

}
