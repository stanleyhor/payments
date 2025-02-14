package com.ecomm.payments.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CustomErrorResponse {

    private String message;
    private Integer status;
    private String errorCode;
    private String errorType;

}
