package com.aeo.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsAuthDBResponseInfo {

    private String paymentHeaderId;
    private String paymentVariation;
    private String paymentStatus;
    private String creditCardType;
    private String gatewayIndicator;
    private String sourceType;
    private String afterPayToken;

}
