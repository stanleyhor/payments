package com.ecomm.payments.model;

import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PaymentStatus {

    private String paymentGroupId;
    private String paymentType;
    private String resultCode;
    private String currencyCode;
    private long amountAuthorized;
    private String pspReference;
    private String idempotencyKey;
    private boolean fraudManualReview;
    private AtgBillingAddress billingAddress;
    private ATGAdditionalData additionalData;

    private String afterpayEventId;
    private String afterpayToken;
    private String afterpayAuthCreatedDate;
    private String afterpayAuthExpiryDate;
    private String redirectCheckoutUrl;
    private double afterpayInstallment;

}
