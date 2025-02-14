package com.ecomm.payments.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.ecomm.payments.service.IntlPaymentAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

class IntlPaymentAuthReceiverTest {

    IntlPaymentAuthReceiver receiver;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        receiver = new IntlPaymentAuthReceiver(new ObjectMapper(), new IntlPaymentAuthService(null));
    }

    @Test
    void testSettings() {
        assertFalse(receiver.ackOnException());
        assertFalse(receiver.throwExceptions());
        assertFalse(receiver.ackOnProcessFailure());
    }

}
