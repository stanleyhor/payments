package com.ecomm.payments.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdyenClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String message;

}
