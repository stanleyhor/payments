package com.ecomm.payments.util;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.braintree.Address;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.PaypalAuthResponse;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.model.database.TransactionType;
import com.ecomm.payments.repository.PaymentHeaderRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BTPaypalRepoWrapper {

    private final PaymentHeaderRepository headerRepository;

    @Transactional
    public void storeTransactionalData(AuthorizationRequest authRequest, PaypalAuthResponse authResponse, String siteId) {

        PaymentHeaderDTO paymentHeader = null;

        var optPaymentHeader = headerRepository.findById(authRequest.getPaymentHeaderId());

        if (optPaymentHeader.isEmpty()) {
            paymentHeader = new PaymentHeaderDTO();
        } else {
            paymentHeader = optPaymentHeader.get();
        }

        populatePaymentHeader(authRequest, authResponse, paymentHeader, siteId);
        populatePaymentEvents(authRequest, authResponse, paymentHeader);
        populateBillingAddress(authRequest, paymentHeader);

        headerRepository.save(paymentHeader);

        log.info("BTPaypalRepoWrapper.storeTransactionalData() successfully for orderNumber: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()));
    }

    private void populatePaymentHeader(AuthorizationRequest authRequest, PaypalAuthResponse authResponse, PaymentHeaderDTO paymentHeader, String siteId) {

        paymentHeader.setPaymentHeaderId(authRequest.getPaymentHeaderId());
        paymentHeader.setPaymentMethod(authRequest.getPaymentMethod()
                .getType());
        paymentHeader.setProfileId(authRequest.getProfileId());

        paymentHeader.setOrderNumber(authRequest.getOrderNumber());
        paymentHeader.setOrderRef(authRequest.getOrderNumber());
        paymentHeader.setGatewayIndicator(PaymentsConstants.GATEWAY_BRAINTREE);

        paymentHeader.setSiteId(siteId);

        paymentHeader.setSubmittedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);

        paymentHeader.setState(authResponse.getResultCode());

        PaymentDetails paymentDetails = new PaymentDetails();

        paymentDetails.setCheckoutType(CommonUtils.getCheckoutType(authRequest.getCheckoutType()));
        paymentDetails.setSourceType(authRequest.getPaymentMethod()
                .getType());

        if (ObjectUtils.isNotEmpty(authResponse.getPayer())) {

            paymentDetails.setPayerEmail(authResponse.getPayer()
                    .getEmail());
            paymentDetails.setPayerId(authResponse.getPayer()
                    .getPayerId());
            paymentDetails.setPayerStatus(authResponse.getPayer()
                    .getPayerStatus());

        }

        paymentDetails.setAuthToken(authResponse.getAuthToken());

        paymentHeader.setPaymentDetails(paymentDetails);
    }

    private void populatePaymentEvents(AuthorizationRequest authRequest, PaypalAuthResponse authResponse, PaymentHeaderDTO paymentHeader) {

        PaymentEventDTO paymentEvent = null;

        if (paymentHeader.getPaymentEvents()
                .isEmpty()) {

            paymentEvent = new PaymentEventDTO();
            paymentHeader.getPaymentEvents()
                    .add(paymentEvent);
            paymentEvent.setPaymentEventId(authRequest.getIdempotencyKey());

        } else {

            paymentEvent = paymentHeader.getPaymentEvents()
                    .get(0);

        }

        paymentEvent.setMerchantReference(new StringBuilder().append(authRequest.getOrderNumber())
                .append(PaymentsConstants.HYPEN)
                .append(authRequest.getPaymentHeaderId())
                .toString());

        if (null != authRequest.getAmount()) {
            paymentHeader.setCurrencyCode(authRequest.getAmount()
                    .getCurrency());
            paymentEvent.setAmount(BigDecimal.valueOf(Double.parseDouble(authRequest.getAmount()
                    .getValue())));
        }

        paymentEvent.setTxnType(TransactionType.AUTH);

        paymentEvent.setSubmittedDate(LocalDateTime.now());
        paymentEvent.setLastModifiedDate(LocalDateTime.now());
        paymentEvent.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentEvent.setGatewayIndicator(PaymentsConstants.GATEWAY_BRAINTREE);

        EventDetails eventDetails = new EventDetails();

        paymentEvent.setTxnState(authResponse.getResultCode());
        paymentEvent.setPaymentReference(authResponse.getGraphqlId());
        paymentEvent.setMerchantAccountName(authResponse.getMerchantAccount());

        eventDetails.setPaypalAuthId(authResponse.getAuthCode());
        eventDetails.setPaypalTransactionLegacyId(authResponse.getTransactionLegacyId());

        paymentEvent.setEventDetails(eventDetails);

    }

    private void populateBillingAddress(AuthorizationRequest authRequest, PaymentHeaderDTO paymentHeader) {

        Address address = authRequest.getBillingAddress();

        BillingAddressDTO billingAddress = null != paymentHeader.getBillingAddress() ? paymentHeader.getBillingAddress() : new BillingAddressDTO();

        if (null != address) {

            billingAddress.setFirstName(address.getFirstName());
            billingAddress.setLastName(address.getLastName());
            billingAddress.setAddress1(address.getAddress1());
            billingAddress.setAddress2(address.getAddress2());
            billingAddress.setCity(address.getCity());
            billingAddress.setState(address.getState());
            billingAddress.setCountry(address.getCountry());
            billingAddress.setPostalCode(address.getPostalCode());
            billingAddress.setPhoneNumber(address.getPhoneNumber());
            billingAddress.setEmail(address.getEmail());

        }

        paymentHeader.setBillingAddress(billingAddress);

    }

}
