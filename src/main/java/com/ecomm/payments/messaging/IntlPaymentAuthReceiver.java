package com.ecomm.payments.messaging;

import com.aeo.gcp.pubsub.subscriber.AeoMessageReceiver;

import com.ecomm.payments.model.IntlPaymentAuthMessage;
import com.ecomm.payments.service.IntlPaymentAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class IntlPaymentAuthReceiver extends AeoMessageReceiver<IntlPaymentAuthMessage> {

    public IntlPaymentAuthReceiver(ObjectMapper jackson, IntlPaymentAuthService intlPaymentAuthService) {
        super(jackson, intlPaymentAuthService);
    }

    @Override
    public boolean ackOnException() {
        return false;
    }

    @Override
    public boolean throwExceptions() {
        return false;
    }

    @Override
    public boolean ackOnProcessFailure() {
        return false;
    }

}
