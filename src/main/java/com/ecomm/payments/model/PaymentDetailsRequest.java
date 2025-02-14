package com.ecomm.payments.model;

import lombok.Data;

@Data
public class PaymentDetailsRequest {

    private String paymentData;
    private Details details;
    private String cartId;
    private String orderNumber;
    private String profileId;
    private OrderSummary orderSummary;

}