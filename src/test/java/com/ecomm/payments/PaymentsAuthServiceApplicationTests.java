package com.ecomm.payments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aeo.correlation.logging.LoggingCorrelationInterceptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ecomm.payments.repository.PaymentHeaderRepository;
import net.saliman.spring.request.correlation.support.RequestCorrelationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = PaymentsAuthServiceApplication.class)
@ActiveProfiles("test")
class PaymentsAuthServiceApplicationTests {

    @MockBean
    private PaymentHeaderRepository paymentHeaderRepository;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RequestCorrelationProperties requestCorrelationProperties;

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setup() {
        logger = (Logger) LoggerFactory.getLogger(LoggingCorrelationInterceptor.class);
        logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void teardown() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void healthPage() {
        URI uri = URI.create("http://localhost:"
                + randomServerPort
                + "/actuator/health/ping");
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        assertTrue(response.getStatusCode()
                .is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody()
                .contains("UP"));
        validateCorrelationLogging();
    }

    void validateCorrelationLogging() {
        // check the MDC map of the appender for the RID header
        assertNotNull(appender.list);
        assertFalse(appender.list.isEmpty());
        assertNotNull(appender.list.getFirst());
        assertFalse(appender.list.getFirst()
                .getMDCPropertyMap()
                .isEmpty());
        assertTrue(appender.list.getFirst()
                .getMDCPropertyMap()
                .containsKey(requestCorrelationProperties.getRequestHeaderName()));
        assertTrue(appender.list.getFirst()
                .getMDCPropertyMap()
                .containsKey(requestCorrelationProperties.getSessionHeaderName()));
    }

}
