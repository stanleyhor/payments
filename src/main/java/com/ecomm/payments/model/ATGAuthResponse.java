package com.ecomm.payments.model;

import com.ecomm.payments.model.adyen.Action;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.FraudResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ATGAuthResponse {

    private ATGAdditionalData additionalData;
    private FraudResult fraudResult;
    private String pspReference;
    private String resultCode;
    private Amount amount;
    private String merchantReference;
    private String merchantAccount;
    private String refusalReason;
    private String refusalReasonCode;
    // Added for paypal
    private Action action;
    private List<AuthResDetails> details;
    private String transactionTimestamp;
    private BillingAddress billingAddress;

}
