package com.ecomm.payments.model;

import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.BillingAddress;
import com.ecomm.payments.model.adyen.DeliveryAddress;
import com.ecomm.payments.model.adyen.Installments;
import com.ecomm.payments.model.adyen.MerchantRiskIndicator;
import com.ecomm.payments.model.adyen.PaymentMethod;
import com.ecomm.payments.model.adyen.ShopperName;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import lombok.Data;

@Data
public class AdyenAuthRequest {

    private String merchantAccount;
    private String reference;
    private String merchantOrderReference;
    private Amount amount;
    private PaymentMethod paymentMethod;
    private BillingAddress billingAddress;
    private boolean storePaymentMethod;
    private String recurringProcessingModel;
    private String shopperInteraction;
    private String shopperReference;
    private String returnUrl;
    private DeliveryAddress deliveryAddress;
    private ShopperName shopperName;
    private String shopperEmail;
    private String telephoneNumber;
    private String shopperIP;
    private String deviceFingerprint;
    private MerchantRiskIndicator merchantRiskIndicator;

    @JsonProperty("additionalData")
    private Map<String, String> additionalData;

    @JsonProperty("metadata")
    private Map<String, String> metadata;
    // Added for 3DS
    private BrowserInfo browserInfo;
    private String channel;
    private String origin;
    // Added for OXXO
    private String countryCode;
    // added for PAYMS-127
    private String shopperLocale;
    private Installments installments;

}
