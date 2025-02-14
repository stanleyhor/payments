package com.ecomm.payments.model.adyen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Action {

    private String paymentData;
    private String paymentMethodType;
    private String type;
    private SdkData sdkData;
    // For 3DS
    private String url;
    private String method;
    private com.ecomm.payments.model.adyen.Data data;
    // For OXXO
    private String alternativeReference;
    private String downloadUrl;
    private String expiresAt;
    private String reference;
    private Amount totalAmount;
    private String merchantReference;
    private String instructionsUrl;
    private String merchantName;

}
