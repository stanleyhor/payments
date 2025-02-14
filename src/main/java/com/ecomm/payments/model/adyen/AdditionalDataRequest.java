package com.ecomm.payments.model.adyen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AdditionalDataRequest {

    @JsonProperty("RequestedTestAcquirerResponseCode")
    private String requestedTestAcquirerResponseCode;

}
