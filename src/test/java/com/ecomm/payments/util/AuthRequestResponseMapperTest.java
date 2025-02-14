package com.ecomm.payments.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.BrowserInfo;
import com.ecomm.payments.model.adyen.Action;
import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.FraudResult;
import com.ecomm.payments.model.adyen.SdkData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.atg.CommerceItem;
import com.ecomm.payments.model.atg.ContactInfo;
import com.ecomm.payments.model.atg.FraudDetail;
import com.ecomm.payments.model.atg.RequestContext;
import com.ecomm.payments.model.atg.ShippingAddress;
import com.ecomm.payments.model.atg.ShippingDetail;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthRequestResponseMapperTest {

    public static final String APPLE_PAY = "applepay";
    public static final String DISCOVER_APPLEPAY = "discover_applepay";
    private static AuthRequestResponseMapper mapper;
    private static AdditionalDetailsMapper additionalDetailsMapper;
    private ATGAuthRequest atgAuthRequest;
    private AdyenAuthResponse adyenAuthResponse;
    private HttpHeaders headers;
    private final String MERCHANT_ACCOUNT = "AE_MX_Ecom";
    private static final String AUTH_TOKEN = "8315901675746062";

    @BeforeAll
    static void initSetUp() {
        additionalDetailsMapper = new AdditionalDetailsMapper();
        mapper = new AuthRequestResponseMapper(TestPaymentsConfig.getPaymentConfig(), additionalDetailsMapper,
                new CommonUtils(TestPaymentsConfig.getPaymentConfig()));
    }

    @BeforeEach
    public void setUp() {
        atgAuthRequest = new ATGAuthRequest();
        atgAuthRequest.setPaymentGroupId("0032433433");
        atgAuthRequest.setAtgProfileId("3242323523532");
        atgAuthRequest.setOrderNumber("3242323523532");
        atgAuthRequest.setWebStoreId("09021");
        atgAuthRequest.setIdempotencyKey("UID-1");
        atgAuthRequest.setExecuteThreeD("true");

        Amount amount = new Amount();
        amount.setValue("1500");
        amount.setCurrency("MXN");
        atgAuthRequest.setAmount(amount);

        RequestContext context = new RequestContext();
        context.setChannelType("WEB");
        context.setEventType("AUTHORIZE");
        context.setGateway("ADYEN");
        context.setSource("ATG");
        atgAuthRequest.setRequestContext(context);

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

        FraudDetail adyenFraudDetail = new FraudDetail();

        adyenFraudDetail.setDeviceFingerPrint("blahblah");
        adyenFraudDetail.setOrderDiscountAmount("10.00");
        adyenFraudDetail.setCommerceItemCount("1");
        adyenFraudDetail.setCouponCode("GETREADY|EMPLOYEE");

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail("user@ae.com");
        contactInfo.setPhoneNo("+14124320820");
        adyenFraudDetail.setContactInfo(contactInfo);

        ShippingDetail shippingDetail = new ShippingDetail();
        shippingDetail.setShippingEmail("seconduser@ae.com");
        shippingDetail.setShippingMethod("STD");

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setAddress1("Kukulcan Km 14.5");
        shippingAddress.setAddress2("Zona Hotelera");
        shippingAddress.setCity("Cancun");
        shippingAddress.setCountry("MX");
        shippingAddress.setFirstName("User");
        shippingAddress.setLastName("Test");
        shippingAddress.setPostalCode("77500");
        shippingAddress.setState("ROO");
        shippingDetail.setShippingAddress(shippingAddress);
        adyenFraudDetail.setShippingDetail(shippingDetail);

        List<CommerceItem> commerceItems = new ArrayList<>();
        CommerceItem commerceItem = new CommerceItem();
        commerceItem.setCommerceItemId("ci324324325324");
        commerceItem.setDisplayName("Real Me Full");
        commerceItem.setProductUPC("032-0343");
        commerceItem.setQuantity("1");
        commerceItem.setSalePrice("39.95");
        commerceItem.setSku("0026516659");
        commerceItems.add(commerceItem);
        adyenFraudDetail.setCommerceItems(commerceItems);

        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);

        adyenAuthResponse = new AdyenAuthResponse();

        AdyenAdditionalData additionalData = new AdyenAdditionalData();
        additionalData.setAuthCode("012097");
        additionalData.setAvsResult("4 AVS not supported for this card type");
        additionalData.setAvsResultRaw("2");
        additionalData.setCardBin("411111");
        additionalData.setCardIssuingBank("ADYEN TEST BANK");
        additionalData.setCardIssuingCountry("NL");
        additionalData.setCardPaymentMethod("visa");
        additionalData.setCardSummary("1111");
        additionalData.setCvcResult("1 Matches");
        additionalData.setExpiryDate("03/2021");
        additionalData.setPaymentMethod("visa");
        additionalData.setRecurringDetailReference(AUTH_TOKEN);
        additionalData.setRecurringProcessingModel("CardOnFile");
        additionalData.setShopperReference("3242323523532");
        additionalData.setFundingSource("CREDIT");
        adyenAuthResponse.setAdditionalData(additionalData);

        adyenAuthResponse.setAmount(amount);

        FraudResult fraudResult = new FraudResult();
        fraudResult.setAccountScore(50);
        adyenAuthResponse.setFraudResult(fraudResult);

        adyenAuthResponse.setMerchantReference("3242323523532");
        adyenAuthResponse.setPspReference("852597229203094H");
        adyenAuthResponse.setResultCode("AUTHORIZED");

        headers = new HttpHeaders();
        headers.add(PaymentsConstants.SITE_ID, "AEO_MX");
    }

    @Test
    void getMerchantAccount() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getMerchantAccount()).isEqualTo(MERCHANT_ACCOUNT);
    }

    @Test
    void isStorePaymentMethod() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertEquals(PaymentsConstants.ADYEN_SHOPPER_INTERACTION_ECOMMERCE, convertedRequest.getShopperInteraction());

        assertTrue(convertedRequest.isStorePaymentMethod());

        atgAuthRequest.setShopperInteraction(PaymentsConstants.ADYEN_SHOPPER_INTERACTION_CONTAUTH);
        convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertEquals(PaymentsConstants.ADYEN_SHOPPER_INTERACTION_CONTAUTH, convertedRequest.getShopperInteraction());

        assertFalse(convertedRequest.isStorePaymentMethod());
    }

    @Test
    void getPaymentGroupIdnonNull() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getReference()).isEqualTo("3242323523532-0032433433");
    }

    @Test
    void getPaymentGroupIdNull() {
        atgAuthRequest.setPaymentGroupId(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getReference()).startsWith("3242323523532");

        assertThat(convertedRequest.getReference()
                .length()).isEqualByComparingTo(25);
    }

    @Test
    void getAdditionalDatat() {
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertThat(atgAuthResponse.getAdditionalData()
                .getPaymentMethod()).isEqualTo(atgAuthRequest.getPaymentMethod()
                        .getType());
    }

    @Test
    void checkAdyenAuthRequest() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(null);

        assertNull(convertedRequest);
    }

    @Test
    void checkAdyenAuthRequestPaymentMethod() {
        atgAuthRequest.setPaymentMethod(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getPaymentMethod());
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodOxxo() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        atgAuthRequest.setReturnUrl(PaymentsConstants.OXXO_PAYMENT_RETURN_URL);

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("oxxo");
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodOxxowithRetunUrlAsNull() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        atgAuthRequest.setReturnUrl(null);

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("oxxo");
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodOxxoWithShopperLocaleAsNull() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        atgAuthRequest.setShopperLocale(null);

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("oxxo");
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodOxxoWithShopperLocale() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        atgAuthRequest.setShopperLocale("en_US");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("oxxo");
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodOxxoWithShippingDetailAsNull() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        atgAuthRequest.setShopperLocale("en_US");

        FraudDetail adyenFraudDetail = new FraudDetail();

        adyenFraudDetail.setDeviceFingerPrint("blahblah");
        adyenFraudDetail.setOrderDiscountAmount("10.00");
        adyenFraudDetail.setCommerceItemCount("1");
        adyenFraudDetail.setCouponCode("GETREADY|EMPLOYEE");

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail("user@ae.com");
        contactInfo.setPhoneNo("+14124320820");
        adyenFraudDetail.setContactInfo(contactInfo);

        adyenFraudDetail.setShippingDetail(null);
        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("oxxo");
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodOxxoWithShippingAddressAsNull() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        atgAuthRequest.setShopperLocale("en_US");

        FraudDetail adyenFraudDetail = new FraudDetail();

        adyenFraudDetail.setDeviceFingerPrint("blahblah");
        adyenFraudDetail.setOrderDiscountAmount("10.00");
        adyenFraudDetail.setCommerceItemCount("1");
        adyenFraudDetail.setCouponCode("GETREADY|EMPLOYEE");

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail("user@ae.com");
        contactInfo.setPhoneNo("+14124320820");
        adyenFraudDetail.setContactInfo(contactInfo);

        ShippingDetail shippingDetail = new ShippingDetail();
        shippingDetail.setShippingEmail("seconduser@ae.com");
        shippingDetail.setShippingMethod("STD");

        shippingDetail.setShippingAddress(null);
        adyenFraudDetail.setShippingDetail(shippingDetail);
        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("oxxo");
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodGiftCard() {
        atgAuthRequest.getPaymentMethod()
                .setType("giftCard");
        atgAuthRequest.getPaymentMethod()
                .setGiftCardNumber("1234567890123");
        atgAuthRequest.getPaymentMethod()
                .setGiftCardPIN("12345");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("svs");

        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth());
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodGiftCardNumber() {
        atgAuthRequest.getPaymentMethod()
                .setType("giftCard");
        atgAuthRequest.getPaymentMethod()
                .setGiftCardNumber("1234567890123");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("svs");
        assertThat(convertedRequest.getPaymentMethod()
                .getNumber()).isEqualTo("1234567890123");

        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth());
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodGiftCardPIN() {
        atgAuthRequest.getPaymentMethod()
                .setType("giftCard");
        atgAuthRequest.getPaymentMethod()
                .setGiftCardNumber("1234567890123");
        atgAuthRequest.getPaymentMethod()
                .setGiftCardPIN("12345");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("svs");
        assertThat(convertedRequest.getPaymentMethod()
                .getCvc()).isEqualTo("12345");
        assertThat(convertedRequest.getPaymentMethod()
                .getNumber()).isEqualTo("1234567890123");

        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth());
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodHolderFirstName() {
        atgAuthRequest.getBillingAddress()
                .setFirstName(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getPaymentMethod()
                .getHolderName());
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodHolderLastName() {
        atgAuthRequest.getBillingAddress()
                .setLastName(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getHolderName()).isEqualTo("Test");
    }

    @Test
    void checkAdyenAuthRequestRecurringProcessingModelCardOnFile() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getRecurringProcessingModel()).isEqualTo(PaymentsConstants.CARD_ON_FILE);
    }

    @ParameterizedTest
    @CsvSource(value =
    { "paypal", "applepay" })
    void testCardOnFileForReauth(String paymentMethod) {
        atgAuthRequest.getPaymentMethod()
                .setType(paymentMethod);
        atgAuthRequest.setReauth(true);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getRecurringProcessingModel()).isEqualTo(PaymentsConstants.UNSCHEDULED_CARD_ON_FILE);
    }

    @ParameterizedTest
    @CsvSource(value =
    { "paypal", "applepay" })
    void testCardOnFileForRetrocharge(String paymentMethod) {
        atgAuthRequest.getPaymentMethod()
                .setType(paymentMethod);
        atgAuthRequest.setRetroCharge(true);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getRecurringProcessingModel()).isEqualTo(PaymentsConstants.UNSCHEDULED_CARD_ON_FILE);
    }

    @Test
    void checkAdyenAuthRequestecurringProcessingModel() {
        atgAuthRequest.getPaymentMethod()
                .setType("oxxo");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getRecurringProcessingModel());
    }

    @Test
    void checkAdyenAuthRequestBillingAddress() {
        atgAuthRequest.setBillingAddress(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getBillingAddress());
    }

    @Test
    void checkATGAuthResponse() {
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(null, null);

        assertNull(atgAuthResponse);
    }

    @Test
    void checkATGAuthResponseAdditionalDetail() {
        adyenAuthResponse.setAdditionalData(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData());
    }

    @Test
    void checkATGAuthResponseFraudResult() {
        adyenAuthResponse.setFraudResult(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getFraudResult());
    }

    @Test
    void checkATGAuthResponseExpirationMonthAndYear() {
        adyenAuthResponse.getAdditionalData()
                .setExpiryDate("03");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData()
                .getExpirationMonth());

        assertNull(atgAuthResponse.getAdditionalData()
                .getExpirationYear());
    }

    @Test
    void checkATGAuthResponseExpirationSingleMonthDoubleDigit() {
        adyenAuthResponse.getAdditionalData()
                .setExpiryDate("3/2020");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getAdditionalData()
                .getExpirationMonth()).isEqualTo("03");

    }

    @Test
    void checkATGAuthResponseExpirationDoubleMonthDoubleDigit() {
        adyenAuthResponse.getAdditionalData()
                .setExpiryDate("09/2020");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getAdditionalData()
                .getExpirationMonth()).isEqualTo("09");

    }

    @Test
    void checkATGAuthResponseExpirationMonthDoubleDigit() {
        adyenAuthResponse.getAdditionalData()
                .setExpiryDate("11/2020");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getAdditionalData()
                .getExpirationMonth()).isEqualTo("11");

    }

    @Test
    void nullCheckATGAuthResponseExpirationMonthAndYear() {
        adyenAuthResponse.getAdditionalData()
                .setExpiryDate(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData()
                .getExpirationMonth());

        assertNull(atgAuthResponse.getAdditionalData()
                .getExpirationYear());
    }

    @Test
    void checkATGAuthResponseMaskedCardNumber() {
        adyenAuthResponse.getAdditionalData()
                .setCardPaymentMethod("AMEX");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertEquals(15, atgAuthResponse.getAdditionalData()
                .getMaskedCardNumber()
                .length());
    }

    @Test
    void nullCheckATGAuthResponseMaskedCardNumber() {
        adyenAuthResponse.getAdditionalData()
                .setCardPaymentMethod(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertEquals(16, atgAuthResponse.getAdditionalData()
                .getMaskedCardNumber()
                .length());
    }

    @Test
    void checkATGAuthResponseMaskedCardNumberWithoutCardBin() {
        adyenAuthResponse.getAdditionalData()
                .setCardPaymentMethod("AMEX");
        adyenAuthResponse.getAdditionalData()
                .setCardBin(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertEquals(15, atgAuthResponse.getAdditionalData()
                .getMaskedCardNumber()
                .length());
    }

    @Test
    void nullCheckATGAuthResponseMaskedCardNumberWithoutCardBin() {
        adyenAuthResponse.getAdditionalData()
                .setCardPaymentMethod(null);
        adyenAuthResponse.getAdditionalData()
                .setCardBin(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertEquals(16, atgAuthResponse.getAdditionalData()
                .getMaskedCardNumber()
                .length());
    }

    @Test
    void checkATGAuthResponseAvsResult() {
        adyenAuthResponse.getAdditionalData()
                .setAvsResult(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData()
                .getAvsResult());
    }

    @Test
    void checkATGAuthResponseCvcResult() {
        adyenAuthResponse.getAdditionalData()
                .setCvcResult(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData()
                .getCvvResponseCode());
    }

    @Test
    void checkATGAuthResponseEmptyAvsResult() {
        adyenAuthResponse.getAdditionalData()
                .setAvsResult("");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData()
                .getAvsResult());
    }

    @Test
    void checkATGAuthResponseEmptyCvcResult() {
        adyenAuthResponse.getAdditionalData()
                .setCvcResult("");
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData()
                .getCvvResponseCode());
    }

    @Test
    void checkNullAdyenFraudDetail() {
        atgAuthRequest.setAdyenFraudDetail(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getShopperEmail());
    }

    @Test
    void getAdditionalDataNullCheckContactInfo() {
        atgAuthRequest.getAdyenFraudDetail()
                .setContactInfo(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getShopperEmail());
    }

    @Test
    void getAdditionalDataNullCheckShippingDetail() {
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingEmail(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getMerchantRiskIndicator());

        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingEmail("");
        convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getMerchantRiskIndicator());

        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingEmail(" ");
        convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getMerchantRiskIndicator());

        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(null);
        convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getMerchantRiskIndicator());
    }

    @Test
    void getAdditionalDataNonNullCheckShopperName() {
        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getShopperName()
                .getFirstName());
    }

    @Test
    void getAdditionalDataNullCheckShopperName() {
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getShopperName()
                .getFirstName());
    }

    @Test
    void getMetaDataCheck() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertEquals("09021", convertedRequest.getMetadata()
                .get(PaymentsConstants.WEB_STORE_ID));
    }

    @Test
    void getMetaDataNullCheck() {
        atgAuthRequest.setWebStoreId(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertNull(convertedRequest.getMetadata()
                .get(PaymentsConstants.WEB_STORE_ID));
    }

    @ParameterizedTest
    @CsvSource(value =
    { "02954", "01790", "02953", "09021" })
    void getWebStoreCheckCombined(String webStoreId) {
        atgAuthRequest.setWebStoreId(webStoreId);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        switch (webStoreId) {
            case "02954" -> assertThat(convertedRequest.getMerchantAccount()).isEqualTo("AE_INTL_Ecom");
            case "01790" -> assertThat(convertedRequest.getMerchantAccount()).isEqualTo("AE_CA_Ecom");
            case "02953" -> assertThat(convertedRequest.getMerchantAccount()).isEqualTo("AE_US_Ecom");
            case "09021" -> assertThat(convertedRequest.getMerchantAccount()).isEqualTo("AE_MX_Ecom");
            default -> assertThat(convertedRequest.getMerchantAccount()).isEqualTo("AE_MX_Ecom");
        }
    }

    @ParameterizedTest
    @CsvSource(value =
    { "dummy", "applepay" })
    void testApplePayTokenForPaymentMethodApplePay(String type) {
        atgAuthRequest.getPaymentMethod()
                .setType(type);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        switch (type) {
            case "dummy" -> assertThat(convertedRequest.getPaymentMethod()
                    .getType()).isNotEqualTo(APPLE_PAY);
            case "applepay" -> assertThat(convertedRequest.getPaymentMethod()
                    .getType()).isEqualTo(APPLE_PAY);
            default -> assertThat(convertedRequest.getPaymentMethod()
                    .getType()).isNotEqualTo(APPLE_PAY);
        }
        assertThat(convertedRequest.getPaymentMethod()
                .getApplePayToken()).isNull();
    }

    @Test
    void checkApplePayTokenForPaymentMethodApplePay() {
        atgAuthRequest.getPaymentMethod()
                .setType(APPLE_PAY);
        atgAuthRequest.getPaymentMethod()
                .setApplePayToken("QWIwMmI0YzAhQlFBQkFnQjMv..");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertThat(convertedRequest.getPaymentMethod()
                .getApplePayToken()).isEqualTo("QWIwMmI0YzAhQlFBQkFnQjMv..");
    }

    @Test
    void getOrderNumberNonNull() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getReference()).isEqualTo("3242323523532-0032433433");
    }

    @Test
    void getOrderNumberNull() {
        atgAuthRequest.setOrderNumber(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getReference()).isEqualTo("0032433433");

        assertThat(convertedRequest.getReference()
                .length()).isEqualByComparingTo(10);
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodCreditCard() {

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("scheme");

        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getSubtype()).isEqualTo(null);
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodCreditCardEncryptedInfo() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("scheme");

        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth()).isNotNull();
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
        var paymentHeader = new PaymentHeaderDTO();
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setFundingSource("DEBIT");
        paymentHeader.setPaymentDetails(paymentDetails);
        var paymentEvent = new PaymentEventDTO();
        paymentEvent.setEventDetails(new EventDetails());

        paymentEvent.setSubmittedDate(LocalDateTime.now());
        paymentEvent.setAmount(new BigDecimal(25));
        paymentHeader.setPaymentMethod("creditcard");
        var authResponse = mapper.buildAuthResponseFromHeader(Optional.of(paymentHeader), paymentEvent);
        assertNotNull(authResponse);
        assertEquals("2500", authResponse.getAmount()
                .getValue());
        paymentHeader.setPaymentMethod("paypal");
        paymentEvent.setAmount(new BigDecimal(0.65));
        authResponse = mapper.buildAuthResponseFromHeader(Optional.of(paymentHeader), paymentEvent);
        assertNotNull(authResponse);
        assertEquals("65", authResponse.getAmount()
                .getValue());
        paymentHeader.setPaymentMethod("giftcard");
        paymentEvent.setAmount(new BigDecimal(5.99));
        authResponse = mapper.buildAuthResponseFromHeader(Optional.of(paymentHeader), paymentEvent);
        assertNotNull(authResponse);
        assertEquals("599", authResponse.getAmount()
                .getValue());
        paymentHeader.setPaymentDetails(null);
        paymentEvent.setEventDetails(null);
        paymentEvent.setSubmittedDate(null);
        authResponse = mapper.buildAuthResponseFromHeader(Optional.of(paymentHeader), paymentEvent);
        assertNotNull(authResponse);
        authResponse = mapper.buildAuthResponseFromHeader(Optional.empty(), paymentEvent);
        assertNotNull(authResponse);
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodNumberWithCC() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertEquals("scheme", convertedRequest.getPaymentMethod()
                .getType());
        assertNull(convertedRequest.getPaymentMethod()
                .getNumber());
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
    }

    @Test
    void checkAdyenAuthRequestPaymentMethodCCWithSCNull() {
        atgAuthRequest.getPaymentMethod()
                .setEncryptedSecurityCode(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertEquals("scheme", convertedRequest.getPaymentMethod()
                .getType());
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode()).isNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear()).isNotNull();
        assertThat(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth()).isNotNull();
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
    }

    @Test
    void getOrderNumberAndPayGrpNotNull() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getReference()).isEqualTo("3242323523532-0032433433");

        assertThat(convertedRequest.getReference()
                .length()).isEqualByComparingTo(24);
    }

    @Test
    void getEGCFraudDetailsNullCheck() {
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        Map<String, String> additionalData = convertedRequest.getAdditionalData();
        assertNull(additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "brand"));
        assertNull(additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "upc"));
        assertNull(additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "receiverEmail"));
        assertNull(additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "color"));
    }

    @Test
    void getEGCFraudDetailsNotNullCheck() {
        List<CommerceItem> commerceItems = atgAuthRequest.getAdyenFraudDetail()
                .getCommerceItems();
        for (CommerceItem item : commerceItems) {
            item.setRecipientName("Steve Smith");
            item.setRecipientEmail("test@ae.com");
            item.setRecipientMobile("8763567656");
            item.setGiftMessage("This is eGC item for friend");
        }
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        Map<String, String> additionalData = convertedRequest.getAdditionalData();
        assertEquals("Steve Smith", additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "brand"));
        assertEquals("This is eGC item for friend", additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "upc"));
        assertEquals("test@ae.com", additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "receiverEmail"));
        assertEquals("8763567656", additionalData.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "color"));
    }

    @Test
    void getBrowserInfoFor3DS() {
        BrowserInfo bInfo = new BrowserInfo();
        bInfo.setUserAgent("Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/70.0.3538.110 Safari\\/537.36");
        bInfo.setAcceptHeader("text\\/html,application\\/xhtml+xml,application\\/xml;q=0.9,image\\/webp,image\\/apng,*\\/*;q=0.8");
        bInfo.setLanguage("es-MX");
        bInfo.setColorDepth(24);
        bInfo.setScreenHeight(723);
        bInfo.setScreenWidth(1356);
        bInfo.setTimeZoneOffset(0);
        bInfo.setJavaEnabled(Boolean.TRUE);
        atgAuthRequest.setBrowserInfo(bInfo);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertThat(convertedRequest.getBrowserInfo()
                .getUserAgent()).startsWith("Mozilla");
        assertThat(convertedRequest.getBrowserInfo()
                .getAcceptHeader()).startsWith("text");
        assertThat(convertedRequest.getBrowserInfo()
                .getLanguage()).isEqualTo("es-MX");
        assertThat(convertedRequest.getBrowserInfo()
                .getColorDepth()).isEqualTo(24);
        assertThat(convertedRequest.getBrowserInfo()
                .getScreenHeight()).isEqualTo(723);
        assertThat(convertedRequest.getBrowserInfo()
                .getScreenWidth()).isEqualTo(1356);
        assertEquals(0, convertedRequest.getBrowserInfo()
                .getTimeZoneOffset());
        assertThat(convertedRequest.getBrowserInfo()
                .isJavaEnabled()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void getBrowserInfoForNon3DS() {
        atgAuthRequest.setBrowserInfo(null);
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertNull(convertedRequest.getBrowserInfo());
    }

    @Test
    void checkAdyenAuthRequestForPaymentMethodPaypal() {
        atgAuthRequest.getPaymentMethod()
                .setType("paypal");
        atgAuthRequest.getPaymentMethod()
                .setSubtype("sdk");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("paypal");
        assertThat(convertedRequest.getPaymentMethod()
                .getSubtype()).isEqualTo("sdk");

        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedCardNumber());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedSecurityCode());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryYear());
        assertNull(convertedRequest.getPaymentMethod()
                .getEncryptedExpiryMonth());
        assertNull(convertedRequest.getPaymentMethod()
                .getNumber());
        assertNull(convertedRequest.getPaymentMethod()
                .getCvc());
    }

    @Test
    void checkSubTypeForPaymentMethodPaypal() {
        atgAuthRequest.getPaymentMethod()
                .setType("paypal");
        atgAuthRequest.getPaymentMethod()
                .setSubtype("sdk");

        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getSubtype()).isEqualTo("sdk");
    }

    @Test
    void checkSubTypeForPaymentMethodNonPaypal() {
        atgAuthRequest.getPaymentMethod()
                .setType("dummy");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);

        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isNotEqualTo("paypal");
        assertThat(convertedRequest.getPaymentMethod()
                .getSubtype()).isNull();
    }

    @Test
    void checkNullSubTypeForPaypalPayment() {
        atgAuthRequest.getPaymentMethod()
                .setType("paypal");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertThat(convertedRequest.getPaymentMethod()
                .getType()).isEqualTo("paypal");
        assertNull(convertedRequest.getPaymentMethod()
                .getSubtype());
    }

    @Test
    void getPaypalResponseForAuth() {

        Action action = new Action();
        action.setPaymentData("Ab02b4c0!BQABAgCuTqDLQIVdxImSwRcff888jqR4H27bI");
        action.setPaymentMethodType("paypal");
        action.setType("sdk");

        SdkData sdkData = new SdkData();
        sdkData.setToken("EC-2G938969LS3719617");
        action.setSdkData(sdkData);
        adyenAuthResponse.setAction(action);
        adyenAuthResponse.setAdditionalData(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertNull(atgAuthResponse.getAdditionalData());
        assertThat(atgAuthResponse.getAction()
                .getPaymentData()).isEqualTo("Ab02b4c0!BQABAgCuTqDLQIVdxImSwRcff888jqR4H27bI");
        assertThat(atgAuthResponse.getAction()
                .getPaymentMethodType()).isEqualTo("paypal");
        assertThat(atgAuthResponse.getAction()
                .getType()).isEqualTo("sdk");
        assertThat(atgAuthResponse.getAction()
                .getSdkData()
                .getToken()).isEqualTo("EC-2G938969LS3719617");
    }

    @Test
    void get3DSResponseForAuth() {

        Action action = new Action();
        action.setPaymentData("Ab02b4c0!BQABAgCuTqDLQIVdxImSwRcff888jqR4H27bI");
        action.setPaymentMethodType("scheme");
        action.setType("redirect");
        action.setUrl("https://checkoutshopper-test.adyen.com/checkoutshopper/threeDS2.shtml");

        adyenAuthResponse.setAction(action);
        adyenAuthResponse.setAdditionalData(null);
        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getAdditionalData()).isEqualTo(null);
        assertThat(atgAuthResponse.getAction()
                .getPaymentData()).isEqualTo("Ab02b4c0!BQABAgCuTqDLQIVdxImSwRcff888jqR4H27bI");
        assertThat(atgAuthResponse.getAction()
                .getPaymentMethodType()).isEqualTo("scheme");
        assertThat(atgAuthResponse.getAction()
                .getType()).isEqualTo("redirect");
        assertThat(atgAuthResponse.getAction()
                .getUrl()).isEqualTo("https://checkoutshopper-test.adyen.com/checkoutshopper/threeDS2.shtml");
        assertNull(atgAuthResponse.getAction()
                .getSdkData());
    }

    @Test
    void getNonPaypalAnd3DSResponseForAuth() {

        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getAdditionalData()
                .getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertNull(atgAuthResponse.getAction());
    }

    @Test
    void testConvertResponseWhenApplePayPaymentMethodThenSetCreditCardTypeAsPaymentMethod() {
        atgAuthRequest.getPaymentMethod()
                .setType(APPLE_PAY);
        adyenAuthResponse.getAdditionalData()
                .setPaymentMethod(DISCOVER_APPLEPAY);
        var atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);
        assertEquals(DISCOVER_APPLEPAY, atgAuthResponse.getAdditionalData()
                .getCreditCardType());
    }

    @ParameterizedTest
    @CsvSource(value =
    { "true", "false", "null" },
            nullValues = "null")
    void getChannelFor3DS(String input) {
        AdyenAuthRequest convertRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        atgAuthRequest.setExecuteThreeD(input);
        assertThat(convertRequest.getChannel()).isEqualTo("Web");
        mapper.convertToAdyenAuthRequest(atgAuthRequest);
    }

    @Test
    void checkAddress2Null() {
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .setAddress2(null);
        AdyenAuthRequest convertRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertEquals("", convertRequest.getDeliveryAddress()
                .getHouseNumberOrName());
    }

    @Test
    void checkPaymentMethodIsNull() {
        atgAuthRequest.setWebStoreId("02953");
        assertTrue(mapper.skipSendingAddress2ForAuth(atgAuthRequest));
        atgAuthRequest.setPaymentMethod(null);
        assertFalse(mapper.skipSendingAddress2ForAuth(atgAuthRequest));
    }

    @Test
    void testBuildAplazoDeclinedResponse() {
        ATGAuthResponse atgAuthResponse = mapper.buildAplazoDeclinedResponse();

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.DECLINED);
    }

    @Test
    void testValidateAuthTokenAndUpdateResultCodeAuthPending() {

        var adyenResponse = adyenAuthResponse;

        adyenResponse.setResultCode("Authorised");
        adyenResponse.getAdditionalData()
                .setRecurringDetailReference(null);

        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.AUTH_PENDING);

    }

    @Test
    void testValidateAuthTokenAndUpdateResultCodeDeclined() {

        ATGAuthResponse atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.DECLINED);

        var config = TestPaymentsConfig.getPaymentConfig();

        config.setAllowedPaymentsForAuthTokenValidation(List.of());

        adyenAuthResponse.setResultCode("Authorised");

        mapper = new AuthRequestResponseMapper(config, additionalDetailsMapper, new CommonUtils(config));

        atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.AUTHORIZED);

        mapper = new AuthRequestResponseMapper(TestPaymentsConfig.getPaymentConfig(), additionalDetailsMapper,
                new CommonUtils(TestPaymentsConfig.getPaymentConfig()));

        atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.AUTHORIZED);

        adyenAuthResponse.setAdditionalData(null);

        atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.AUTHORIZED);

        var request = atgAuthRequest;
        var paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType(null);
        request.setPaymentMethod(paymentMethod);

        atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, request);

        assertThat(atgAuthResponse.getResultCode()).isEqualTo(PaymentsConstants.AUTHORIZED);
    }

    @Test
    void checkAdyenAuthRequestStoredPaymentMethodGiftcard() {
        atgAuthRequest.getPaymentMethod()
                .setType("giftCard");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertFalse(convertedRequest.isStorePaymentMethod());
    }

    @Test
    void checkAdyenAuthRequestStoredPaymentMethodCreditcard() {
        atgAuthRequest.getPaymentMethod()
                .setType("creditCard");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertTrue(convertedRequest.isStorePaymentMethod());
    }

    @Test
    void checkAdyenAuthRequestStoredPaymentMethodOxxo() {
        atgAuthRequest.getPaymentMethod()
                .setType("Oxxo");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertFalse(convertedRequest.isStorePaymentMethod());
    }

    @Test
    void testAdyenBillingAddress() {
        var authRequest = atgAuthRequest;
        var convertedRequest = mapper.convertToAdyenAuthRequest(authRequest);
        assertNotNull(convertedRequest);
        assertNotNull(convertedRequest.getBillingAddress());

        authRequest.getBillingAddress()
                .setAddress1(null);
        convertedRequest = mapper.convertToAdyenAuthRequest(authRequest);
        assertNotNull(convertedRequest);
        assertNull(convertedRequest.getBillingAddress());

        authRequest.getBillingAddress()
                .setCountry(null);
        convertedRequest = mapper.convertToAdyenAuthRequest(authRequest);
        assertNotNull(convertedRequest);
        assertNull(convertedRequest.getBillingAddress());

        authRequest.getBillingAddress()
                .setCity(null);
        convertedRequest = mapper.convertToAdyenAuthRequest(authRequest);
        assertNotNull(convertedRequest);
        assertNull(convertedRequest.getBillingAddress());
    }

    @ParameterizedTest
    @CsvSource(value =
    { "synchrony_plcc", "synchrony_cbcc", "synchrony_cbcc_applepay", "synchrony_plcc_applepay" },
            nullValues = "null")
    void synchronyCreditCardsTest(String cardType) {
        adyenAuthResponse.getAdditionalData()
                .setFundingSource(null);
        adyenAuthResponse.getAdditionalData()
                .setCardPaymentMethod(cardType);
        var response = mapper.convertToATGAuthResponse(adyenAuthResponse, atgAuthRequest);
        assertEquals(PaymentsConstants.CREDIT, response.getAdditionalData()
                .getFundingSource());
    }

    @Test
    void testAdyenApplePayInteracResponse() {
        var adyenAuthResponse = AdyenTestUtils.getAdyenApplePayInteracAuthResponse();
        var atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, AdyenTestUtils.getApplePayAuthRequest());
        assertNotNull(atgAuthResponse);
        assertNotNull(atgAuthResponse.getResultCode());
        assertEquals("SETTLED", atgAuthResponse.getResultCode());
        adyenAuthResponse.getAdditionalData()
                .setPaymentMethod(null);
        atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthResponse, AdyenTestUtils.getApplePayAuthRequest());
        assertNotNull(atgAuthResponse);
        assertNotNull(atgAuthResponse.getResultCode());
        assertEquals("AUTHORIZED", atgAuthResponse.getResultCode());

        var adyenAuthRefusedResponse = AdyenTestUtils.getAdyenApplePayInteracRefusedResponse();
        atgAuthResponse = mapper.convertToATGAuthResponse(adyenAuthRefusedResponse, AdyenTestUtils.getApplePayAuthRequest());
        assertNotNull(atgAuthResponse);
        assertNotNull(atgAuthResponse.getResultCode());
        assertEquals("AUTHORIZE_REFUSED", atgAuthResponse.getResultCode());
    }

    @Test
    void checkDeliveryAddress() {
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingMethod("BPS");
        AdyenAuthRequest convertedRequest = mapper.convertToAdyenAuthRequest(atgAuthRequest);
        assertNull(convertedRequest.getDeliveryAddress());
    }

}
