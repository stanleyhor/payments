package com.ecomm.payments.util;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AuthorizationFailedException;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.OrderPaymentDetails;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.PaymentStatus;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.BillingAddress;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.error.ErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PaymentDetailsRequestResponseMapper {

    private PaymentsConfig paymentsConfig;
    private CommonUtils commonUtils;

    /**
     * This method will convert the detailsReq object to AdyenDetailsRequest Object
     * 
     * @param detailsReq - PaymentDetailsRequest object
     * @return adyenDetailsReq - AdyenDetailsRequest Object
     */
    public AdyenDetailsRequest convertDetailsRequest(PaymentDetailsRequest detailsReq) {
        AdyenDetailsRequest adyenDetailsReq = null;
        if (detailsReq == null) {
            return adyenDetailsReq;
        }
        adyenDetailsReq = new AdyenDetailsRequest();
        adyenDetailsReq.setPaymentData(detailsReq.getPaymentData());
        adyenDetailsReq.setDetails(detailsReq.getDetails());
        return adyenDetailsReq;
    }

    /**
     * This method will convert the AdyenDetailsResponse response object to PaymentDetailsResponse object.
     * 
     * @param adyenDetailsResponse - AdyenDetailsResponse Object
     * @param input
     * @param input                - PaymentDetailsRequest Object
     * @return paymentDetailsResponse - PaymentDetailsResponse Object
     */
    public PaymentDetailsResponse convertDetailsResponse(AdyenDetailsResponse adyenDetailsResponse, PaymentDetailsRequest input) {
        String merchantReference;
        if (adyenDetailsResponse == null) {
            return null;
        }
        // check of result code
        if (isAuthFailedResultCode(adyenDetailsResponse.getResultCode())) {
            throw new AuthorizationFailedException(ErrorType.AUTH_FAILED);
        }
        PaymentDetailsResponse paymentDetailsResponse = new PaymentDetailsResponse();
        ResponseData data = new ResponseData();
        OrderPaymentDetails orderDetails = new OrderPaymentDetails();
        if (adyenDetailsResponse.getAmount() != null) {
            orderDetails.setCurrencyCode(adyenDetailsResponse.getAmount()
                    .getCurrency());
        } else {
            orderDetails.setCurrencyCode(adyenDetailsResponse.getAdditionalData()
                    .getAuthorisedAmountCurrency());
        }
        List<PaymentStatus> listOfPaymentStatus = new ArrayList<>();
        // Payment Status
        PaymentStatus paymentStatus = new PaymentStatus();

        // Set Order Number and Payment Group id
        if (StringUtils.isNotBlank(adyenDetailsResponse.getMerchantReference())) {
            merchantReference = adyenDetailsResponse.getMerchantReference();
        } else {
            merchantReference = adyenDetailsResponse.getAdditionalData()
                    .getMerchantReference();
        }

        setPGIdAndOrderNumber(orderDetails, paymentStatus, merchantReference);
        // Method to set the Amount data.
        setAmountData(paymentStatus, adyenDetailsResponse.getAmount(), adyenDetailsResponse.getAdditionalData());
        paymentStatus.setPspReference(adyenDetailsResponse.getPspReference());
        paymentStatus.setFraudManualReview(getFraudManual(adyenDetailsResponse.getAdditionalData()));
        // Get ATG Additional data
        ATGAdditionalData atgAdditionalData = null;
        if (adyenDetailsResponse.getAdditionalData() != null) {
            atgAdditionalData = commonUtils.getAtgAdditionalData(adyenDetailsResponse.getAdditionalData(), null);
            atgAdditionalData.setFraudManualReview(null);
            // Setting Billing Address
            paymentStatus.setBillingAddress(getDetailsBillingAddress(adyenDetailsResponse.getAdditionalData()));
        }

        if (input.getDetails() != null
                && (StringUtils.isNotBlank(input.getDetails()
                        .getPayerID())
                        || StringUtils.isNotBlank(input.getDetails()
                                .getPayload()))) {
            paymentStatus.setPaymentType(PaymentsConstants.PAYPAL_PAYMENT_METHOD);
        } else {
            paymentStatus.setPaymentType(PaymentsConstants.CREDIT_CARD_TYPE);
        }

        if (adyenDetailsResponse.getFraudResult() != null) {
            if (atgAdditionalData == null) {
                atgAdditionalData = new ATGAdditionalData();
            }
            atgAdditionalData.setFraudScore(adyenDetailsResponse.getFraudResult()
                    .getAccountScore());
        }
        paymentStatus.setAdditionalData(atgAdditionalData);
        paymentStatus.setResultCode(mappedResultCode(adyenDetailsResponse.getResultCode()));

        if (commonUtils.validateAuthTokenAndUpdateResultCode(orderDetails.getOrderNumber(), paymentStatus.getPaymentType(), atgAdditionalData,
                paymentStatus.getResultCode())) {
            paymentStatus.setResultCode(PaymentsConstants.AUTH_PENDING);
        }

        listOfPaymentStatus.add(paymentStatus);
        orderDetails.setPaymentStatus(listOfPaymentStatus);
        data.setOrderPaymentDetails(orderDetails);
        paymentDetailsResponse.setData(data);
        paymentDetailsResponse.setError(new ErrorResponse());
        return paymentDetailsResponse;
    }

    private String mappedResultCode(String resultCode) {
        return paymentsConfig.getResultCodeMap()
                .getOrDefault(resultCode, PaymentsConstants.DECLINED);
    }

    private boolean isAuthFailedResultCode(String resultCode) {
        if (StringUtils.isBlank(resultCode)) {
            return true;
        }
        return !paymentsConfig.getAuthSuccessResultCodes()
                .contains(resultCode);
    }

    /**
     * Method to set the payment group id and order number to response data.
     * 
     * @param orderDetails      - OrderPaymentDetails Object
     * @param paymentStatus     - PaymentStatus Object
     * @param merchantReference - String
     */
    private void setPGIdAndOrderNumber(OrderPaymentDetails orderDetails, PaymentStatus paymentStatus, String merchantReference) {
        if (StringUtils.isBlank(merchantReference)) {
            return;
        }
        String[] split = merchantReference.split(PaymentsConstants.HYPEN);
        if (split.length > 1) {
            orderDetails.setOrderNumber(split[0]);
            paymentStatus.setPaymentGroupId(split[1]);
        } else {
            paymentStatus.setPaymentGroupId(split[0]);
        }
    }

    /**
     * Method to return the fraud manual
     * 
     * @param additionalData - AdditionalData Object
     * @return isFraudManual - boolean
     */
    private boolean getFraudManual(AdyenAdditionalData additionalData) {
        boolean isFraudManual = Boolean.FALSE;
        if (additionalData != null) {
            isFraudManual = Boolean.valueOf(additionalData.getFraudManualReview());
        }
        return isFraudManual;
    }

    /**
     * Method to update the atg billing address from the adyen billing address.
     * 
     * @param additionalData - AdditionalData Object
     * @return address - ATG AtgBillingAddress Object
     */
    private AtgBillingAddress getDetailsBillingAddress(AdyenAdditionalData additionalData) {
        if (additionalData.getBillingAddress() == null) {
            return null;
        }
        BillingAddress billingAddress = additionalData.getBillingAddress();
        AtgBillingAddress atgBillingAddress = new AtgBillingAddress();
        atgBillingAddress.setAddress1(billingAddress.getStreet());
        atgBillingAddress.setAddress2(billingAddress.getHouseNumberOrName());
        atgBillingAddress.setCity(billingAddress.getCity());
        atgBillingAddress.setState(billingAddress.getStateOrProvince());
        atgBillingAddress.setPostalCode(billingAddress.getPostalCode());
        atgBillingAddress.setCountry(billingAddress.getCountry());
        return atgBillingAddress;
    }

    /**
     * Method to set the currency code and amount.
     * 
     * @param paymentStatus : PaymentStatus Object
     * @param amount        : Amount Object
     */
    private void setAmountData(PaymentStatus paymentStatus, Amount amount, AdyenAdditionalData adyenAdditionalData) {
        if (amount != null) {
            paymentStatus.setAmountAuthorized(Long.parseLong(amount.getValue()));
            paymentStatus.setCurrencyCode(amount.getCurrency());
        } else {
            if (StringUtils.isNotBlank(adyenAdditionalData.getAuthorisedAmountValue())) {
                paymentStatus.setAmountAuthorized(Long.parseLong(adyenAdditionalData.getAuthorisedAmountValue()));
            }
            paymentStatus.setCurrencyCode(adyenAdditionalData.getAuthorisedAmountCurrency());
        }
    }

}
