package com.ecomm.payments.model.atg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ATGAdditionalData {

    private String authCode;
    private String avsResult;
    private String avsResultRaw;
    private String maskedCardNumber;
    private String cvvResponseCode;
    private String authToken;
    private String paymentMethod;
    private String profileId;
    private String creditCardType;
    private String expirationMonth;
    private String expirationYear;
    private String fraudResultType;
    private String fraudManualReview;
    // for paypal
    private String paypalEmail;
    private String paypalPayerId;
    private String paypalPayerResidenceCountry;
    private String paypalPayerStatus;
    private String paypalProtectionEligibility;
    private int fraudScore;
    // for 3DS
    private String scaExemptionRequested;
    private String cardAlias;
    private String paymentAccountReference;
    private String cardPaymentMethod;
    private String fundingSource;

}
