package com.ecomm.payments.util;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.afterpay.AfterpayAddress;
import com.ecomm.payments.model.afterpay.AfterpayAuthResponse;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.database.BillingAddressDTO;
import com.ecomm.payments.model.database.EventDetails;
import com.ecomm.payments.model.database.PaymentDetails;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.ecomm.payments.model.database.TransactionType;
import com.ecomm.payments.repository.PaymentHeaderRepository;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AfterpayRepoWrapper {

    private final PaymentHeaderRepository headerRepository;

    private final PaymentsConfig paymentsConfig;

    @Transactional
    public void saveCreateCheckoutDetails(ATGAuthRequest request, AfterpayCheckoutResponse response) {

        var optPaymentHeader = getPaymentHeaderByOrderNumber(request.getOrderNumber());

        var paymentHeaderDTO = optPaymentHeader.isPresent() ? optPaymentHeader.get() : new PaymentHeaderDTO();

        populatePaymentHeader(request, response, paymentHeaderDTO);
        populatePaymentEvents(request, paymentHeaderDTO);
        populateBillingAddress(request.getBillingAddress(), paymentHeaderDTO);

        headerRepository.save(paymentHeaderDTO);

        log.info("AfterpayRepoWrapper.saveCreateCheckoutDetails :: saved create checkout details for orderNumber {}", request.getOrderNumber());
    }

    private void populatePaymentHeader(ATGAuthRequest request, AfterpayCheckoutResponse response, PaymentHeaderDTO paymentHeader) {

        paymentHeader.setPaymentHeaderId(request.getPaymentGroupId());
        paymentHeader.setPaymentMethod(request.getPaymentMethod()
                .getType());
        paymentHeader.setOrderNumber(request.getOrderNumber());
        paymentHeader.setOrderRef(request.getOrderNumber());
        paymentHeader.setGatewayIndicator(PaymentsConstants.GATEWAY_AFTERPAY);
        paymentHeader.setSubmittedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedDate(LocalDateTime.now());
        paymentHeader.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentHeader.setState(PaymentsConstants.REDIRECT_AFTERPAY);
        paymentHeader.setSiteId(request.getSiteId());
        paymentHeader.setProfileId(request.getAtgProfileId());
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setCheckoutType(CommonUtils.getCheckoutType(request.getCheckoutType()));
        paymentDetails.setSourceType(request.getPaymentMethod()
                .getType());
        paymentDetails.setAfterPayToken(response.getToken());
        paymentHeader.setPaymentDetails(paymentDetails);
    }

    private void populatePaymentEvents(ATGAuthRequest request, PaymentHeaderDTO paymentHeader) {

        PaymentEventDTO paymentEvent = null;

        if (paymentHeader.getPaymentEvents()
                .isEmpty()) {
            paymentEvent = new PaymentEventDTO();
            paymentHeader.getPaymentEvents()
                    .add(paymentEvent);
            paymentEvent.setPaymentEventId(request.getIdempotencyKey());
        } else {
            paymentEvent = paymentHeader.getPaymentEvents()
                    .get(0);
        }
        paymentEvent.setMerchantReference(new StringBuilder().append(request.getOrderNumber())
                .append(PaymentsConstants.HYPEN)
                .append(request.getPaymentGroupId())
                .toString());
        if (null != request.getAmount()) {
            paymentHeader.setCurrencyCode(request.getAmount()
                    .getCurrency());
            paymentEvent.setAmount(BigDecimal.valueOf(CommonUtils.convertCentsInToDollars(request.getAmount()
                    .getValue())));
        }
        paymentEvent.setTxnType(TransactionType.AUTH);
        paymentEvent.setTxnState(PaymentsConstants.REDIRECT_AFTERPAY);
        paymentEvent.setSubmittedDate(LocalDateTime.now());
        paymentEvent.setLastModifiedDate(LocalDateTime.now());
        paymentEvent.setLastModifiedBy(PaymentsConstants.AUTH_TXN_SERVICE);
        paymentEvent.setGatewayIndicator(PaymentsConstants.GATEWAY_AFTERPAY);
        paymentEvent.setEventDetails(new EventDetails());
    }

    private void populateBillingAddress(AtgBillingAddress address, PaymentHeaderDTO paymentHeader) {

        var billingAddress = null != paymentHeader.getBillingAddress() ? paymentHeader.getBillingAddress() : new BillingAddressDTO();
        if (null != address) {
            billingAddress.setAddress1(address.getAddress1());
            billingAddress.setAddress2(address.getAddress2());
            billingAddress.setCity(address.getCity());
            billingAddress.setPostalCode(address.getPostalCode());
            billingAddress.setState(address.getState());
            billingAddress.setCountry(address.getCountry());
            billingAddress.setFirstName(address.getFirstName());
            billingAddress.setLastName(address.getLastName());
            billingAddress.setEmail(address.getEmail());
            billingAddress.setPhoneNumber(address.getPhoneNumber());
        }
        paymentHeader.setBillingAddress(billingAddress);
    }

    @Transactional
    public PaymentHeaderDTO updateAuthorizeDetails(PaymentDetailsRequest request, AfterpayAuthResponse response) {

        PaymentHeaderDTO paymentHeader = null;
        var optPaymentHeader = getPaymentHeaderByOrderNumber(request.getOrderNumber());

        if (optPaymentHeader.isPresent()) {
            paymentHeader = optPaymentHeader.get();
            paymentHeader.setLastModifiedDate(LocalDateTime.now());
            paymentHeader.setState(response.getPaymentState());
            if (null != paymentHeader.getPaymentDetails()) {
                paymentHeader.getPaymentDetails()
                        .setAfterPayInstallmentAmount(request.getOrderSummary()
                                .getAfterpayInstallment());
                response.setInstallment(request.getOrderSummary()
                        .getAfterpayInstallment());
                populateBillingAddress(getBillingAddressFromResponse(request.getOrderNumber(), response), paymentHeader);
            }
            if (!paymentHeader.getPaymentEvents()
                    .isEmpty()) {
                var paymentEvent = paymentHeader.getPaymentEvents()
                        .get(0);
                paymentEvent.setPaymentReference(response.getId());
                paymentEvent.setLastModifiedDate(LocalDateTime.now());
                if (null != response.getEvents()
                        && !response.getEvents()
                                .isEmpty()) {
                    String state = paymentsConfig.getAfterpayResultCodeMap()
                            .get(response.getEvents()
                                    .get(0)
                                    .getType());
                    response.setPaymentState(state);
                    paymentHeader.setState(state);
                    paymentEvent.setTxnState(state);
                    paymentEvent.getEventDetails()
                            .setAfterpayEventId(response.getEvents()
                                    .get(0)
                                    .getId());
                    paymentEvent.getEventDetails()
                            .setAfterpayExpiryDate(response.getEvents()
                                    .get(0)
                                    .getExpires());
                }
                // error details
                paymentEvent.setErrorMessage(response.getMessage());
            } else {
                log.error("AfterpayRepoWrapper.updateAuthorizeDetails :: PaymentEventDTO missing for the existing paymentHeaderId {} and orderNumber {}",
                        paymentHeader.getPaymentHeaderId(), request.getOrderNumber());
            }
            headerRepository.save(paymentHeader);
        }
        log.info("AfterpayRepoWrapper.updateAuthorizeDetails :: Updated Authorize details for orderNumber {}", request.getOrderNumber());
        return paymentHeader;
    }

    public Optional<PaymentHeaderDTO> getPaymentHeaderByOrderNumber(String orderNumber) {
        log.info("AfterpayRepoWrapper.getPaymentHeaderByOrderNumber() :: fetch the payment header for orderNumber {}", orderNumber);
        var optPaymentHeader = headerRepository.findByOrderNumber(orderNumber);
        return optPaymentHeader.isPresent()
                && !optPaymentHeader.get()
                        .isEmpty() ? Optional.of(optPaymentHeader.get()
                                .get(0)) : Optional.empty();
    }

    @Transactional
    public PaymentHeaderDTO updateVoidDetails(String orderNumber) {
        PaymentHeaderDTO paymentHeader = null;
        var optPaymentHeader = getPaymentHeaderByOrderNumber(orderNumber);

        if (optPaymentHeader.isPresent()) {
            log.info("AfterpayRepoWrapper.updateVoidDetails() :: updating the payment header state to cancelled for orderNumber {}", orderNumber);
            paymentHeader = optPaymentHeader.get();
            paymentHeader.setState(PaymentsConstants.CANCELLED);
            if (null != paymentHeader.getPaymentEvents()
                    && !paymentHeader.getPaymentEvents()
                            .isEmpty()) {
                paymentHeader.getPaymentEvents()
                        .get(0)
                        .setTxnState(PaymentsConstants.CANCELLED);
                headerRepository.save(paymentHeader);
            } else {
                log.error("AfterpayRepoWrapper.uppdateVoidDetails :: PaymentEventDTO missing for the existing paymentHeader {} ",
                        paymentHeader.getPaymentHeaderId());
            }
        }
        return paymentHeader;
    }

    private AtgBillingAddress getBillingAddressFromResponse(String orderNumber, AfterpayAuthResponse response) {
        var address = new AtgBillingAddress();
        if (null != response.getOrderDetails()) {
            if (null != response.getOrderDetails()
                    .getBilling()) {
                var afterpayBilling = response.getOrderDetails()
                        .getBilling();
                log.debug("AfterpayRepoWrapper.getBillingAddressFromResponse() :: populate address for orderNumber {} from billing in afterpay response {}",
                        orderNumber, new Gson().toJson(afterpayBilling));
                populateAddress(afterpayBilling, address);
            } else if (null != response.getOrderDetails()
                    .getShipping()) {
                var afterpayShipping = response.getOrderDetails()
                        .getShipping();
                log.debug("AfterpayRepoWrapper.getBillingAddressFromResponse() :: populate address for orderNumber {} from shipping in afterpay response {}",
                        orderNumber, new Gson().toJson(afterpayShipping));
                populateAddress(afterpayShipping, address);
            }
            if (null != response.getOrderDetails()
                    .getConsumer()) {
                address.setPhoneNumber(
                        getFormatedPhoneNumber(StringUtils.hasText(address.getPhoneNumber()) ? address.getPhoneNumber() : response.getOrderDetails()
                                .getConsumer()
                                .getPhoneNumber(), address.getCountry()));
                address.setEmail(response.getOrderDetails()
                        .getConsumer()
                        .getEmail());
            }
            response.setBillingAddress(address);
        }
        return address;
    }

    private void populateAddress(AfterpayAddress afterpayAddress, AtgBillingAddress address) {
        if (StringUtils.hasText(afterpayAddress.getName())) {
            String[] names = afterpayAddress.getName()
                    .trim()
                    .split(PaymentsConstants.SINGLE_SPACE);
            address.setFirstName(names[0]);
            address.setLastName(names.length > 1 ? names[1] : afterpayAddress.getName());
        }
        address.setAddress1(afterpayAddress.getLine1());
        address.setAddress2(afterpayAddress.getLine2());
        address.setCity(afterpayAddress.getArea1());
        address.setState(afterpayAddress.getRegion());
        address.setPostalCode(afterpayAddress.getPostcode());
        address.setCountry(afterpayAddress.getCountryCode());
        address.setPhoneNumber(afterpayAddress.getPhoneNumber());
    }

    private String getFormatedPhoneNumber(String phoneNumber, String countryCode) {
        return CommonUtils.billingCountryCodeUSOrCA(countryCode)
                && StringUtils.hasText(phoneNumber)
                && !phoneNumber.startsWith(PaymentsConstants.PLUS_SIGN) ? new StringBuilder(PaymentsConstants.PLUS_SIGN).append(phoneNumber)
                        .toString() : phoneNumber;
    }

}
