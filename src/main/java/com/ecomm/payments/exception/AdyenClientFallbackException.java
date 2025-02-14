package com.ecomm.payments.exception;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdyenClientFallbackException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

}
