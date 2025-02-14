package com.ecomm.payments.model.afterpay;

import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class AfterpayAuthResponse {

    private String id;
    private String token;
    private String status;
    private String created;
    private String paymentState;
    private String merchantReference;
    private List<AfterpayEvent> events;
    private String paymentEventMerchantReference;
    private double installment;
    private String errorCode;
    private String errorId;
    private String message;
    private int httpStatusCode;
    private AfterpayOrderDetails orderDetails;
    private AtgBillingAddress billingAddress;

}
