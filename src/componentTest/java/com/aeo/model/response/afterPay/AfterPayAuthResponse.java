package com.aeo.model.response.afterPay;

import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.model.error.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AfterPayAuthResponse {

    @JsonProperty("data")
    private AfterpayCheckoutResponse data;

    @JsonProperty("error")
    private ErrorResponse error;

}
