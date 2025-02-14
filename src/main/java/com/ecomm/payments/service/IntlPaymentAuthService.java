package com.ecomm.payments.service;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.gcp.pubsub.subscriber.AeoMessageProcessor;

import com.ecomm.payments.model.IntlPaymentAuthMessage;
import com.ecomm.payments.util.TransactionalDataMapper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntlPaymentAuthService implements AeoMessageProcessor<IntlPaymentAuthMessage> {

    private final TransactionalDataMapper transactionalDataMapper;

    @Override
    public boolean processMessage(IntlPaymentAuthMessage payload) {

        log.info("IntlPaymentAuthService.processMessage() :: START");

        boolean isUpdate = updateIntlPaymenDetails(payload);

        log.info("IntlPaymentAuthService.processMessage() Success {} :: END", isUpdate);

        return isUpdate;
    }

    public boolean updateIntlPaymenDetails(IntlPaymentAuthMessage message) {

        if (!Objects.nonNull(message)) {
            log.error("IntlPaymentAuthService.updateIntlPaymenDetails() :: IntlPaymentAuthMessage is null");
            throw new MessagingException("IntlPaymentAuthService.updateIntlPaymenDetails() IntlPaymentAuthMessage message is null");
        }

        if (message.getPaymentHeaders() != null
                && !message.getPaymentHeaders()
                        .isEmpty()
                && message.getPaymentHeaders()
                        .get(0)
                        .getPaymentHeaderId() != null) {

            log.info("IntlPaymentAuthService.updateIntlPaymenDetails() :: orderNumber: {} :: paymentMethod: {}", v("ORDERNUMBER", message.getOrderNumber()),
                    v("PAYMENT_METHOD", message.getPaymentHeaders()
                            .get(0)
                            .getPaymentVariation()));

            log.debug("IntlPaymentAuthService.updateIntlPaymenDetails() :: orderNumber: {} :: paymentMethod: {}, IntlPayment message received: {}",
                    v("ORDERNUMBER", message.getOrderNumber()), v("PAYMENT_METHOD", message.getPaymentHeaders()
                            .get(0)
                            .getPaymentVariation()),
                    message);

            transactionalDataMapper.storeIntlAuthData(message);

        } else {
            log.error("TransactionalDataMapper.updateIntlPaymenDetails() failed for {}", message);
            throw new MessagingException("TransactionalDataMapper.updateIntlPaymenDetails() failed");
        }

        return true;
    }

}
