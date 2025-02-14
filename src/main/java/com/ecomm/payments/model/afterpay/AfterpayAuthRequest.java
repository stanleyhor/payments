package com.ecomm.payments.model.afterpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterpayAuthRequest {

    private String requestId;
    private String token;
    private String merchantReference;
    private AfterpayAmount amount;

}
