package com.ecomm.payments.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecomm.payments.model.error.ClientErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientExceptionTest {

    @Test
    void testExpectedExceptionWithSuperType() {

        Assertions.assertThrows(Exception.class, () -> {
            throw new ClientException(new ClientErrorResponse());
        });

    }

    @Test
    void testExpectedClientException() {

        Assertions.assertThrows(ClientException.class, () -> {
            throw new ClientException(new ClientErrorResponse());
        });

    }

    @Test
    void getNullClientErrorResponse() {
        ClientException clientException = new ClientException(null);
        assertNull(clientException.getErrorResponse());
    }

    @Test
    void getNotNullClientErrorResponse() {
        ClientException clientException = new ClientException(new ClientErrorResponse());
        assertNotNull(clientException.getErrorResponse());
    }

}
