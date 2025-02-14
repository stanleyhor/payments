package com.ecomm.payments.util;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.database.PaymentHeaderDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonUtils {

    private final PaymentsConfig paymentsConfig;

    public ATGAdditionalData getAtgAdditionalData(AdyenAdditionalData adyenAdditionalData, ATGAuthRequest atgAuthRequest) {

        ATGAdditionalData atgAdditionalData = new ATGAdditionalData();
        atgAdditionalData.setAuthCode(adyenAdditionalData.getAuthCode());
        atgAdditionalData.setAvsResult(StringUtils.isNotBlank(adyenAdditionalData.getAvsResult()) ? adyenAdditionalData.getAvsResult() : null);
        atgAdditionalData.setAvsResultRaw(StringUtils.isNotBlank(adyenAdditionalData.getAvsResultRaw()) ? adyenAdditionalData.getAvsResultRaw() : null);
        atgAdditionalData.setMaskedCardNumber(getMaskedCardNumber(adyenAdditionalData));
        atgAdditionalData.setCvvResponseCode(getCvcResult(adyenAdditionalData));
        atgAdditionalData.setAuthToken(adyenAdditionalData.getRecurringDetailReference());
        atgAdditionalData.setFundingSource(populateFundingSource(adyenAdditionalData));
        if (atgAuthRequest != null) {
            atgAdditionalData.setPaymentMethod(atgAuthRequest.getPaymentMethod()
                    .getType());
        } else {
            // 3DS changes
            atgAdditionalData.setScaExemptionRequested(adyenAdditionalData.getScaExemptionRequested());
        }
        atgAdditionalData.setProfileId(adyenAdditionalData.getShopperReference());
        if (PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(atgAuthRequest != null ? atgAuthRequest.getPaymentMethod()
                .getType() : null)
                || PaymentsConstants.APPLE_PAY.equalsIgnoreCase(atgAuthRequest != null ? atgAuthRequest.getPaymentMethod()
                        .getType() : null)) {
            atgAdditionalData.setCreditCardType(adyenAdditionalData.getPaymentMethod());
        }
        if (StringUtils.isNotBlank(adyenAdditionalData.getFraudResultType())) {
            atgAdditionalData.setFraudResultType(adyenAdditionalData.getFraudResultType());
        } else {
            atgAdditionalData.setFraudResultType(PaymentsConstants.GREEN);
        }
        atgAdditionalData.setFraudManualReview(adyenAdditionalData.getFraudManualReview());

        String expiryDate = adyenAdditionalData.getExpiryDate();
        if (StringUtils.isNotBlank(expiryDate)) {
            String[] expirationMonthAndYear = expiryDate.split(PaymentsConstants.EXPIRY_DATE_DELIMITER);
            if (expirationMonthAndYear.length == 2) {
                atgAdditionalData.setExpirationMonth(String.format("%02d", Integer.valueOf(expirationMonthAndYear[0])));
                atgAdditionalData.setExpirationYear(expirationMonthAndYear[1]);
            }
        }
        // for paypal
        atgAdditionalData.setPaypalEmail(adyenAdditionalData.getPaypalEmail());
        atgAdditionalData.setPaypalPayerId(adyenAdditionalData.getPaypalPayerId());
        atgAdditionalData.setPaypalPayerResidenceCountry(adyenAdditionalData.getPaypalPayerResidenceCountry());
        atgAdditionalData.setPaypalPayerStatus(adyenAdditionalData.getPaypalPayerStatus());
        atgAdditionalData.setPaypalProtectionEligibility(adyenAdditionalData.getPaypalProtectionEligibility());
        atgAdditionalData.setCardAlias(adyenAdditionalData.getAlias());
        atgAdditionalData.setPaymentAccountReference(adyenAdditionalData.getPaymentAccountReference());
        atgAdditionalData.setCardPaymentMethod(adyenAdditionalData.getCardPaymentMethod());

        return atgAdditionalData;
    }

    private String populateFundingSource(AdyenAdditionalData adyenAdditionalData) {
        return Optional.ofNullable(adyenAdditionalData.getFundingSource())
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> paymentsConfig.getSynchronyCreditCards()
                        .contains(adyenAdditionalData.getCardPaymentMethod()) ? PaymentsConstants.CREDIT : PaymentsConstants.DEBIT);
    }

    private String getMaskedCardNumber(AdyenAdditionalData adyenAdditionalData) {
        String cardMask = PaymentsConstants.CARD_MASK;
        String cardBin = PaymentsConstants.CARD_MASK;
        StringBuilder builder = new StringBuilder();

        if (adyenAdditionalData.getCardPaymentMethod() != null
                && adyenAdditionalData.getCardPaymentMethod()
                        .equalsIgnoreCase("AMEX")) {
            cardMask = PaymentsConstants.AMEX_CARD_MASK;
        }

        if (StringUtils.isNotBlank(adyenAdditionalData.getCardBin())) {
            cardBin = adyenAdditionalData.getCardBin();
        }
        if (StringUtils.isNotBlank(adyenAdditionalData.getCardSummary())) {
            builder.append(cardBin)
                    .append(cardMask)
                    .append(adyenAdditionalData.getCardSummary());
            return builder.toString();
        } else {
            return adyenAdditionalData.getCardSummary();
        }
    }

    private String getCvcResult(AdyenAdditionalData adyenAdditionalData) {
        Optional<String> adyenCvcResult = Optional.ofNullable(adyenAdditionalData.getCvcResult());
        return adyenCvcResult.isPresent()
                && !adyenCvcResult.get()
                        .isEmpty() ? String.valueOf(adyenCvcResult.get()
                                .charAt(0)) : null;
    }

    public static String getFraudResultType(AdyenAuthResponse adyenAuthResponse) {
        return Objects.nonNull(adyenAuthResponse)
                && Objects.nonNull(adyenAuthResponse.getAdditionalData())
                && Objects.nonNull(adyenAuthResponse.getAdditionalData()
                        .getFraudResultType()) ? adyenAuthResponse.getAdditionalData()
                                .getFraudResultType() : PaymentsConstants.DEFAULT_FRAUD_RESULT_TYPE_GREEN;
    }

    public static String getResultCode(AdyenAuthResponse adyenAuthResponse) {
        return Objects.nonNull(adyenAuthResponse)
                && Objects.nonNull(adyenAuthResponse.getResultCode()) ? adyenAuthResponse.getResultCode() : PaymentsConstants.DEFAULT_RESULT_CODE_TIMEOUT;
    }

    public static String getMerchantReference(AdyenAuthRequest convertedRequest, AdyenAuthResponse adyenAuthResponse) {
        return Objects.nonNull(adyenAuthResponse)
                && Objects.nonNull(adyenAuthResponse.getMerchantReference()) ? adyenAuthResponse.getMerchantReference() : convertedRequest.getReference();
    }

    public static String getDetailsResultCode(AdyenDetailsResponse adyenDetailsResponse) {
        return Objects.nonNull(adyenDetailsResponse)
                && Objects.nonNull(adyenDetailsResponse.getResultCode()) ? adyenDetailsResponse.getResultCode() : PaymentsConstants.DEFAULT_RESULT_CODE_TIMEOUT;
    }

    public static String getDetailsFraudResultType(AdyenDetailsResponse adyenDetailsResponse) {
        return Objects.nonNull(adyenDetailsResponse)
                && Objects.nonNull(adyenDetailsResponse.getAdditionalData())
                && Objects.nonNull(adyenDetailsResponse.getAdditionalData()
                        .getFraudResultType()) ? adyenDetailsResponse.getAdditionalData()
                                .getFraudResultType() : PaymentsConstants.DEFAULT_FRAUD_RESULT_TYPE_GREEN;
    }

    public static String getOrderNumber(AdyenDetailsResponse adyenDetailsResponse) {
        String orderNumber = null;

        if (Objects.nonNull(adyenDetailsResponse)
                && Objects.nonNull(adyenDetailsResponse.getMerchantReference())) {
            var references = getSplitReferences(adyenDetailsResponse.getMerchantReference());
            orderNumber = references.length > 1 ? references[0] : adyenDetailsResponse.getMerchantReference();

        }

        return orderNumber;
    }

    public static String getPaymentHeaderId(AdyenDetailsResponse adyenDetailsResponse) {
        String paymentHeaderId = null;

        if (Objects.nonNull(adyenDetailsResponse)
                && Objects.nonNull(adyenDetailsResponse.getMerchantReference())) {
            var references = getSplitReferences(adyenDetailsResponse.getMerchantReference());
            paymentHeaderId = references.length > 1 ? references[1] : adyenDetailsResponse.getMerchantReference();

        }

        return paymentHeaderId;
    }

    private static String[] getSplitReferences(String merchantReference) {
        return (merchantReference.contains(PaymentsConstants.HYPEN)) ? merchantReference.split(PaymentsConstants.HYPEN) : merchantReference
                .split(PaymentsConstants.SINGLE_SPACE);
    }

    public boolean validateAuthTokenAndUpdateResultCode(String orderNumber, String paymentMethod, ATGAdditionalData additionalData, String resultCode) {

        boolean isAuthPending = false;

        if (Objects.nonNull(orderNumber)
                && PaymentsConstants.AUTHORIZED.equalsIgnoreCase(resultCode)
                && Objects.nonNull(paymentMethod)
                && paymentsConfig.getAllowedPaymentsForAuthTokenValidation()
                        .contains(paymentMethod.toUpperCase())
                && Objects.nonNull(additionalData)
                && Objects.isNull(additionalData.getAuthToken())) {

            isAuthPending = true;

            log.info("CommonUtils.validateAuthTokenAndUpdateResultCode() :: orderNumber {} paymentMethod {}, "
                    + "authPendingFlag {}", v(CommonKeys.ORDER_NUMBER.key(), orderNumber), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, paymentMethod),
                    v(PaymentsConstants.LOG_KEY_AUTH_PENDING_FLAG, isAuthPending));

        }
        return isAuthPending;
    }

    public static double convertCentsInToDollars(String amount) {
        double dollars = PaymentsConstants.DOUBLE_ZERO;
        if (StringUtils.isEmpty(amount)) {
            return dollars;
        }
        double dollarAmount = Double.parseDouble(amount);
        dollars = dollarAmount;
        if (dollarAmount != PaymentsConstants.DOUBLE_ZERO) {
            dollars = dollarAmount / PaymentsConstants.DOUBLE_HUNDRED;
        }
        log.debug("Amount in cents {} and dollars {} ", amount, dollars);
        return dollars;
    }

    public static String convertDollarsInToCents(double pAmount) {

        double tempAmount = pAmount * PaymentsConstants.INT_HUNDRED;

        return MessageFormat.format(PaymentsConstants.INTEGER_FORMAT, Double.valueOf(tempAmount));
    }

    public static boolean billingCountryCodeUSOrCA(String countryCode) {
        return StringUtils.isNotBlank(countryCode)
                && (countryCode.equalsIgnoreCase(PaymentsConstants.COUNTRY_US)
                        || countryCode.equalsIgnoreCase(PaymentsConstants.COUNTRY_CA));
    }

    public static String getCheckoutType(String checkoutType) {
        return StringUtils.isNotBlank(checkoutType) ? checkoutType : PaymentsConstants.CHECKOUT_TYPE_REGULAR;
    }

    public static String getChannelType(String channelType) {
        return StringUtils.isNotBlank(channelType) ? channelType : PaymentsConstants.CHANNEL_WEB;
    }

    public static String getSourceType(PaymentHeaderDTO paymentHeaderDTO) {
        return null != paymentHeaderDTO
                && null != paymentHeaderDTO.getPaymentDetails() ? paymentHeaderDTO.getPaymentDetails()
                        .getSourceType() : "";
    }

    public static String getPaymentMethod(AtgPaymentMethod atgPaymentMethod) {
        return Objects.nonNull(atgPaymentMethod) ? atgPaymentMethod.getType() : null;
    }

    public static String getDetailsRequestChannelType(PaymentDetailsRequest detailsRequest) {
        String channelType = Objects.nonNull(detailsRequest)
                && Objects.nonNull(detailsRequest.getOrderSummary()) ? detailsRequest.getOrderSummary()
                        .getChannelType() : null;

        return getChannelType(channelType);
    }

    public static String getDetailsRequestCheckoutType(PaymentDetailsRequest detailsRequest) {
        String checkoutType = Objects.nonNull(detailsRequest)
                && Objects.nonNull(detailsRequest.getOrderSummary()) ? detailsRequest.getOrderSummary()
                        .getCheckoutType() : null;

        return getCheckoutType(checkoutType);
    }

    public boolean checkSaleTransactionPaymentMethod(String creditCardType) {
        return Objects.nonNull(creditCardType)
                && paymentsConfig.getSaleTransactionPaymentMethods()
                        .contains(creditCardType);
    }

}
