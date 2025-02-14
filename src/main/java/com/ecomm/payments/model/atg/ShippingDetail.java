package com.ecomm.payments.model.atg;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ShippingDetail {

    private ShippingAddress shippingAddress;
    private String shippingMethod;
    private String shippingEmail;
    private double shippingAmount;

}
