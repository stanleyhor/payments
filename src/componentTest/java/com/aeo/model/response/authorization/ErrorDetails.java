package com.aeo.model.response.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    private String message;
    private int status;
    private String errorCode;
    private String errorType;
    private String source;
    private String paymentHeaderId;
    private String paymentMethod;

}
