package com.ecomm.payments.model.afterpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterpayAddress {

    private String name;
    private String line1;
    private String line2;
    private String area1;
    private String area2;
    private String region;
    private String postcode;
    private String countryCode;
    private String phoneNumber;

}
