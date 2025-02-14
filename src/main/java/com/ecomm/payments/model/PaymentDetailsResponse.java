package com.ecomm.payments.model;

import com.ecomm.payments.model.error.ErrorResponse;

import lombok.Data;

@Data
public class PaymentDetailsResponse {

    private ResponseData data;
    private ErrorResponse error;

}
