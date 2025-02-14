package com.ecomm.payments.exception;

import com.ecomm.payments.model.error.ClientErrorResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient ClientErrorResponse errorResponse;

}
