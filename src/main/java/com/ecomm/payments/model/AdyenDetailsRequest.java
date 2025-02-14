package com.ecomm.payments.model;

import lombok.Data;

@Data
public class AdyenDetailsRequest {

    private String paymentData;
    private Details details;

}
