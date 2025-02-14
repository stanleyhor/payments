package com.aeo.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class FraudDetail {

    private BrowserInfo browserInfo;
    private int itemCount;
    private int totalItems;
    private String customerIp;
    private String deviceFingerPrint;
    private String couponCode;

}
