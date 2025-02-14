package com.ecomm.payments.model.adyen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PaymentMethod {

    private String type;
    private String encryptedCardNumber;
    private String encryptedExpiryMonth;
    private String encryptedExpiryYear;
    private String encryptedSecurityCode;
    private String holderName;
    private String storedPaymentMethodId;
    private String number;
    private String cvc;
    private String subtype;
    private String applePayToken;

}
