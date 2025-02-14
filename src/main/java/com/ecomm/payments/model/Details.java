package com.ecomm.payments.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Details {

    @JsonProperty("MD")
    private String md;
    @JsonProperty("PaRes")
    private String paRes;
    private String redirectResult;
    private String billingToken;
    private String facilitatorAccessToken;
    private String orderID;
    private String payerID;
    private String paymentID;
    private String payload;
    private String afterpayToken;

}
