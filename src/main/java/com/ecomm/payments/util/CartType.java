package com.ecomm.payments.util;

import com.ecomm.payments.constants.PaymentsConstants;

public enum CartType {

    VGC(PaymentsConstants.CART_TYPE_VGC),
    CHARITY(PaymentsConstants.CART_TYPE_REGULAR),
    REGULAR(PaymentsConstants.CART_TYPE_REGULAR),
    GWP(PaymentsConstants.CART_TYPE_REGULAR),
    PGC(PaymentsConstants.CART_TYPE_REGULAR),
    JACRON(PaymentsConstants.CART_TYPE_REGULAR);

    public final String type;

    private CartType(String type) {
        this.type = type;
    }

}