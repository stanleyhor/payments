package com.ecomm.payments.model;

import com.ecomm.payments.model.adyen.Action;
import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.adyen.AdyenPaymentMethod;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.FraudResult;

import java.util.List;

import lombok.Data;

@Data
public class AdyenAuthResponse {

    private AdyenAdditionalData additionalData;
    private String pspReference;
    private String resultCode;
    private AdyenPaymentMethod paymentMethod;
    private Amount amount;
    private String merchantOrderReference;
    private String merchantReference;
    private FraudResult fraudResult;
    private String refusalReason;
    private String refusalReasonCode;
    // Added for paypal
    private Action action;
    private List<AuthResDetails> details;

    private int status;
    private String errorCode;
    private String message;
    private String errorType;

}
