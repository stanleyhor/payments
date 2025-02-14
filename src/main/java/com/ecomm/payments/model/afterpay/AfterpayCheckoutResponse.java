package com.ecomm.payments.model.afterpay;

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
@JsonInclude(Include.NON_EMPTY)
public class AfterpayCheckoutResponse {

    private String orderNumber;
    private String token;
    private String expires;
    private String redirectCheckoutUrl;
    private int httpStatusCode;
    private String errorCode;
    private String message;
    private String errorId;
    private String resultCode;

}
