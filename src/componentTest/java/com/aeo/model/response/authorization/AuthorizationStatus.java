package com.aeo.model.response.authorization;

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
@JsonInclude(Include.NON_NULL)
public class AuthorizationStatus {

    private String authCode;
    private String paymentStatus;
    private String cvvResponse;
    private String cvvDescriptiveResult;
    private String avsCode;
    private String avsDescriptiveResult;
    private String pspReference;
    private String transactionId;
    private String transactionTimestamp;
    private String resultCode;
    private String type;
    private String alternativeRefNumber;
    private String voucherUrl;
    private String voucherExpireDate;
    private String voucherReferenceNumber;
    private String merchantReference;
    private String instructionsUrl;
    private String merchantName;
    private String loanId;

}
