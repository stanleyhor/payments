package com.ecomm.payments.model.adyen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AdyenAdditionalData {

    private String authCode;
    private String avsResult;
    private String avsResultRaw;
    private String cardSummary;
    private String cardIssuingBank;
    private String expiryDate;
    private String cvcResult;
    private String cardBin;
    @JsonProperty("recurring.recurringDetailReference")
    private String recurringDetailReference;
    private String recurringProcessingModel;
    private String paymentMethod;
    @JsonProperty("recurring.shopperReference")
    private String shopperReference;
    private String cardPaymentMethod;
    private String cardIssuingCountry;
    private String fraudResultType;
    private String fraudManualReview;
    // for paypal
    private String paypalEmail;
    private String paypalPayerId;
    private String paypalPayerResidenceCountry;
    private String paypalPayerStatus;
    private String paypalProtectionEligibility;
    private BillingAddress billingAddress;
    // for 3DS
    private String scaExemptionRequested;
    private String merchantReference;
    private String authorisedAmountCurrency;
    private String authorisedAmountValue;
    private String alias;
    @JsonProperty("PaymentAccountReference")
    private String paymentAccountReference;
    private String issuerBin;
    private String fundingSource;
    private String cardSchemeEnhancedDataLevel;
    private String coBrandedWith;

}
