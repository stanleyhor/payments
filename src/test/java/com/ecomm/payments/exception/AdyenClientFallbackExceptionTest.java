package com.ecomm.payments.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdyenClientFallbackExceptionTest {

    @Test
    void testExpectedExceptionWithSuperType() {

        Assertions.assertThrows(Exception.class, () -> {
            throw new AdyenClientFallbackException("message");
        });
    }

    @Test
    void testExpectedException() {

        Assertions.assertThrows(AdyenClientException.class, () -> {
            throw new AdyenClientException("message");
        });
    }

    @Test
    void testExpectedAuthFailedException() {

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            throw new AuthorizationFailedException("message");
        });
    }

}
