package com.ecomm.payments.model.atg;

import lombok.Data;

@Data
public class AtgPaymentMethod {

    private String type;
    private String encryptedCardNumber;
    private String encryptedExpiryMonth;
    private String encryptedExpiryYear;
    private String encryptedSecurityCode;
    private String storedPaymentMethodId;
    private String giftCardNumber;
    private String giftCardPIN;
    private String subtype;
    private String applePayToken;

}
