package com.aeo.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrderSummary {

    private String orderNumber;
    private String cartId;
    private String currency;
    private String giftCardTotal;
    private double amountToBeAuthorized;
    private double orderTotal;
    private String channelType;
    private String returnUrl;
    private String profileId;
    private String orderDiscountAmount;
    private long daysSinceRegistration;
    private String checkoutLocale;
    private double taxTotal;
    private double shippingTotal;
    private boolean cartContainsOnlyVirtualItems;
    private String checkoutType;
    private double afterpayInstallment;

}
