package com.ecomm.payments.model.afterpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterpayCheckoutRequest {

    private AfterpayAmount amount;
    private Consumer consumer;
    private String merchantReference;
    private AfterpayAddress billing;
    private AfterpayAddress shipping;
    private List<Item> items;
    private List<Discount> discounts;
    private Merchant merchant;
    private AfterpayAmount taxAmount;
    private AfterpayAmount shippingAmount;
    private Courier courier;
    private String mode;
    @JsonProperty
    private boolean isCashAppPay;
    private String shippingOptionIdentifier;

}
