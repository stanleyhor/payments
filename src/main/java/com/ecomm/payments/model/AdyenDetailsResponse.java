package com.ecomm.payments.model;

import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.FraudResult;

import lombok.Data;

@Data
public class AdyenDetailsResponse {

    private AdyenAdditionalData additionalData;
    private String pspReference;
    private String resultCode;
    private Amount amount;
    private String merchantOrderReference;
    private String merchantReference;
    private FraudResult fraudResult;
    private String refusalReason;
    private String refusalReasonCode;

}