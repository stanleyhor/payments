package com.ecomm.payments.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.adyen.AdditionalDataRequest;
import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.FraudResult;
import com.ecomm.payments.model.atg.AdyenEnhancedSchemeData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.atg.CommerceItem;
import com.ecomm.payments.model.atg.ContactInfo;
import com.ecomm.payments.model.atg.FraudDetail;
import com.ecomm.payments.model.atg.ItemDetailLine;
import com.ecomm.payments.model.atg.RequestContext;
import com.ecomm.payments.model.atg.ShippingAddress;
import com.ecomm.payments.model.atg.ShippingDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AdditionalDetailsMapperTest {

    private AdditionalDetailsMapper mapper;
    private ATGAuthRequest atgAuthRequest;
    private AdyenAuthResponse adyenAuthResponse;
    private HttpHeaders headers;

    private static final String AUTH_TOKEN = "8315901675746062";

    @BeforeEach
    public void setUp() {
        mapper = new AdditionalDetailsMapper();
        atgAuthRequest = new ATGAuthRequest();
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

        var requestContext = new RequestContext();
        requestContext.setChannelType("WEB");
        atgAuthRequest.setRequestContext(requestContext);

        FraudDetail adyenFraudDetail = new FraudDetail();

        adyenFraudDetail.setDeviceFingerPrint("blahblah");
        adyenFraudDetail.setOrderDiscountAmount("10.00");
        adyenFraudDetail.setCommerceItemCount("1");
        adyenFraudDetail.setCouponCode("GETREADY|EMPLOYEE|ABCD1234567890098765|PROMO123456");

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
        shippingAddress.setNeighborhood("Centro Historico");
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
        commerceItem.setItemType("Regular");
        commerceItems.add(commerceItem);
        CommerceItem commerceItem1 = new CommerceItem();
        commerceItem1.setCommerceItemId("ci324324325324");
        commerceItem1.setDisplayName("Real Me Full");
        commerceItem1.setProductUPC("032-0343");
        commerceItem1.setQuantity("1");
        commerceItem1.setSalePrice("39.95");
        commerceItem1.setSku("0026516659");
        commerceItem1.setItemType("VGC");
        commerceItems.add(commerceItem1);
        adyenFraudDetail.setCommerceItems(commerceItems);

        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);

        var enhancedSchemeData = new AdyenEnhancedSchemeData();
        enhancedSchemeData.setCustomerReference("cust");
        var itemDetailLines = new ArrayList<ItemDetailLine>();
        var detailLine = new ItemDetailLine();
        detailLine.setQuantity(1);
        detailLine.setUnitPrice(2200);
        detailLine.setTotalAmount(2200);
        detailLine.setDescription("Sample Product éàöñ Description");
        detailLine.setProductCode("productCode");
        itemDetailLines.add(detailLine);
        enhancedSchemeData.setItemDetailLineList(itemDetailLines);
        atgAuthRequest.setEnhancedSchemeData(enhancedSchemeData);

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
    void getAdditionalDataFailureTest() {
        AdditionalDataRequest additionalData = new AdditionalDataRequest();
        additionalData.setRequestedTestAcquirerResponseCode("6");
        atgAuthRequest.setAdditionalData(additionalData);

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("6", additionalDetails.get("RequestedTestAcquirerResponseCode"));
    }

    @ParameterizedTest
    @ValueSource(booleans =
    { true, false })
    void testAdditionalFraudFieldsForMX(boolean flag) {
        var adyenFraudDetail = atgAuthRequest.getAdyenFraudDetail();
        adyenFraudDetail.setHasLoyalty(flag);
        atgAuthRequest.setSiteId("AEO_MX");
        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);
        var requestContext = new RequestContext();
        requestContext.setChannelType("WEB");
        atgAuthRequest.setRequestContext(requestContext);
        var additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals(additionalDetails.get("riskdata.channel"), atgAuthRequest.getRequestContext()
                .getChannelType());
        if (flag) {
            assertEquals("y", additionalDetails.get("riskdata.loyalty"));
        } else {
            assertEquals("n", additionalDetails.get("riskdata.loyalty"));
        }

        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingMethod("BPS");
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals(additionalDetails.get("riskdata.pickupfirstname"), atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getFirstName());
        assertEquals(additionalDetails.get("riskdata.pickupcity"), atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getCity());
        assertEquals(additionalDetails.get("riskdata.pickupcountry"), atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getCountry());
        atgAuthRequest.getAdyenFraudDetail()
                .setContactInfo(null);
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        assertEquals(additionalDetails.get("riskdata.pickupfirstname"), null);
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        assertEquals(additionalDetails.get("riskdata.pickupfirstname"), null);
        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(null);
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        assertEquals(additionalDetails.get("riskdata.pickupfirstname"), null);
    }

    @ParameterizedTest
    @CsvSource(value =
    { "AEO_MX, creditCard", "AEO_US, creditCard", "AEO_MX, giftCard", "AEO_US, giftCard" })
    void testAdditionalFraudFieldsForMXCouponCode(String siteId, String paymentMethod) {

        var atgPaymentMethod = new AtgPaymentMethod();
        atgPaymentMethod.setType(paymentMethod);
        atgAuthRequest.setPaymentMethod(atgPaymentMethod);

        var adyenFraudDetail = atgAuthRequest.getAdyenFraudDetail();
        adyenFraudDetail.setHasLoyalty(true);
        atgAuthRequest.setSiteId(siteId);
        adyenFraudDetail.setCouponCode("");
        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);
        var requestContext = new RequestContext();
        requestContext.setChannelType("WEB");
        atgAuthRequest.setRequestContext(requestContext);
        var additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        if ("AEO_MX".equals(siteId)
                && "creditCard".equals(paymentMethod)) {
            assertEquals("0", additionalDetails.get("riskdata.couponQuantity"));
        }
        adyenFraudDetail.setCouponCode("ABCD1234567890098765");
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        if ("AEO_MX".equals(siteId)
                && "creditCard".equals(paymentMethod)) {
            assertEquals("1", additionalDetails.get("riskdata.couponQuantity"));
        }
        adyenFraudDetail.setCouponCode("promo123");
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        if ("AEO_MX".equals(siteId)
                && "creditCard".equals(paymentMethod)) {
            assertEquals("0", additionalDetails.get("riskdata.couponQuantity"));
        }

        adyenFraudDetail.setCouponCode("promo123,ABCD1234567890098765|Abc1234,promo12|Abc123");
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);
        if ("AEO_MX".equals(siteId)
                && "creditCard".equals(paymentMethod)) {
            assertEquals("3", additionalDetails.get("riskdata.couponQuantity"));
        }

        adyenFraudDetail.setCouponCode(null);
        additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        if ("AEO_MX".equals(siteId)
                && "creditCard".equals(paymentMethod)) {
            assertNull(additionalDetails.get("riskdata.couponQuantity"));
        }
    }

    @Test
    void getAdditionalDataCheckCommerceItem() {
        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("1", additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "quantity"));
    }

    @Test
    void getAdditionalDataNullCheckShippingDetail() {
        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(null);

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertNull(additionalDetails.get("riskdata.shippingMethod"));
    }

    @Test
    void getAdditionalDataNullCheckShippingNeighourhood() {
        ShippingDetail shippingDetail = new ShippingDetail();
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingDetail.setShippingAddress(shippingAddress);
        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(shippingDetail);

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertNull(additionalDetails.get("riskdata.neighborhood"));
    }

    @Test
    void getAdditionalDataNullCheckCommerceItem() {
        atgAuthRequest.getAdyenFraudDetail()
                .setCommerceItems(null);

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertNull(additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "quantity"));
    }

    @Test
    void getAdditionalDataTimeOnFIle() {
        atgAuthRequest.getAdyenFraudDetail()
                .setDaysSinceRegistration(15);
        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("15", additionalDetails.get(PaymentsConstants.TIME_ON_FILE));
    }

    @Test
    void getAdditionalDataAlternatePaymentTypeForCreditCard() {
        atgAuthRequest.getPaymentMethod()
                .setType("creditcard");
        atgAuthRequest.getEnhancedSchemeData()
                .getItemDetailLineList()
                .get(0)
                .setDescription(null);

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertNull(additionalDetails.get(PaymentsConstants.ALTERNATE_PAYMENT_TYPE));
    }

    @Test
    void getAdditionalDataAlternatePaymentTypeForGiftCard() {
        atgAuthRequest.getPaymentMethod()
                .setType("giftcard");

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("giftcard", additionalDetails.get(PaymentsConstants.ALTERNATE_PAYMENT_TYPE));
    }

    @Test
    void getAdditionalDataNotNullRecipientNameCheck() {
        List<CommerceItem> commerceItems = atgAuthRequest.getAdyenFraudDetail()
                .getCommerceItems();
        for (CommerceItem item : commerceItems) {
            item.setRecipientName("Steve Smith");
        }

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("Steve Smith", additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "brand"));
    }

    @Test
    void getAdditionalDataNotNullGiftMessageCheck() {
        List<CommerceItem> commerceItems = atgAuthRequest.getAdyenFraudDetail()
                .getCommerceItems();
        for (CommerceItem item : commerceItems) {
            item.setGiftMessage("This is eGC item for friend");
        }

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("This is eGC item for friend", additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "upc"));
    }

    @Test
    void getAdditionalDataNotNullRecipientEmailCheck() {
        List<CommerceItem> commerceItems = atgAuthRequest.getAdyenFraudDetail()
                .getCommerceItems();
        for (CommerceItem item : commerceItems) {
            item.setRecipientEmail("test@ae.com");
        }

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("test@ae.com", additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "receiverEmail"));
    }

    @Test
    void getAdditionalDataNotNullRecipientMobileCheck() {

        List<CommerceItem> commerceItems = atgAuthRequest.getAdyenFraudDetail()
                .getCommerceItems();
        for (CommerceItem item : commerceItems) {
            item.setRecipientMobile("8763567656");
        }
        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertEquals("8763567656", additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "color"));
    }

    @Test
    void getAdditionalDataNullChecks() {

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertNull(additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "brand"));
        assertNull(additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "upc"));
        assertNull(additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "receiverEmail"));
        assertNull(additionalDetails.get(PaymentsConstants.RISK_BASKET_ITEM
                + 1
                + "."
                + "color"));
    }

    @Test
    void getAdditionalDataItemDetailLineListEmpty() {
        atgAuthRequest.getEnhancedSchemeData()
                .setItemDetailLineList(null);

        Map<String, String> additionalDetails = mapper.getAdditionalDetails(atgAuthRequest);

        assertThat(additionalDetails).isNotNull();
    }

}
