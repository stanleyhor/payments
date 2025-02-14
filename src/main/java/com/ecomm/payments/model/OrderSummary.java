package com.ecomm.payments.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {

    private String checkoutType;
    private double orderTotal;
    private double amountToBeAuthorized;
    private String currency;
    private String channelType;
    private double afterpayInstallment;

}
