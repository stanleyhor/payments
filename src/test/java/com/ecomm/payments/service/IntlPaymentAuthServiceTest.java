package com.ecomm.payments.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecomm.payments.model.IntlPaymentAuthMessage;
import com.ecomm.payments.util.TestUtil;
import com.ecomm.payments.util.TransactionalDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.MessagingException;

import java.nio.file.Paths;

public class IntlPaymentAuthServiceTest {

    IntlPaymentAuthService aplazoAuthService;

    @Mock
    private TransactionalDataMapper transactionalDataMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        aplazoAuthService = new IntlPaymentAuthService(transactionalDataMapper);
    }

    @Test
    void testAplazoAuthMessage() {

        assertDoesNotThrow(() -> aplazoAuthService.processMessage(mockMessage()));

        assertThrows(MessagingException.class, () -> aplazoAuthService.processMessage(null));

        assertDoesNotThrow(() -> aplazoAuthService.processMessage(mockMessageError("InvlaidAplazoResponse.json")));

        assertThrows(MessagingException.class, () -> aplazoAuthService.processMessage(mockMessageError("InvlaidAplazoResponseWithOutPaymentHeaders.json")));

        assertThrows(MessagingException.class, () -> aplazoAuthService.processMessage(mockMessageError("InvlaidAplazoResponseWithOutPaymentHeaders.json")));

        assertThrows(MessagingException.class, () -> aplazoAuthService.processMessage(mockMessageError("InvlaidAplazoResponseEmptyPaymentHeaders.json")));

        assertThrows(MessagingException.class, () -> aplazoAuthService.processMessage(mockMessageError("InvlaidAplazoResponse2.json")));
    }

    public IntlPaymentAuthMessage mockMessage() {
        String path = Paths.get("payload", "IntlPaymentAuthService", "AplazoResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, IntlPaymentAuthMessage.class);
    }

    public IntlPaymentAuthMessage mockMessageError(String message) {
        String path = Paths.get("payload", "IntlPaymentAuthService", message)
                .toString();
        return TestUtil.getResponseFromJsonPath(path, IntlPaymentAuthMessage.class);
    }

}
