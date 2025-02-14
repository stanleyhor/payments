package com.ecomm.payments.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHeader {

    private String paymentHeaderId;
    private String transactionId;
    private String state;
    private Integer loanId;
    private double amount;
    private String currencyCode;
    private String gatewayIndicator;
    private String paymentMethod;
    private String paymentVariation;
    private LocalDateTime submittedDate;
    private BillingAddress billingAddress;

}
