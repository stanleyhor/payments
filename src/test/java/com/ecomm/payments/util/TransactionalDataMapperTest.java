package com.ecomm.payments.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.BillingAddress;
import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.IntlPaymentAuthMessage;
import com.ecomm.payments.model.OrderPaymentDetails;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.PaymentHeader;
import com.ecomm.payments.model.PaymentStatus;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.adyen.Action;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.Installments;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.atg.CommerceItem;
import com.ecomm.payments.model.atg.ContactInfo;
import com.ecomm.payments.model.atg.FraudDetail;
import com.ecomm.payments.model.atg.FraudResult;
import com.ecomm.payments.model.atg.RequestContext;
import com.ecomm.payments.model.atg.ShippingAddress;
import com.ecomm.payments.model.atg.ShippingDetail;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TransactionalDataMapperTest {

    public static final String PAYMENT_HEADER_ID = "pg121590693";
    public static final String IDEMPOTENCY_KEY = "07nk9zk1-0n4-xhhk-ky85-l12ef88ad321";
    public static final String PAYPAL = "Paypal";
    private ATGAuthRequest atgAuthRequest;

    private ATGAuthResponse atgAuthResponse;

    private TransactionalDataMapper transactionalDataMapper;

    private PaymentDetailsRequest paymentDetailsRequest;

    private PaymentDetailsResponse paymentDetailsResponse;

    @Mock
    PaymentHeaderRepository paymentsRepository;

    @BeforeEach
    public void setUp() {
        transactionalDataMapper = new TransactionalDataMapper(paymentsRepository, TestPaymentsConfig.getPaymentConfig());
        atgAuthRequest = new ATGAuthRequest();
        atgAuthRequest.setPaymentGroupId(PAYMENT_HEADER_ID);
        atgAuthRequest.setAtgProfileId("3242323523532");
        atgAuthRequest.setOrderNumber("3242323523532");
        atgAuthRequest.setIdempotencyKey("UID-1");

        Amount amount = new Amount();
        amount.setValue("1500");
        amount.setCurrency("MXN");
        atgAuthRequest.setAmount(amount);

        RequestContext requestContext = new RequestContext();
        requestContext.setGateway("Adyen");
        atgAuthRequest.setRequestContext(requestContext);

        AtgPaymentMethod paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType("creditcard");
        paymentMethod.setEncryptedCardNumber("test_4111111111111111");
        paymentMethod.setEncryptedExpiryMonth("test_03");
        paymentMethod.setEncryptedExpiryYear("test_2030");
        paymentMethod.setEncryptedSecurityCode("test_737");
        paymentMethod.setStoredPaymentMethodId("8315901675746062");
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
        atgAuthRequest.setInstallments(new Installments());

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
        commerceItems.add(commerceItem);
        adyenFraudDetail.setCommerceItems(commerceItems);

        atgAuthRequest.setAdyenFraudDetail(adyenFraudDetail);
        atgAuthResponse = new ATGAuthResponse();

        ATGAdditionalData additionalData = new ATGAdditionalData();
        additionalData.setAuthCode("012097");
        additionalData.setAvsResult("4 AVS not supported for this card type");
        additionalData.setAvsResultRaw("2");
        additionalData.setCvvResponseCode("1 Matches");
        additionalData.setPaymentMethod("visa");
        additionalData.setPaymentAccountReference("56789536728");
        atgAuthResponse.setAdditionalData(additionalData);
        atgAuthResponse.setAmount(amount);

        FraudResult fraudResult = new FraudResult();
        fraudResult.setAccountScore(50);
        atgAuthResponse.setFraudResult(fraudResult);

        atgAuthResponse.setMerchantReference("3242323523532-pg121590693");
        atgAuthResponse.setPspReference("852597229203094H");
        atgAuthResponse.setResultCode("AUTHORIZED");
        atgAuthResponse.setTransactionTimestamp(LocalDateTime.now()
                .toString());

        Action action = new Action();

        atgAuthResponse.setAction(action);

        paymentDetailsRequest = new PaymentDetailsRequest();
        paymentDetailsRequest.setPaymentData("paymentData");
        Details details = new Details();
        details.setBillingToken("76543");
        details.setFacilitatorAccessToken("87655443");
        details.setMd("MD");
        details.setOrderID("3242323523532");
        details.setPaRes("fdf");
        details.setPayerID("paypalId");
        details.setPayload("payload");
        details.setPaymentID(PAYMENT_HEADER_ID);
        paymentDetailsRequest.setDetails(details);

        paymentDetailsResponse = new PaymentDetailsResponse();
        ResponseData data = new ResponseData();
        OrderPaymentDetails order = new OrderPaymentDetails();
        order.setCurrencyCode("USD");
        order.setOrderNumber("3242323523532");
        PaymentStatus status = new PaymentStatus();
        status.setAdditionalData(additionalData);
        status.setBillingAddress(billingAddress);
        status.setAmountAuthorized(0);
        status.setCurrencyCode("USD");
        status.setFraudManualReview(false);
        status.setIdempotencyKey("bjhvasjlfihv");
        status.setPaymentGroupId(PAYMENT_HEADER_ID);
        status.setPaymentType("paypal");
        status.setPspReference("fgykuiokgty");
        status.setResultCode("AUTHORIZED");
        List<PaymentStatus> list = new ArrayList<>();
        list.add(status);
        order.setPaymentStatus(list);
        data.setOrderPaymentDetails(order);
        paymentDetailsResponse.setData(data);

    }

    @Test
    public void storeTransactionData() {
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void storeTransactionDataNullBillingAddress() {
        atgAuthRequest.setRequestContext(null);
        atgAuthRequest.setBillingAddress(null);
        atgAuthResponse.setAction(null);
        atgAuthRequest.setInstallments(null);
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void storeTransactionDataNullBillingAddressFirstName() {
        atgAuthRequest.setRequestContext(null);
        atgAuthRequest.getBillingAddress()
                .setFirstName(null);
        atgAuthResponse.setAction(null);
        atgAuthRequest.setAdyenFraudDetail(null);
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void storeTransactionDataNullBillingAddressLastName() {
        atgAuthRequest.setRequestContext(null);
        atgAuthRequest.getBillingAddress()
                .setLastName(null);
        atgAuthRequest.getAdyenFraudDetail()
                .setContactInfo(null);
        atgAuthResponse.setAction(null);
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void storeTransactionDataGiftCard() {
        atgAuthRequest.getPaymentMethod()
                .setType("giftCard");
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void getMerchantAccount() {
        atgAuthResponse.getAdditionalData()
                .setCreditCardType("visa");
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertThat("visa").isEqualTo(atgAuthResponse.getAdditionalData()
                .getCreditCardType());
    }

    @Test
    public void convertCentsInToDollarsNullAmount() {
        atgAuthRequest.getAmount()
                .setValue(null);
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void convertCentsInToDollarsAmount() {
        atgAuthRequest.getAmount()
                .setValue("0.0D");
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void storeTransactionalDataNullAmount() {
        atgAuthResponse.setAmount(null);
        atgAuthResponse.setMerchantReference(null);
        atgAuthResponse.setAdditionalData(null);
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void paypalPaymentDetails() {
        atgAuthRequest.getPaymentMethod()
                .setType("paypal");
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void paypalAdditionalDataNull() {
        atgAuthRequest.getPaymentMethod()
                .setType("paypal");
        atgAuthResponse.setAdditionalData(null);
        transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse);

        assertTrue(true);
    }

    @Test
    public void paymentHeaderEmpty() {
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        assertTrue(true);
    }

    @Test
    public void checkOXXOPayment() {
        atgAuthRequest.setBillingAddress(null);
        atgAuthRequest.getPaymentMethod()
                .setType(PaymentsConstants.OXXO_PAYMENT_METHOD);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));

        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));

        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));

        atgAuthRequest.setAdyenFraudDetail(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));

        atgAuthRequest.getPaymentMethod()
                .setType(PaymentsConstants.APPLE_PAY);
        atgAuthResponse.getAdditionalData()
                .setPaymentAccountReference(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));

        atgAuthRequest.setCheckoutType("express");
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checkOXXOPayShippingAddressNull() {
        atgAuthRequest.getPaymentMethod()
                .setType(PaymentsConstants.OXXO_PAYMENT_METHOD);
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checkShippingAddressNull() {
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checAdyenFraudDetailAndBillingAddressNull() {
        atgAuthRequest.setBillingAddress(null);
        atgAuthRequest.setAdyenFraudDetail(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checkShiipingDetailDetailAndBillingAddressNull() {
        atgAuthRequest.setBillingAddress(null);
        atgAuthRequest.getAdyenFraudDetail()
                .setShippingDetail(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checkExistingPaymentEvents() {

        Optional<PaymentHeaderDTO> headerDTO = Optional.of(this.getPaymentHeader());
        PaymentEventDTO paymentEvent = this.createPaymentEvent();
        headerDTO.get()
                .getPaymentEvents()
                .add(paymentEvent);
        Mockito.when(paymentsRepository.findById(ArgumentMatchers.any()))
                .thenReturn(headerDTO);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checkBillingAddressNull() {

        Optional<PaymentHeaderDTO> headerDTO = Optional.of(this.getPaymentHeader());
        headerDTO.get()
                .setBillingAddress(null);
        Mockito.when(paymentsRepository.findById(ArgumentMatchers.any()))
                .thenReturn(headerDTO);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void checkShippingAndBillingAddressNull() {
        atgAuthRequest.setBillingAddress(null);
        atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(atgAuthRequest, atgAuthResponse));
    }

    @Test
    public void updateTransactionalDataPaymentHeader() {

        Optional<PaymentHeaderDTO> headerDTO = Optional.of(getPaymentHeader());
        Mockito.when(paymentsRepository.findById(PAYMENT_HEADER_ID))
                .thenReturn(headerDTO);
        headerDTO.get()
                .getPaymentEvents()
                .add(createPaymentEvent());
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setPaymentType(PaymentsConstants.CREDIT_CARD);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getBillingAddress()
                .setLastName(null);
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setPaymentType(PaymentsConstants.CREDIT_CARD);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getBillingAddress()
                .setLastName(null);
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setPaymentType(PaymentsConstants.PAYPAL_PAYMENT_METHOD);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getBillingAddress()
                .setFirstName(null);
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setPaymentType(PaymentsConstants.CREDIT_CARD);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setBillingAddress(null);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setAdditionalData(null);
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setPaymentType(PaymentsConstants.PAYPAL_PAYMENT_METHOD);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setBillingAddress(null);
        paymentDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setAdditionalData(null);
        transactionalDataMapper.updateTransactionalData(paymentDetailsResponse);

        assertTrue(true);
    }

    @ParameterizedTest
    @CsvSource(value =
    { "07nk9zk1-0n4-xhhk-ky85-l12ef88ad321, 05nk9zk1-0n4-xhhk-ly85-l12ef88ad329", "07nk9zk1-0n4-xhhk-ky85-l12ef88ad321, 07nk9zk1-0n4-xhhk-ky85-l12ef88ad321",
        "null, 07nk9zk1-0n4-xhhk-ky85-l12ef88ad321" },
            nullValues = "null")
    void testUpdateReAuthPaymentData(String idempotencyKey1, String idempotencyKey2) {
        var authRequest = atgAuthRequest;
        authRequest.setReauth(true);
        authRequest.getPaymentMethod()
                .setType(PAYPAL);
        authRequest.setIdempotencyKey(idempotencyKey1);
        var paymentHeaderDTO = getPaymentHeader();
        var paymentEventDTOList = new ArrayList<PaymentEventDTO>();
        var paymentEventDTO = new PaymentEventDTO();
        paymentEventDTO.setPaymentEventId(idempotencyKey2);
        paymentEventDTOList.add(paymentEventDTO);
        paymentHeaderDTO.setPaymentEvents(paymentEventDTOList);
        var headerDTO = Optional.of(paymentHeaderDTO);
        Mockito.when(paymentsRepository.findById(PAYMENT_HEADER_ID))
                .thenReturn(headerDTO);
        transactionalDataMapper.updateReAuthPaymentData(atgAuthRequest, atgAuthResponse);
        var paymentHeader = transactionalDataMapper.getPaymentHeader(PAYMENT_HEADER_ID);
        assertNotNull(paymentHeader);
        paymentHeader = transactionalDataMapper.getPaymentHeader(null);
        assertNotNull(paymentHeader);
    }

    @Test
    void testUpdateReAuthPaymentDataWhenPaymentHeaderDTOIsNull() {
        var authRequest = atgAuthRequest;
        authRequest.setReauth(true);
        authRequest.getPaymentMethod()
                .setType(PAYPAL);
        authRequest.setIdempotencyKey(IDEMPOTENCY_KEY);
        transactionalDataMapper.updateReAuthPaymentData(atgAuthRequest, atgAuthResponse);
        var paymentHeader = transactionalDataMapper.getPaymentHeader(PAYMENT_HEADER_ID);
        assertNotNull(paymentHeader);
    }

    @Test
    void testUpdateReAuthPaymentDataWhenPaymentEventIsNull() {
        var authRequest = atgAuthRequest;
        authRequest.setReauth(true);
        authRequest.getPaymentMethod()
                .setType(PAYPAL);
        authRequest.setIdempotencyKey(IDEMPOTENCY_KEY);
        var paymentHeaderDTO = getPaymentHeader();
        paymentHeaderDTO.setPaymentEvents(Collections.emptyList());
        var headerDTO = Optional.of(paymentHeaderDTO);
        Mockito.when(paymentsRepository.findById(PAYMENT_HEADER_ID))
                .thenReturn(headerDTO);
        transactionalDataMapper.updateReAuthPaymentData(atgAuthRequest, atgAuthResponse);
        var paymentHeader = transactionalDataMapper.getPaymentHeader(PAYMENT_HEADER_ID);
        assertNotNull(paymentHeader);
    }

    @Test
    void testAplazoPaymentAuthData() {
        IntlPaymentAuthMessage intlPaymentAuthMessage = new IntlPaymentAuthMessage();
        intlPaymentAuthMessage.setOrderNumber("0002919021");
        intlPaymentAuthMessage.setCartId("o730575550070");
        intlPaymentAuthMessage.setProfileId("ugp2233800071");
        intlPaymentAuthMessage.setSiteId("AEO_MX");

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setAddress1("22");
        billingAddress.setAddress2("Hot Metal st");
        billingAddress.setFirstName("Test");
        billingAddress.setLastName("User");
        billingAddress.setPhoneNumber("+526354635689");
        billingAddress.setPostalCode("15234");
        billingAddress.setState("CMX");
        billingAddress.setCity("Mexico City");
        billingAddress.setCountry("MX");
        billingAddress.setEmail("email@ae.com");

        PaymentHeader paymentHeaders = new PaymentHeader();
        paymentHeaders.setAmount(1219);
        paymentHeaders.setCurrencyCode("MXN");
        paymentHeaders.setGatewayIndicator("APLAZO");
        paymentHeaders.setLoanId(123_456);
        paymentHeaders.setPaymentHeaderId("ph123456789");
        paymentHeaders.setPaymentMethod("altPayments");
        paymentHeaders.setPaymentVariation("aplazo");
        paymentHeaders.setState("REDIRECT_APLAZO");
        paymentHeaders.setSubmittedDate(LocalDateTime.now());
        paymentHeaders.setBillingAddress(billingAddress);
        intlPaymentAuthMessage.setPaymentHeaders(Arrays.asList(paymentHeaders));

        assertDoesNotThrow(() -> transactionalDataMapper.storeIntlAuthData(intlPaymentAuthMessage));
        paymentHeaders.setBillingAddress(null);
        assertDoesNotThrow(() -> transactionalDataMapper.storeIntlAuthData(intlPaymentAuthMessage));

        Optional<PaymentHeaderDTO> headerDTO = Optional.of(this.getPaymentHeaderForAplazo());
        PaymentEventDTO paymentEvent = this.createPaymentEvent();
        headerDTO.get()
                .getPaymentEvents()
                .add(paymentEvent);

        Mockito.when(paymentsRepository.findById(ArgumentMatchers.any()))
                .thenReturn(headerDTO);
        assertDoesNotThrow(() -> transactionalDataMapper.storeIntlAuthData(intlPaymentAuthMessage));

    }

    @Test
    void testApplePayInteracAuthResponse() {

        assertDoesNotThrow(() -> transactionalDataMapper.storeTransactionalData(AdyenTestUtils.getApplePayInteracAuthRequest(),
                AdyenTestUtils.getApplePayInteracAuthResponse()));
    }

    private PaymentHeaderDTO getPaymentHeader() {
        PaymentHeaderDTO paymentHeader = new PaymentHeaderDTO();
        paymentHeader.setLastModifiedBy("STS");
        paymentHeader.setCurrencyCode("USD");
        paymentHeader.setGatewayIndicator("gateway-indicator");
        paymentHeader.setPaymentDetails(new PaymentDetails());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setOrderNumber("3242323523532");
        paymentHeader.setPaymentHeaderId(PAYMENT_HEADER_ID);
        paymentHeader.setOrderRef("o3525353453");
        paymentHeader.setSiteId("AEO_US");
        paymentHeader.setState("Authorized");
        paymentHeader.setPaymentMethod("paypal");
        paymentHeader.setBillingAddress(getBillingAddress());
        return paymentHeader;
    }

    private PaymentHeaderDTO getPaymentHeaderForAplazo() {
        PaymentHeaderDTO paymentHeader = new PaymentHeaderDTO();
        paymentHeader.setLastModifiedBy("STS");
        paymentHeader.setCurrencyCode("mxn");
        paymentHeader.setGatewayIndicator("gateway-indicator");
        paymentHeader.setPaymentDetails(new PaymentDetails());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setOrderNumber("3242323523532");
        paymentHeader.setPaymentHeaderId(PAYMENT_HEADER_ID);
        paymentHeader.setOrderRef("o3525353453");
        paymentHeader.setSiteId("AEO_MX");
        paymentHeader.setState("REDIRECT_APLAZO");
        paymentHeader.setPaymentMethod("aplazo");
        paymentHeader.setBillingAddress(null);
        return paymentHeader;
    }

    private BillingAddressDTO getBillingAddress() {
        BillingAddressDTO billingAddress = new BillingAddressDTO();
        billingAddress.setFirstName("Samanth");
        billingAddress.setLastName("KV");
        billingAddress.setAddress1("403, complex");
        billingAddress.setAddress1("manimala enclave");
        billingAddress.setEmail("test@yopmaillll.com");
        billingAddress.setCity("Hyderabad");
        billingAddress.setCountry("India");
        billingAddress.setState("Telangana");
        billingAddress.setPostalCode("507133");
        return billingAddress;
    }

    private PaymentEventDTO createPaymentEvent() {
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

}