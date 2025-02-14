package com.ecomm.payments.model;

import java.util.List;

import lombok.Data;

@Data
public class IntlPaymentAuthMessage {

    private String orderNumber;
    private String checkoutLocale;
    private String cartId;
    private String profileId;
    private String siteId;
    private List<PaymentHeader> paymentHeaders;

}
