package com.ecomm.payments.model.afterpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    private String redirectConfirmUrl;
    private String redirectCancelUrl;
    private String popupOriginUrl;

}
