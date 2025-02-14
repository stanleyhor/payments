package com.ecomm.payments.model.braintree;

import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.atg.RequestContext;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class AuthorizationRequest {

    private String orderNumber;

    private String paymentHeaderId;

    private String profileId;

    private String idempotencyKey;

    private PaymentMethod paymentMethod;

    private Amount amount;

    private String checkoutType;

    private RequestContext context;

    private Address shippingAddress;

    private Address billingAddress;

    @Data
    @ToString
    public static class PaymentMethod {

        private String type;

        private String paypalToken;

    }

    private String deviceData;

}