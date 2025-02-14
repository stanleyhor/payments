package com.aeo.constants;

import lombok.Getter;

public enum CheckedPaymentMethods {

    CREDIT_CARD("authCreditCartRequest.json", "authCreditCartResponse.json"),
    CREDIT_CARD_3DS("auth3dsCreditCartRequest.json", "auth3dsCreditCartResponse.json"),
    PAYPAL("authPayPalRequest.json", "authPayPalResponse.json"),
    APPLEPAY("authApplePayRequest.json", "authApplePayResponse.json"),
    APPLEPAY_INTERAC("authApplePayInteracRequest.json", "authApplePayInteracResponse.json"),
    SYNCHRONY_CREDIT_CARD("authSynchronyCCRequest.json", "authSynchronyCCResponse.json");

    @Getter
    private final String pathToRequest;
    @Getter
    private final String pathToResponse;

    CheckedPaymentMethods(String pathToRequest, String pathToResponse) {
        this.pathToRequest = String.format("%s%s", Constants.REQUESTS_DIRECTORY, pathToRequest);
        this.pathToResponse = String.format("%s%s", Constants.RESPONSES_DIRECTORY, pathToResponse);
    }

}
