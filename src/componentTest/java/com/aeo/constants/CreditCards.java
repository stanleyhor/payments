package com.aeo.constants;

import lombok.Getter;

public enum CreditCards {

    VISA("visa.json"),
    MASTERCARD("mastercard.json"),
    DISCOVER("discover.json"),
    DINERS("diners.json"),
    JCB("jcb.json"),
    AMERICAN_EXPRESS("amex.json"),
    VISA_3DS("3ds_visa.json"),
    MASTERCARD_3DS("3ds_mastercard.json"),
    AMERICAN_EXPRESS_3DS("3ds_amex.json"),
    AECREDITCARD("aecreditcard.json"),
    AEVISA("aevisa.json");

    @Getter
    private final String fileName;

    CreditCards(String fileName) {
        this.fileName = String.format("%s%s", Constants.PAYMENT_METHODS_DIRECTORY, fileName);
    }

}
