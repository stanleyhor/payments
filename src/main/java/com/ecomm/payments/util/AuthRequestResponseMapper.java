package com.ecomm.payments.util;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.BrowserInfo;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.BillingAddress;
import com.ecomm.payments.model.adyen.DeliveryAddress;
import com.ecomm.payments.model.adyen.MerchantRiskIndicator;
import com.ecomm.payments.model.adyen.PaymentMethod;
import com.ecomm.payments.model.adyen.ShopperName;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.FraudResult;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class AuthRequestResponseMapper {

    private static final String EMPTY_STR = "";
    private PaymentsConfig paymentsConfig;
    private AdditionalDetailsMapper detailsMapper;
    private CommonUtils commonUtils;

    public AdyenAuthRequest convertToAdyenAuthRequest(ATGAuthRequest atgAuthRequest) {

        log.debug("AuthRequestResponseMapper.convertToAdyenAuthRequest() :: START");

        if (atgAuthRequest == null) {
            return null;
        }
        AdyenAuthRequest adyenAuthRequest = new AdyenAuthRequest();
        // Modified for PAYMS-130
        atgAuthRequest.setPaymentGroupId(getPaymentGroupId(atgAuthRequest));
        adyenAuthRequest.setMerchantAccount(getMerchantAccount(atgAuthRequest));
        adyenAuthRequest.setReference(getReference(atgAuthRequest));
        adyenAuthRequest.setMerchantOrderReference(atgAuthRequest.getOrderNumber());

        adyenAuthRequest.setAmount(atgAuthRequest.getAmount());
        if (atgAuthRequest.getPaymentMethod() != null) {
            adyenAuthRequest.setPaymentMethod(getPaymentDetails(atgAuthRequest));

            setRecurringProcessingModel(adyenAuthRequest, atgAuthRequest);
            setStorePaymentMethod(atgAuthRequest, adyenAuthRequest);
        }
        adyenAuthRequest.setReturnUrl(atgAuthRequest.getReturnUrl());
        if (atgAuthRequest.getAdyenFraudDetail() != null) {
            adyenAuthRequest.setShopperName(getShopperName(atgAuthRequest));
            if (atgAuthRequest.getAdyenFraudDetail()
                    .getContactInfo() != null) {
                adyenAuthRequest.setShopperEmail(atgAuthRequest.getAdyenFraudDetail()
                        .getContactInfo()
                        .getEmail());
            }
        }

        if (atgAuthRequest.getPaymentMethod() != null
                && PaymentsConstants.OXXO_PAYMENT_METHOD.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                        .getType())) {
            setCountryCode(atgAuthRequest, adyenAuthRequest);

            if (StringUtils.isBlank(atgAuthRequest.getReturnUrl())) {
                adyenAuthRequest.setReturnUrl(PaymentsConstants.OXXO_PAYMENT_RETURN_URL);
            }

            // code added for PAYMS-127
            adyenAuthRequest.setStorePaymentMethod(false);
            adyenAuthRequest.setShopperLocale(getShopperLocale(atgAuthRequest));
            return adyenAuthRequest;
        }

        if (atgAuthRequest.getBillingAddress() != null) {
            adyenAuthRequest.setBillingAddress(getBillingAddress(atgAuthRequest));
        }

        adyenAuthRequest.setShopperInteraction(atgAuthRequest.getShopperInteraction());
        adyenAuthRequest.setShopperReference(atgAuthRequest.getAtgProfileId());
        adyenAuthRequest.setAdditionalData(detailsMapper.getAdditionalDetails(atgAuthRequest));

        if (atgAuthRequest.getAdyenFraudDetail() != null) {

            if (!PaymentsConstants.SHIPPING_METHOD_BOPIS.equalsIgnoreCase(adyenAuthRequest.getAdditionalData()
                    .get(PaymentsConstants.RISK_SHIPPING_METHOD))) {
                adyenAuthRequest.setDeliveryAddress(getDeliveryAddress(atgAuthRequest));
            }

            if (atgAuthRequest.getAdyenFraudDetail()
                    .getContactInfo() != null) {
                adyenAuthRequest.setTelephoneNumber(atgAuthRequest.getAdyenFraudDetail()
                        .getContactInfo()
                        .getPhoneNo());
            }
            adyenAuthRequest.setShopperIP(atgAuthRequest.getAdyenFraudDetail()
                    .getCustomerIp());
            adyenAuthRequest.setDeviceFingerprint(atgAuthRequest.getAdyenFraudDetail()
                    .getDeviceFingerPrint());

            adyenAuthRequest.setMerchantRiskIndicator(getMerchantRiskIndicator(atgAuthRequest));
        }

        adyenAuthRequest.setMetadata(getMetaData(atgAuthRequest));
        // for 3DS
        adyenAuthRequest.setBrowserInfo(getBrowserInfo(atgAuthRequest));
        adyenAuthRequest.setChannel(getAdyenChannelType(atgAuthRequest.getRequestContext()
                .getChannelType()));
        adyenAuthRequest.setInstallments(atgAuthRequest.getInstallments());

        log.debug("AuthRequestResponseMapper.convertToAdyenAuthRequest() :: END");

        return adyenAuthRequest;
    }

    private void setStorePaymentMethod(ATGAuthRequest atgAuthRequest, AdyenAuthRequest adyenAuthRequest) {

        adyenAuthRequest.setStorePaymentMethod(true);

        if (PaymentsConstants.GIFT_CARD.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                .getType())
                || PaymentsConstants.ADYEN_SHOPPER_INTERACTION_CONTAUTH.equalsIgnoreCase(atgAuthRequest.getShopperInteraction())) {
            adyenAuthRequest.setStorePaymentMethod(false);
        }

    }

    private void setRecurringProcessingModel(AdyenAuthRequest adyenAuthRequest, ATGAuthRequest atgAuthRequest) {

        String paymentMethodType = atgAuthRequest.getPaymentMethod()
                .getType();
        boolean reauthOrRetrocharge = atgAuthRequest.isReauth()
                || atgAuthRequest.isRetroCharge();

        if (PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(paymentMethodType)) {
            adyenAuthRequest.setRecurringProcessingModel(PaymentsConstants.CARD_ON_FILE);
        } else if (PaymentsConstants.APPLE_PAY.equalsIgnoreCase(paymentMethodType)
                || PaymentsConstants.PAYPAL_PAYMENT_METHOD.equalsIgnoreCase(paymentMethodType)) {
            adyenAuthRequest.setRecurringProcessingModel(reauthOrRetrocharge ? PaymentsConstants.UNSCHEDULED_CARD_ON_FILE : PaymentsConstants.CARD_ON_FILE);
        }
    }

    private void setCountryCode(ATGAuthRequest atgAuthRequest, AdyenAuthRequest adyenAuthRequest) {

        if (atgAuthRequest.getAdyenFraudDetail()
                .getShippingDetail() != null
                && atgAuthRequest.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingAddress() != null) {
            adyenAuthRequest.setCountryCode(atgAuthRequest.getAdyenFraudDetail()
                    .getShippingDetail()
                    .getShippingAddress()
                    .getCountry());
        }

    }

    private String getShopperLocale(ATGAuthRequest atgAuthRequest) {
        String shopperLocale = atgAuthRequest.getShopperLocale();

        if (StringUtils.isBlank(shopperLocale)) {
            shopperLocale = paymentsConfig.getDefaultOxxoShopperLocale();
        }

        return shopperLocale;
    }

    private Map<String, String> getMetaData(ATGAuthRequest request) {
        Map<String, String> metaData = new HashMap<>();

        if (StringUtils.isNotBlank(request.getWebStoreId())) {
            metaData.put(PaymentsConstants.WEB_STORE_ID, request.getWebStoreId());
        }

        return metaData;
    }

    private MerchantRiskIndicator getMerchantRiskIndicator(ATGAuthRequest request) {

        if (request.getAdyenFraudDetail()
                .getShippingDetail() == null
                || StringUtils.isBlank(request.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingEmail())) {
            return null;
        }

        MerchantRiskIndicator merchantRiskIndicator = new MerchantRiskIndicator();

        merchantRiskIndicator.setDeliveryEmail(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingEmail());

        return merchantRiskIndicator;

    }

    private ShopperName getShopperName(ATGAuthRequest request) {
        ShopperName shopperName = new ShopperName();

        if (request.getAdyenFraudDetail()
                .getShippingDetail() == null
                || request.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingAddress() == null) {
            return shopperName;
        }

        shopperName.setFirstName(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getFirstName());
        shopperName.setLastName(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getLastName());

        return shopperName;
    }

    private DeliveryAddress getDeliveryAddress(ATGAuthRequest request) {

        log.debug("AuthRequestResponseMapper.getDeliveryAddress() :: START");

        if (request.getAdyenFraudDetail()
                .getShippingDetail() == null
                || request.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingAddress() == null) {
            return null;
        }

        DeliveryAddress deliveryAddress = new DeliveryAddress();

        if (StringUtils.isNotBlank(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getAddress2())) {
            deliveryAddress.setHouseNumberOrName(request.getAdyenFraudDetail()
                    .getShippingDetail()
                    .getShippingAddress()
                    .getAddress2());
        } else {
            deliveryAddress.setHouseNumberOrName(EMPTY_STR);
        }

        deliveryAddress.setStreet(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getAddress1());
        deliveryAddress.setCity(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getCity());
        deliveryAddress.setStateOrProvince(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getState());
        deliveryAddress.setCountry(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getCountry());
        deliveryAddress.setPostalCode(request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress()
                .getPostalCode());

        log.debug("AuthRequestResponseMapper.getDeliveryAddress() :: END");

        return deliveryAddress;
    }

    /**
     * Added for PAYMS-60 This method will be used to return the reference as order number and payment id combination.
     * 
     * @param atgAuthRequest - ATGAuthRequest Object
     * @return paymentGroupId - null or combination of order and payment id.
     */
    private String getReference(ATGAuthRequest atgAuthRequest) {
        String paymentGroupId = atgAuthRequest.getPaymentGroupId();

        if (StringUtils.isNotBlank(atgAuthRequest.getOrderNumber())) {
            paymentGroupId = atgAuthRequest.getOrderNumber() + PaymentsConstants.HYPEN + paymentGroupId;
        }

        return paymentGroupId;
    }

    private String getPaymentGroupId(ATGAuthRequest atgAuthRequest) {
        String paymentGroupId = atgAuthRequest.getPaymentGroupId();

        if (StringUtils.isBlank(paymentGroupId)) {
            SimpleDateFormat format = new SimpleDateFormat(PaymentsConstants.DATE_FORMAT_HOUR_MIN_SEC_MILL);
            paymentGroupId = PaymentsConstants.REFRENCE_PREFIX_CC + format.format(new java.util.Date());
        }

        return paymentGroupId;
    }

    public ATGAuthResponse convertToATGAuthResponse(AdyenAuthResponse adyenAuthResponse, ATGAuthRequest atgAuthRequest) {

        log.debug("AuthRequestResponseMapper.convertToATGAuthResponse() :: START");

        if (adyenAuthResponse == null) {
            return null;
        }

        ATGAuthResponse atgAuthResponse = new ATGAuthResponse();

        if (adyenAuthResponse.getAdditionalData() != null) {
            atgAuthResponse.setAdditionalData(commonUtils.getAtgAdditionalData(adyenAuthResponse.getAdditionalData(), atgAuthRequest));
        }

        if (adyenAuthResponse.getFraudResult() != null) {
            atgAuthResponse.setFraudResult(getFraudResult(adyenAuthResponse));
        }

        atgAuthResponse.setPspReference(adyenAuthResponse.getPspReference());

        if (Objects.nonNull(atgAuthResponse.getAdditionalData())
                && commonUtils.checkSaleTransactionPaymentMethod(atgAuthResponse.getAdditionalData()
                        .getCreditCardType())) {
            atgAuthResponse.setResultCode(getSaleTransactionResultCode(adyenAuthResponse.getResultCode()));
        } else {
            atgAuthResponse.setResultCode(mappedResultCode(adyenAuthResponse.getResultCode()));
        }

        if (commonUtils.validateAuthTokenAndUpdateResultCode(atgAuthRequest.getOrderNumber(), atgAuthRequest.getPaymentMethod()
                .getType(), atgAuthResponse.getAdditionalData(), atgAuthResponse.getResultCode())) {
            atgAuthResponse.setResultCode(PaymentsConstants.AUTH_PENDING);
        }

        atgAuthResponse.setAmount(adyenAuthResponse.getAmount());
        atgAuthResponse.setMerchantReference(adyenAuthResponse.getMerchantReference());
        atgAuthResponse.setRefusalReason(adyenAuthResponse.getRefusalReason());
        atgAuthResponse.setRefusalReasonCode(adyenAuthResponse.getRefusalReasonCode());
        // Added for paypal and 3DS
        atgAuthResponse.setAction(adyenAuthResponse.getAction());
        atgAuthResponse.setDetails(adyenAuthResponse.getDetails());
        atgAuthResponse.setTransactionTimestamp(LocalDateTime.now()
                .toString());

        log.debug("AuthRequestResponseMapper.convertToATGAuthResponse() {} : {}", v(CommonKeys.ORDER_NUMBER.key(), atgAuthRequest.getOrderNumber()),
                new Gson().toJson(atgAuthResponse, ATGAuthResponse.class));

        log.debug("AuthRequestResponseMapper.convertToATGAuthResponse() :: END");

        return atgAuthResponse;
    }

    private String mappedResultCode(String resultCode) {
        return paymentsConfig.getResultCodeMap()
                .getOrDefault(resultCode, PaymentsConstants.DECLINED);
    }

    private String getSaleTransactionResultCode(String resultCode) {
        String saleTransactionResultCode = mappedResultCode(resultCode);
        return PaymentsConstants.AUTHORIZED.equalsIgnoreCase(saleTransactionResultCode) ? PaymentsConstants.SETTLED : saleTransactionResultCode;
    }

    private String getMerchantAccount(ATGAuthRequest atgAuthRequest) {
        return paymentsConfig.getMerchantAccounts()
                .getOrDefault(atgAuthRequest.getWebStoreId(), atgAuthRequest.getWebStoreId());
    }

    private FraudResult getFraudResult(AdyenAuthResponse adyenAuthResponse) {
        FraudResult fraudResult = new FraudResult();
        fraudResult.setAccountScore(adyenAuthResponse.getFraudResult()
                .getAccountScore());
        return fraudResult;
    }

    private PaymentMethod getPaymentDetails(ATGAuthRequest atgAuthRequest) {

        log.debug("AuthRequestResponseMapper.getPaymentDetails() :: START");

        PaymentMethod paymentMethod = new PaymentMethod();

        if (PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                .getType())) {

            paymentMethod.setEncryptedCardNumber(atgAuthRequest.getPaymentMethod()
                    .getEncryptedCardNumber());
            paymentMethod.setEncryptedExpiryMonth(atgAuthRequest.getPaymentMethod()
                    .getEncryptedExpiryMonth());
            paymentMethod.setEncryptedExpiryYear(atgAuthRequest.getPaymentMethod()
                    .getEncryptedExpiryYear());
            paymentMethod.setEncryptedSecurityCode(atgAuthRequest.getPaymentMethod()
                    .getEncryptedSecurityCode());

            String holderName = null;
            if (atgAuthRequest.getBillingAddress() != null
                    && StringUtils.isNotBlank(atgAuthRequest.getBillingAddress()
                            .getFirstName())) {
                holderName = atgAuthRequest.getBillingAddress()
                        .getFirstName();

                if (StringUtils.isNotBlank(atgAuthRequest.getBillingAddress()
                        .getLastName())) {
                    holderName += StringUtils.SPACE + atgAuthRequest.getBillingAddress()
                            .getLastName();
                }
            }
            paymentMethod.setHolderName(holderName);

        } else if (PaymentsConstants.GIFT_CARD.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                .getType())) {

            paymentMethod.setCvc(atgAuthRequest.getPaymentMethod()
                    .getGiftCardPIN());
            paymentMethod.setNumber(atgAuthRequest.getPaymentMethod()
                    .getGiftCardNumber());

        } else if (PaymentsConstants.PAYPAL_PAYMENT_METHOD.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                .getType())) {
            paymentMethod.setSubtype(atgAuthRequest.getPaymentMethod()
                    .getSubtype());

        } else if (PaymentsConstants.APPLE_PAY.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                .getType())) {
            paymentMethod.setApplePayToken(atgAuthRequest.getPaymentMethod()
                    .getApplePayToken());
        }

        paymentMethod.setStoredPaymentMethodId(atgAuthRequest.getPaymentMethod()
                .getStoredPaymentMethodId());

        paymentMethod.setType(paymentsConfig.getPaymentMethodType()
                .getOrDefault(atgAuthRequest.getPaymentMethod()
                        .getType()
                        .toLowerCase(),
                        atgAuthRequest.getPaymentMethod()
                                .getType()));

        log.debug("AuthRequestResponseMapper.getPaymentDetails() :: END");

        return paymentMethod;
    }

    private BillingAddress getBillingAddress(ATGAuthRequest atgAuthRequest) {

        log.debug("AuthRequestResponseMapper.getBillingAddress() :: START");

        BillingAddress billingAddress = null;

        if (StringUtils.isNotBlank(atgAuthRequest.getBillingAddress()
                .getCity())
                && StringUtils.isNotBlank(atgAuthRequest.getBillingAddress()
                        .getCountry())
                && StringUtils.isNotBlank(atgAuthRequest.getBillingAddress()
                        .getAddress1())) {
            billingAddress = new BillingAddress();
            billingAddress.setCity(atgAuthRequest.getBillingAddress()
                    .getCity());
            billingAddress.setCountry(atgAuthRequest.getBillingAddress()
                    .getCountry());

            if (!skipSendingAddress2ForAuth(atgAuthRequest)) {
                billingAddress.setHouseNumberOrName(atgAuthRequest.getBillingAddress()
                        .getAddress2());
            } else {
                billingAddress.setHouseNumberOrName(PaymentsConstants.EMPTY_STRING);
            }

            billingAddress.setPostalCode(atgAuthRequest.getBillingAddress()
                    .getPostalCode());
            billingAddress.setStateOrProvince(atgAuthRequest.getBillingAddress()
                    .getState());
            billingAddress.setStreet(atgAuthRequest.getBillingAddress()
                    .getAddress1());
        }
        log.debug("AuthRequestResponseMapper.getBillingAddress() :: END");

        return billingAddress;
    }

    protected boolean skipSendingAddress2ForAuth(ATGAuthRequest atgAuthRequest) {

        log.debug("AuthRequestResponseMapper.skipSendingAddress2ForAuth() :: START");

        var paymentTypes = paymentsConfig.getSkipAddress2ForAuth()
                .get(atgAuthRequest.getWebStoreId());
        boolean isValidPaymentMethod = false;

        if (paymentTypes != null
                && !paymentTypes.isEmpty()
                && atgAuthRequest.getPaymentMethod() != null) {
            isValidPaymentMethod = paymentTypes.stream()
                    .anyMatch(s -> s.equalsIgnoreCase(atgAuthRequest.getPaymentMethod()
                            .getType()));
        }

        log.debug("AuthRequestResponseMapper.skipSendingAddress2ForAuth() :: END");
        return isValidPaymentMethod;
    }

    private BrowserInfo getBrowserInfo(ATGAuthRequest atgAuthRequest) {

        log.debug("AuthRequestResponseMapper.getBrowserInfo() :: START");

        BrowserInfo browserInfo = null;

        if (atgAuthRequest.getBrowserInfo() != null) {
            BrowserInfo atgBrowserInfo = atgAuthRequest.getBrowserInfo();
            browserInfo = new BrowserInfo();
            browserInfo.setUserAgent(atgBrowserInfo.getUserAgent());
            browserInfo.setAcceptHeader(atgBrowserInfo.getAcceptHeader());
            browserInfo.setLanguage(atgBrowserInfo.getLanguage());
            browserInfo.setColorDepth(atgBrowserInfo.getColorDepth());
            browserInfo.setScreenHeight(atgBrowserInfo.getScreenHeight());
            browserInfo.setScreenWidth(atgBrowserInfo.getScreenWidth());
            browserInfo.setTimeZoneOffset(atgBrowserInfo.getTimeZoneOffset());
            browserInfo.setJavaEnabled(atgBrowserInfo.isJavaEnabled());
        }

        log.debug("AuthRequestResponseMapper.getBrowserInfo() :: END");

        return browserInfo;
    }

    private String getAdyenChannelType(String channelType) {
        return paymentsConfig.getChannelType()
                .getOrDefault(channelType, PaymentsConstants.WEB);
    }

    public ATGAuthResponse buildAuthResponseFromHeader(Optional<PaymentHeaderDTO> paymentHeaderDTO, PaymentEventDTO paymentEvent) {

        log.debug("AuthRequestResponseMapper.buildAuthResponseFromHeader() :: START");

        var atgAuthResponse = new ATGAuthResponse();

        if (paymentHeaderDTO.isEmpty()) {
            return atgAuthResponse;
        }

        var paymentHeader = paymentHeaderDTO.get();
        atgAuthResponse.setAdditionalData(getAtgAdditionalData(paymentHeader, paymentEvent));
        atgAuthResponse.setPspReference(paymentEvent.getPaymentReference());
        atgAuthResponse.setResultCode(paymentHeader.getState());
        atgAuthResponse.setTransactionTimestamp(null != paymentEvent.getSubmittedDate() ? paymentEvent.getSubmittedDate()
                .toString() : null);
        var amount = new Amount();
        amount.setCurrency(paymentHeader.getCurrencyCode());
        amount.setValue(CommonUtils.convertDollarsInToCents(paymentEvent.getAmount()
                .doubleValue()));
        atgAuthResponse.setAmount(amount);
        atgAuthResponse.setMerchantReference(paymentEvent.getMerchantReference());
        atgAuthResponse.setBillingAddress(getBillingAddress(paymentHeader.getBillingAddress()));

        log.info("AuthRequestResponseMapper.buildAuthResponseFromHeader() {} : {}", v(CommonKeys.ORDER_NUMBER.key(), paymentHeaderDTO.get()
                .getOrderNumber()), new Gson().toJson(atgAuthResponse, ATGAuthResponse.class));

        log.debug("AuthRequestResponseMapper.buildAuthResponseFromHeader() :: END");

        return atgAuthResponse;
    }

    private com.ecomm.payments.model.BillingAddress getBillingAddress(BillingAddressDTO billingAddress) {

        return com.ecomm.payments.model.BillingAddress.builder()
                .firstName(billingAddress.getFirstName())
                .lastName(billingAddress.getLastName())
                .address1(billingAddress.getAddress1())
                .address2(billingAddress.getAddress2())
                .city(billingAddress.getCity())
                .state(billingAddress.getState())
                .country(billingAddress.getCountry())
                .email(billingAddress.getEmail())
                .phoneNumber(billingAddress.getPhoneNumber())
                .postalCode(billingAddress.getPostalCode())
                .email(billingAddress.getEmail())
                .build();

    }

    private ATGAdditionalData getAtgAdditionalData(PaymentHeaderDTO paymentHeader, PaymentEventDTO paymentEvent) {

        log.debug("AuthRequestResponseMapper.getAtgAdditionalData() :: START");

        var atgAdditionalData = new ATGAdditionalData();

        if (null != paymentEvent.getEventDetails()) {
            var eventDetails = paymentEvent.getEventDetails();
            atgAdditionalData.setAuthCode(eventDetails.getAuthCode());
            atgAdditionalData.setAvsResult(eventDetails.getAvsResponseCode());
            atgAdditionalData.setAvsResultRaw(eventDetails.getAvsResponseRawCode());
            atgAdditionalData.setCvvResponseCode(eventDetails.getCvvResponseCode());
        }

        if (null != paymentHeader.getPaymentDetails()) {
            var paymentDetails = paymentHeader.getPaymentDetails();

            if (PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(paymentHeader.getPaymentMethod())) {
                atgAdditionalData.setCreditCardType(paymentDetails.getCardType());
                atgAdditionalData.setMaskedCardNumber(paymentDetails.getCardNumber());
                atgAdditionalData.setAuthToken(paymentDetails.getAuthToken());
                atgAdditionalData.setExpirationMonth(paymentDetails.getExpirationMonth());
                atgAdditionalData.setExpirationYear(paymentDetails.getExpirationYear());
                atgAdditionalData.setCardAlias(paymentDetails.getCardAlias());
            }

            if (PaymentsConstants.PAYPAL_PAYMENT_METHOD.equalsIgnoreCase(paymentHeader.getPaymentMethod())) {
                atgAdditionalData.setPaypalEmail(paymentDetails.getPayerEmail());
                atgAdditionalData.setPaypalPayerId(paymentDetails.getPayerId());
                atgAdditionalData.setPaypalPayerStatus(paymentDetails.getPayerStatus());
            }

            if (PaymentsConstants.GIFT_CARD.equalsIgnoreCase(paymentHeader.getPaymentMethod())) {
                atgAdditionalData.setMaskedCardNumber(paymentDetails.getCardNumber());
            }
            atgAdditionalData.setFundingSource(paymentDetails.getFundingSource());

        }

        atgAdditionalData.setPaymentMethod(paymentHeader.getPaymentMethod());
        atgAdditionalData.setProfileId(paymentHeader.getProfileId());

        log.debug("AuthRequestResponseMapper.getAtgAdditionalData() :: END");

        return atgAdditionalData;
    }

    public ATGAuthResponse buildAplazoDeclinedResponse() {

        log.debug("AuthRequestResponseMapper.buildAuthDeclinedResponse() :: START");

        var authResponse = new ATGAuthResponse();
        authResponse.setResultCode(PaymentsConstants.DECLINED);
        authResponse.setTransactionTimestamp(LocalDateTime.now()
                .toString());

        log.debug("AuthRequestResponseMapper.buildAuthDeclinedResponse() :: END");

        return authResponse;
    }

}
