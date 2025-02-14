package com.ecomm.payments.model.adyen;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class Data {

    @JsonProperty("MD")
    private String md;
    @JsonProperty("PaReq")
    private String paReq;
    @JsonProperty("TermUrl")
    private String termUrl;

}
