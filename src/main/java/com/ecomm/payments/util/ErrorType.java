package com.ecomm.payments.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorType {

    public static final String AUTH_FAILED = "error.checkout.placeOrder.paymentInfo.authorizationFailed";
    public static final String VALIDATION_FAILED = "error.checkout.placeOrder.paymentInfo.missingInformation";
    public static final String INTERNAL_SERVER_ERROR = "error.checkout.placeOrder.paymentInfo.internalException";
    public static final String DUPLICATE_AUTHORIZATION_FAILED = "error.checkout.placeOrder.duplicate.authorizationFailed";

}
