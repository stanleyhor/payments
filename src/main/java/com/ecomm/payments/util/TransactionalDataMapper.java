package com.ecomm.payments.util;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.IntlPaymentAuthMessage;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.PaymentHeader;
import com.ecomm.payments.model.PaymentStatus;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.ShippingAddress;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.model.database.TransactionType;
import com.ecomm.payments.repository.PaymentHeaderRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalDataMapper {

    private final PaymentHeaderRepository headerRepository;

    private final PaymentsConfig paymentsConfig;

    @Transactional
    public void storeTransactionalData(ATGAuthRequest request, ATGAuthResponse response) {

        PaymentHeaderDTO paymentHeader = null;

        var optPaymentHeader = headerRepository.findById(request.getPaymentGroupId());

        if (optPaymentHeader.isEmpty()) {
            paymentHeader = new PaymentHeaderDTO();
        } else {
            paymentHeader = optPaymentHeader.get();
        }

        populatePaymentHeader(request, response, paymentHeader);
        populatePaymentEvents(request, response, paymentHeader);
        populateBillingAddress(request, paymentHeader);

        headerRepository.save(paymentHeader);
    }

    @Transactional
    public void storeIntlAuthData(IntlPaymentAuthMessage intlPaymentAuthMessage) {

        PaymentHeaderDTO paymentHeader = null;

        var optPaymentHeader = headerRepository.findById(intlPaymentAuthMessage.getPaymentHeaders()
                .get(0)
                .getPaymentHeaderId());

        if (optPaymentHeader.isEmpty()) {
            paymentHeader = new PaymentHeaderDTO();
        } else {
            paymentHeader = optPaymentHeader.get();
        }

        populateIntlPaymentHeader(intlPaymentAuthMessage, paymentHeader);
        populateIntlPaymentEvents(intlPaymentAuthMessage, paymentHeader);
        populateIntlBillingAddress(intlPaymentAuthMessage, paymentHeader);

        headerRepository.save(paymentHeader);

        log.info("TransactionalDataMapper.storeIntlAuthData() successfully");
    }

    private void populatePaymentHeader(ATGAuthRequest request, ATGAuthResponse response, PaymentHeaderDTO paymentHeader) {

        paymentHeader.setPaymentMethod(request.getPaymentMethod()
                .getType());
        paymentHeader.setCurrencyCode(request.getAmount()
                .getCurrency());
        paymentHeader.setPaymentHeaderId(request.getPaymentGroupId());
        paymentHeader.setOrderNumber(request.getOrderNumber());
        paymentHeader.setOrderRef(request.getOrderNumber());
        paymentHeader.setState(response.getResultCode());
        if (null != request.getRequestContext()) {
            paymentHeader.setGatewayIndicator(request.getRequestContext()
                    .getGateway());
        }
        paymentHeader.setSubmittedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentHeader.setSiteId(request.getSiteId());
        paymentHeader.setProfileId(request.getAtgProfileId());

        populatePaymentDetails(request, response, paymentHeader);
    }

    private void populateIntlPaymentHeader(IntlPaymentAuthMessage intlPaymentAuthMessage, PaymentHeaderDTO paymentHeader) {

        PaymentHeader requestPaymentHeader = intlPaymentAuthMessage.getPaymentHeaders()
                .get(0);

        paymentHeader.setPaymentMethod(requestPaymentHeader.getPaymentVariation());
        paymentHeader.setCurrencyCode(requestPaymentHeader.getCurrencyCode());
        paymentHeader.setPaymentHeaderId(requestPaymentHeader.getPaymentHeaderId());
        paymentHeader.setOrderNumber(intlPaymentAuthMessage.getOrderNumber());
        paymentHeader.setState(requestPaymentHeader.getState());
        paymentHeader.setGatewayIndicator(requestPaymentHeader.getGatewayIndicator());
        paymentHeader.setSubmittedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentHeader.setSiteId(intlPaymentAuthMessage.getSiteId());
        paymentHeader.setProfileId(intlPaymentAuthMessage.getProfileId());

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setLoanId(requestPaymentHeader.getLoanId());

        paymentHeader.setPaymentDetails(paymentDetails);

    }

    private void populateIntlPaymentEvents(IntlPaymentAuthMessage intlPaymentAuthMessage, PaymentHeaderDTO paymentHeader) {

        PaymentHeader requestPaymentHeader = intlPaymentAuthMessage.getPaymentHeaders()
                .get(0);

        PaymentEventDTO paymentEvent = new PaymentEventDTO();
        paymentEvent.setPaymentEventId(String.valueOf(requestPaymentHeader.getTransactionId()));
        paymentEvent.setPaymentReference(String.valueOf(requestPaymentHeader.getLoanId()));
        paymentEvent.setAmount(BigDecimal.valueOf(requestPaymentHeader.getAmount()));
        paymentEvent.setTxnType(TransactionType.AUTH);
        paymentEvent.setTxnState(requestPaymentHeader.getState());
        paymentEvent.setSubmittedDate(requestPaymentHeader.getSubmittedDate());
        paymentEvent.setLastModifiedDate(LocalDateTime.now());
        paymentEvent.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentEvent.setGatewayIndicator(requestPaymentHeader.getGatewayIndicator());
        paymentEvent.setMerchantReference(intlPaymentAuthMessage.getOrderNumber());
        paymentEvent.setEventDetails(new EventDetails());
        paymentHeader.getPaymentEvents()
                .add(paymentEvent);
    }

    private void populateIntlBillingAddress(IntlPaymentAuthMessage intlPaymentAuthMessage, PaymentHeaderDTO paymentHeader) {

        PaymentHeader requestPaymentHeader = intlPaymentAuthMessage.getPaymentHeaders()
                .get(0);

        BillingAddressDTO billingAddress = null;

        if (paymentHeader.getBillingAddress() != null) {
            billingAddress = paymentHeader.getBillingAddress();
        } else {
            billingAddress = new BillingAddressDTO();
        }

        var address = requestPaymentHeader.getBillingAddress();

        if (null != address) {

            billingAddress.setAddress1(address.getAddress1());
            billingAddress.setAddress2(address.getAddress2());
            billingAddress.setAddress3(address.getNeighbourhood());
            billingAddress.setCity(address.getCity());
            billingAddress.setPostalCode(address.getPostalCode());
            billingAddress.setState(address.getState());
            billingAddress.setCountry(address.getCountry());
            billingAddress.setFirstName(address.getFirstName());
            billingAddress.setLastName(address.getLastName());
            billingAddress.setPhoneNumber(address.getPhoneNumber());
            billingAddress.setEmail(address.getEmail());
        }

        paymentHeader.setBillingAddress(billingAddress);
    }

    public void populatePaymentEvents(ATGAuthRequest request, ATGAuthResponse response, PaymentHeaderDTO paymentHeader) {

        PaymentEventDTO paymentEvent = null;

        if (!paymentHeader.getPaymentEvents()
                .isEmpty()) {
            paymentEvent = paymentHeader.getPaymentEvents()
                    .get(0);
        } else {
            paymentEvent = new PaymentEventDTO();
            paymentHeader.getPaymentEvents()
                    .add(paymentEvent);
            paymentEvent.setPaymentEventId(request.getIdempotencyKey());
        }
        setUpPaymentEventData(request, response, paymentEvent);
    }

    /**
     * Sets data in payment event dto
     * 
     * @param request
     * @param response
     * @param paymentEvent
     */
    private void setUpPaymentEventData(ATGAuthRequest request, ATGAuthResponse response, PaymentEventDTO paymentEvent) {

        if (StringUtils.isNotBlank(response.getMerchantReference())) {
            paymentEvent.setMerchantReference(response.getMerchantReference());
        } else {
            paymentEvent.setMerchantReference(request.getOrderNumber() + PaymentsConstants.HYPEN + request.getPaymentGroupId());
        }

        paymentEvent.setPaymentReference(response.getPspReference());
        if (response.getAmount() != null) {
            paymentEvent.setAmount(BigDecimal.valueOf(CommonUtils.convertCentsInToDollars(response.getAmount()
                    .getValue())));
        } else {
            paymentEvent.setAmount(BigDecimal.valueOf(CommonUtils.convertCentsInToDollars(request.getAmount()
                    .getValue())));
        }
        paymentEvent.setMerchantAccountName(response.getMerchantAccount());
        if (PaymentsConstants.SETTLED.equalsIgnoreCase(response.getResultCode())) {
            paymentEvent.setTxnType(TransactionType.SALE);
            paymentEvent.setTxnState(PaymentsConstants.CAPTURED);
        } else {
            paymentEvent.setTxnType(TransactionType.AUTH);
            paymentEvent.setTxnState(response.getResultCode());
        }
        paymentEvent.setSubmittedDate(LocalDateTime.parse(response.getTransactionTimestamp()));
        paymentEvent.setLastModifiedDate(LocalDateTime.now());
        paymentEvent.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        if (null != request.getRequestContext()) {
            paymentEvent.setGatewayIndicator(request.getRequestContext()
                    .getGateway());
        }
        EventDetails eventDetails = new EventDetails();

        if (response.getAdditionalData() != null) {
            eventDetails.setAuthCode(response.getAdditionalData()
                    .getAuthCode());
            eventDetails.setAvsResponseCode(response.getAdditionalData()
                    .getAvsResult());
            eventDetails.setAvsResponseRawCode(response.getAdditionalData()
                    .getAvsResultRaw());
            eventDetails.setCvvResponseCode(response.getAdditionalData()
                    .getCvvResponseCode());
        }
        paymentEvent.setEventDetails(eventDetails);
    }

    private void populatePaymentDetails(ATGAuthRequest request, ATGAuthResponse response, PaymentHeaderDTO paymentHeader) {

        PaymentDetails paymentDetails = new PaymentDetails();
        String paymentMethod = request.getPaymentMethod()
                .getType();

        if (PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(paymentMethod)) {
            populateCreditCardAndApplePayPaymentDetails(request, response, paymentDetails);
        }

        if (PaymentsConstants.APPLE_PAY.equalsIgnoreCase(paymentMethod)) {
            populateCreditCardAndApplePayPaymentDetails(request, response, paymentDetails);
            paymentDetails.setCheckoutType(CommonUtils.getCheckoutType(request.getCheckoutType()));
        }

        String holderName = null;
        if (request.getBillingAddress() != null
                && StringUtils.isNotBlank(request.getBillingAddress()
                        .getFirstName())) {
            holderName = request.getBillingAddress()
                    .getFirstName();

            if (StringUtils.isNotBlank(request.getBillingAddress()
                    .getLastName())) {
                holderName += StringUtils.SPACE + request.getBillingAddress()
                        .getLastName();
            }
        }
        paymentDetails.setCardHolderName(holderName);

        if (PaymentsConstants.PAYPAL_PAYMENT_METHOD.equalsIgnoreCase(paymentMethod)
                && response.getAdditionalData() != null) {
            paymentDetails.setPayerId(response.getAdditionalData()
                    .getPaypalPayerId());
            paymentDetails.setPayerEmail(response.getAdditionalData()
                    .getPaypalEmail());
            paymentDetails.setPayerStatus(response.getAdditionalData()
                    .getPaypalPayerStatus());
        }

        if (PaymentsConstants.GIFT_CARD.equalsIgnoreCase(paymentMethod)) {
            paymentDetails.setCardNumber(request.getPaymentMethod()
                    .getGiftCardNumber());
        }

        if (null != response.getAction()) {
            paymentDetails.setAlternativeRefNumber(response.getAction()
                    .getAlternativeReference());
            paymentDetails.setVoucherReferenceNumber(response.getAction()
                    .getReference());
            paymentDetails.setVoucherExpirydate(response.getAction()
                    .getExpiresAt());
            paymentDetails.setVoucherUrl(response.getAction()
                    .getDownloadUrl());
        }
        paymentDetails.setRetroCharge(request.isRetroCharge());
        paymentDetails.setFundingSource(response.getAdditionalData() != null ? response.getAdditionalData()
                .getFundingSource() : PaymentsConstants.DEBIT);
        paymentHeader.setPaymentDetails(paymentDetails);
    }

    private void populateCreditCardAndApplePayPaymentDetails(ATGAuthRequest request, ATGAuthResponse response, PaymentDetails paymentDetails) {

        var additionalData = response.getAdditionalData();

        if (additionalData != null) {
            paymentDetails.setCardNumber(additionalData.getMaskedCardNumber());
            paymentDetails.setExpirationMonth(additionalData.getExpirationMonth());
            paymentDetails.setExpirationYear(additionalData.getExpirationYear());
            paymentDetails.setAuthToken(additionalData.getAuthToken());
            paymentDetails.setCardType(getCreditCardType(additionalData.getCreditCardType()));
            paymentDetails.setCardAlias(additionalData.getCardAlias());
            populateInstallments(request, paymentDetails);

            if (null != additionalData.getPaymentAccountReference()) {
                paymentDetails.setPaymentAccountReference(additionalData.getPaymentAccountReference());
            }
        }
    }

    private void populateInstallments(ATGAuthRequest request, PaymentDetails paymentDetails) {

        if (Objects.nonNull(request.getInstallments())) {
            paymentDetails.setInstallments(request.getInstallments()
                    .getValue());
        }
    }

    private String getCreditCardType(String creditCardType) {
        return paymentsConfig.getCreditCardType()
                .getOrDefault(creditCardType, creditCardType);
    }

    private void populateBillingAddress(ATGAuthRequest request, PaymentHeaderDTO paymentHeader) {

        BillingAddressDTO billingAddress = null;

        if (paymentHeader.getBillingAddress() != null) {
            billingAddress = paymentHeader.getBillingAddress();
        } else {
            billingAddress = new BillingAddressDTO();
        }

        if (null != request.getBillingAddress()) {

            billingAddress.setAddress1(request.getBillingAddress()
                    .getAddress1());
            billingAddress.setAddress2(request.getBillingAddress()
                    .getAddress2());
            billingAddress.setCity(request.getBillingAddress()
                    .getCity());
            billingAddress.setPostalCode(request.getBillingAddress()
                    .getPostalCode());
            billingAddress.setState(request.getBillingAddress()
                    .getState());
            billingAddress.setCountry(request.getBillingAddress()
                    .getCountry());
            billingAddress.setFirstName(request.getBillingAddress()
                    .getFirstName());
            billingAddress.setLastName(request.getBillingAddress()
                    .getLastName());
            billingAddress.setEmail(request.getBillingAddress()
                    .getEmail());
            billingAddress.setPhoneNumber(request.getBillingAddress()
                    .getPhoneNumber());
        } else if (PaymentsConstants.OXXO_PAYMENT_METHOD.equalsIgnoreCase(request.getPaymentMethod()
                .getType())
                && null != request.getAdyenFraudDetail()
                && null != request.getAdyenFraudDetail()
                        .getShippingDetail()
                && null != request.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingAddress()) {

            ShippingAddress shipAddress = request.getAdyenFraudDetail()
                    .getShippingDetail()
                    .getShippingAddress();

            billingAddress.setAddress1(shipAddress.getAddress1());
            billingAddress.setAddress2(shipAddress.getAddress2());
            billingAddress.setCity(shipAddress.getCity());
            billingAddress.setPostalCode(shipAddress.getPostalCode());
            billingAddress.setState(shipAddress.getState());
            billingAddress.setCountry(shipAddress.getCountry());
            billingAddress.setFirstName(shipAddress.getFirstName());
            billingAddress.setLastName(shipAddress.getLastName());
        } else {
            log.info("Request does not contain both shipping and billing Address for order {}", request.getOrderNumber());
        }

        if (null != request.getAdyenFraudDetail()
                && null != request.getAdyenFraudDetail()
                        .getContactInfo()) {
            billingAddress.setEmail(request.getAdyenFraudDetail()
                    .getContactInfo()
                    .getEmail());
            billingAddress.setPhoneNumber(request.getAdyenFraudDetail()
                    .getContactInfo()
                    .getPhoneNo());
        }

        paymentHeader.setBillingAddress(billingAddress);
    }

    public Optional<PaymentHeaderDTO> updateTransactionalData(PaymentDetailsResponse detailsResponse) {

        String orderNumber = detailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber();
        String paymentGroupId = detailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentGroupId();

        var paymentHeader = headerRepository.findById(paymentGroupId);

        if (paymentHeader.isEmpty()) {
            return Optional.empty();
        }

        String resultCode = detailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getResultCode();
        String merchantReference = orderNumber + PaymentsConstants.HYPEN + paymentGroupId;

        paymentHeader.get()
                .setState(resultCode);
        paymentHeader.get()
                .setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentHeader.get()
                .setLastModifiedDate(LocalDateTime.now());

        PaymentDetails paymentDetails = paymentHeader.get()
                .getPaymentDetails();
        PaymentStatus paymentStatus = detailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0);
        ATGAdditionalData additionalData = paymentStatus.getAdditionalData();
        if (PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(paymentStatus.getPaymentType())
                && additionalData != null) {

            paymentDetails.setCardNumber(additionalData.getMaskedCardNumber());
            paymentDetails.setExpirationMonth(additionalData.getExpirationMonth());
            paymentDetails.setExpirationYear(additionalData.getExpirationYear());
            paymentDetails.setAuthToken(additionalData.getAuthToken());
            paymentDetails.setCardType(getCreditCardType(additionalData.getCreditCardType()));
            paymentDetails.setCardAlias(additionalData.getCardAlias());
        }

        String holderName = null;
        if (paymentStatus.getBillingAddress() != null
                && StringUtils.isNotBlank(paymentStatus.getBillingAddress()
                        .getFirstName())) {
            holderName = paymentStatus.getBillingAddress()
                    .getFirstName();

            if (StringUtils.isNotBlank(paymentStatus.getBillingAddress()
                    .getLastName())) {
                holderName += StringUtils.SPACE + paymentStatus.getBillingAddress()
                        .getLastName();
            }
        }
        paymentDetails.setCardHolderName(holderName);

        if (PaymentsConstants.PAYPAL_PAYMENT_METHOD.equalsIgnoreCase(paymentStatus.getPaymentType())
                && additionalData != null) {
            paymentDetails.setPayerId(additionalData.getPaypalPayerId());
            paymentDetails.setPayerEmail(additionalData.getPaypalEmail());
            paymentDetails.setPayerStatus(additionalData.getPaypalPayerStatus());
        }

        paymentHeader.get()
                .getPaymentEvents()
                .stream()
                .filter(event -> event.getMerchantReference()
                        .equalsIgnoreCase(merchantReference))
                .forEach(event -> {
                    event.setPaymentReference(paymentStatus.getPspReference());
                    event.setTxnState(resultCode);
                    event.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
                    event.setLastModifiedDate(LocalDateTime.now());
                    if (additionalData != null) {
                        EventDetails eventDetails = event.getEventDetails();
                        eventDetails.setAuthCode(additionalData.getAuthCode());
                        eventDetails.setAvsResponseCode(additionalData.getAvsResult());
                        eventDetails.setAvsResponseRawCode(additionalData.getAvsResultRaw());
                        eventDetails.setCvvResponseCode(additionalData.getCvvResponseCode());
                    }
                });
        headerRepository.save(paymentHeader.get());
        return paymentHeader;
    }

    /**
     * Updates data after auth retry
     * 
     * @param request
     * @param response
     */
    public void updateReAuthPaymentData(ATGAuthRequest request, ATGAuthResponse response) {
        var paymentHeader = headerRepository.findById(request.getPaymentGroupId());
        if (checkIdempotencyKey(request, paymentHeader)) {
            var paymentHeaderDTO = paymentHeader.get();
            paymentHeaderDTO.getPaymentDetails()
                    .setReAuth(request.isReauth());
            paymentHeaderDTO.getPaymentDetails()
                    .setEditReAuth(request.isEditAddress());
            var paymentEvent = new PaymentEventDTO();
            paymentEvent.setPaymentEventId(request.getIdempotencyKey());
            setUpPaymentEventData(request, response, paymentEvent);
            paymentHeaderDTO.getPaymentEvents()
                    .add(paymentEvent);
            headerRepository.save(paymentHeaderDTO);
        }
    }

    private boolean checkIdempotencyKey(ATGAuthRequest authRequest, Optional<PaymentHeaderDTO> paymentHeaderDTO) {
        return authRequest.getIdempotencyKey() != null
                && paymentHeaderDTO.isPresent()
                && !paymentHeaderDTO.get()
                        .getPaymentEvents()
                        .isEmpty()
                && paymentHeaderDTO.get()
                        .getPaymentEvents()
                        .stream()
                        .noneMatch(paymentEventDTO -> authRequest.getIdempotencyKey()
                                .equalsIgnoreCase(paymentEventDTO.getPaymentEventId()));
    }

    /**
     * Returns payment header dto
     * 
     * @param paymentHeaderId
     * @return
     */
    public Optional<PaymentHeaderDTO> getPaymentHeader(String paymentHeaderId) {
        if (StringUtils.isNotBlank(paymentHeaderId)) {
            return headerRepository.findById(paymentHeaderId);
        }
        return Optional.empty();
    }

}