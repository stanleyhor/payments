package com.ecomm.payments.util;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.DuplicateAuthorizationException;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.database.PaymentEventDTO;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationUtils {

    private final AuthRequestResponseMapper authRequestAndResponseMapper;

    private final TransactionalDataMapper transactionalDataMapper;

    private final PaymentsConfig paymentsConfig;

    private final PaymentDetailsRequestResponseMapper authDetailsMapper;

    public ATGAuthResponse checkExistingPaymentHeaderAvailable(ATGAuthRequest authRequest) {

        ATGAuthResponse authResponse = null;
        var paymentHeader = transactionalDataMapper.getPaymentHeader(authRequest.getPaymentGroupId());

        if (paymentHeader.isPresent()) {

            if (!isVoidOrRedirectShopperPresent(paymentHeader.get())) {

                var paymentEvent = getPaymentEventIfAvailable(paymentHeader.get(), authRequest);

                if (paymentEvent.isPresent()) {

                    if (authRequest.getOrderNumber()
                            .equalsIgnoreCase(paymentHeader.get()
                                    .getOrderNumber())) {
                        log.warn("authorization payment header is already available for this request :: {}",
                                new Gson().toJson(authRequest, ATGAuthRequest.class));
                        authResponse = authRequestAndResponseMapper.buildAuthResponseFromHeader(paymentHeader, paymentEvent.get());
                    } else {
                        log.error("Duplicate authorization exception for :: {}", new Gson().toJson(authRequest, ATGAuthRequest.class));
                        throw new DuplicateAuthorizationException(ErrorType.DUPLICATE_AUTHORIZATION_FAILED);
                    }

                }
            } else {
                authRequest.setIdempotencyKey(UUID.randomUUID()
                        .toString());
            }

        }
        return authResponse;
    }

    private Optional<PaymentEventDTO> getPaymentEventIfAvailable(PaymentHeaderDTO paymentHeaderDTO, ATGAuthRequest authRequest) {

        return paymentHeaderDTO.getPaymentEvents()
                .stream()
                .filter(event -> event.getPaymentEventId()
                        .equalsIgnoreCase(authRequest.getIdempotencyKey()))
                .findFirst();

    }

    private boolean isVoidOrRedirectShopperPresent(PaymentHeaderDTO paymentHeaderDTO) {

        return paymentHeaderDTO.getPaymentEvents()
                .stream()
                .anyMatch(event -> (PaymentsConstants.VOID.equalsIgnoreCase(Objects.nonNull(event.getTxnType()) ? event.getTxnType()
                        .name() : PaymentsConstants.EMPTY_STRING))
                        || (PaymentsConstants.REDIRECT_SHOPPER.equalsIgnoreCase(event.getTxnState())));
    }

    public boolean isAuthRetry(ATGAuthRequest authRequest) {

        return authRequest.isReauth()
                || authRequest.isRetroCharge()
                || authRequest.isEditAddress();
    }

    public Optional<PaymentEventDTO> findByPaymentReference(Optional<PaymentHeaderDTO> paymentHeader, ATGAuthRequest authRequest) {

        if (paymentHeader.isEmpty()) {
            return Optional.empty();
        }

        return paymentHeader.get()
                .getPaymentEvents()
                .stream()
                .filter(event -> Objects.nonNull(event.getPaymentReference())
                        && event.getPaymentReference()
                                .equalsIgnoreCase(authRequest.getIdempotencyKey()))
                .findFirst();
    }

    public ATGAuthResponse processAndStoreAdyenAuthResponse(
            ATGAuthRequest authRequest, AdyenAuthRequest convertedRequest, AdyenAuthResponse adyenAuthResponse
    ) {

        ATGAuthResponse authResponse = authRequestAndResponseMapper.convertToATGAuthResponse(adyenAuthResponse, authRequest);

        if (authResponse != null) {
            authResponse.setMerchantAccount(convertedRequest.getMerchantAccount());

            log.debug("ATGAuthResponse {} : {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                    new Gson().toJson(authResponse, ATGAuthResponse.class));

            if (authRequest.getOrderNumber() != null
                    && (paymentsConfig.getDbAllowedResultCodes()
                            .contains(authResponse.getResultCode()))) {

                if ((authRequest.isReauth()
                        && paymentsConfig.getAllowedReauthCapturePayments()
                                .stream()
                                .anyMatch(paymentType -> paymentType.equalsIgnoreCase(authRequest.getPaymentMethod()
                                        .getType())))
                        || authRequest.isEditAddress()) {
                    transactionalDataMapper.updateReAuthPaymentData(authRequest, authResponse);
                } else {
                    transactionalDataMapper.storeTransactionalData(authRequest, authResponse);
                }

            }
        }
        return authResponse;
    }

    public PaymentDetailsResponse processAndStoreAdyenAuthDetailsResponse(PaymentDetailsRequest detailsRequest, AdyenDetailsResponse adyenDetailsResponse) {

        var detailsResponse = authDetailsMapper.convertDetailsResponse(adyenDetailsResponse, detailsRequest);

        log.info("PaymentDetailsResponse {}", new Gson().toJson(detailsResponse));

        var paymentHeader = transactionalDataMapper.updateTransactionalData(detailsResponse);

        populateBillingEmail(detailsResponse, paymentHeader);

        return detailsResponse;
    }

    private void populateBillingEmail(PaymentDetailsResponse detailsResponse, Optional<PaymentHeaderDTO> paymentHeader) {

        if (Objects.nonNull(detailsResponse)
                && Objects.nonNull(detailsResponse.getData())
                && Objects.nonNull(detailsResponse.getData()
                        .getOrderPaymentDetails())
                && Objects.nonNull(detailsResponse.getData()
                        .getOrderPaymentDetails()
                        .getPaymentStatus())
                && Objects.nonNull(detailsResponse.getData()
                        .getOrderPaymentDetails()
                        .getPaymentStatus()
                        .get(0))
                && paymentHeader.isPresent()
                && Objects.nonNull(paymentHeader.get()
                        .getBillingAddress())) {

            var paymentStatus = detailsResponse.getData()
                    .getOrderPaymentDetails()
                    .getPaymentStatus()
                    .get(0);
            var billingAddress = paymentHeader.get()
                    .getBillingAddress();

            if (Objects.nonNull(paymentStatus.getBillingAddress())) {

                paymentStatus.getBillingAddress()
                        .setEmail(billingAddress.getEmail());
                paymentStatus.getBillingAddress()
                        .setPhoneNumber(billingAddress.getPhoneNumber());

            } else {
                var detailsBillingAddress = new AtgBillingAddress();
                detailsBillingAddress.setAddress1(billingAddress.getAddress1());
                detailsBillingAddress.setAddress2(billingAddress.getAddress2());
                detailsBillingAddress.setCity(billingAddress.getCity());
                detailsBillingAddress.setCountry(billingAddress.getCountry());
                detailsBillingAddress.setEmail(billingAddress.getEmail());
                detailsBillingAddress.setFirstName(billingAddress.getFirstName());
                detailsBillingAddress.setLastName(billingAddress.getLastName());
                detailsBillingAddress.setPhoneNumber(billingAddress.getPhoneNumber());
                detailsBillingAddress.setPostalCode(billingAddress.getPostalCode());
                detailsBillingAddress.setState(billingAddress.getState());
                paymentStatus.setBillingAddress(detailsBillingAddress);
            }
        }
    }

}
