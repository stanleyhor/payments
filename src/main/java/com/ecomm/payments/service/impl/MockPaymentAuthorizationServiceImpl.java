package com.ecomm.payments.service.impl;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.BillingAddress;
import com.ecomm.payments.model.adyen.Action;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.SdkData;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.FraudResult;
import com.ecomm.payments.service.PaymentAuthorizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MockPaymentAuthorizationServiceImpl implements PaymentAuthorizationService {

    private final PaymentsConfig paymentsConfig;

    private static final String MX_MERCHANT_ACCOUNT = "AE_MX_Ecom";

    private static final String CA_MERCHANT_ACCOUNT = "AE_CA_Ecom";

    @Override
    public ATGAuthResponse authorizePayment(ATGAuthRequest input) {
        log.info("ATGAuthRequest {}", input);
        ATGAuthResponse response = buildMockATGAuthResponse(Optional.ofNullable(input));
        log.info("Mock ATGAuthResponse {}", response);
        return response;
    }

    private ATGAuthResponse buildMockATGAuthResponse(Optional<ATGAuthRequest> optRequest) {
        ATGAuthResponse mockResponse = null;
        String paymentType = null;
        if (optRequest.isPresent()) {
            ATGAuthRequest atgAuthRequest = optRequest.get();
            paymentType = atgAuthRequest.getPaymentMethod()
                    .getType();
        }
        if (PaymentsConstants.PAYPAL_PAYMENT_METHOD.equals(paymentType)) {
            mockResponse = buildPaypalATGAuthResponse(optRequest);
        } else if (PaymentsConstants.OXXO_PAYMENT_METHOD.equals(paymentType)) {
            mockResponse = buildOxxoATGAuthResponse(optRequest);
        } else if (PaymentsConstants.APPLE_PAY.equalsIgnoreCase(paymentType)) {
            mockResponse = buildApplePayAuthResponse(optRequest);
        } else {
            mockResponse = buildCCATGAuthResponse(optRequest);
        }
        if (StringUtils.isBlank(mockResponse.getResultCode())) {
            mockResponse.setResultCode(PaymentsConstants.AUTHORIZED);
        }
        return mockResponse;
    }

    public static ATGAuthResponse buildPaypalATGAuthResponse(Optional<ATGAuthRequest> optRequest) {
        ATGAuthResponse mockPaypalResponse = new ATGAuthResponse();
        ATGAuthRequest authRequest = optRequest.isPresent() ? optRequest.get() : null;

        ATGAdditionalData additionalData = new ATGAdditionalData();
        Action paypalActionResponse = new Action();
        if (null != authRequest) {
            additionalData.setPaymentMethod(authRequest.getPaymentMethod()
                    .getType());
            additionalData.setProfileId(authRequest.getAtgProfileId());
            BillingAddress billingAddress = new BillingAddress();
            if (null != authRequest.getBillingAddress()) {
                billingAddress.setAddress1(authRequest.getBillingAddress()
                        .getAddress1());
                billingAddress.setAddress2(authRequest.getBillingAddress()
                        .getAddress2());
                billingAddress.setCity(authRequest.getBillingAddress()
                        .getCity());
                billingAddress.setCountry(authRequest.getBillingAddress()
                        .getCountry());
                billingAddress.setPostalCode(authRequest.getBillingAddress()
                        .getPostalCode());
            }
            mockPaypalResponse.setAdditionalData(additionalData);
            mockPaypalResponse.setBillingAddress(billingAddress);
            paypalActionResponse.setPaymentMethodType(authRequest.getPaymentMethod()
                    .getType());
            populateAmountInResponse(authRequest, mockPaypalResponse);
            mockPaypalResponse.setMerchantReference(String.join(PaymentsConstants.HYPEN, authRequest.getOrderNumber(), authRequest.getPaymentGroupId()));
        }
        paypalActionResponse.setPaymentData("Ab02b4c0!BQABAgCtHwRZCgUiPZ+vH8iK...");
        paypalActionResponse.setType("sdk");
        SdkData data = new SdkData();
        data.setToken("EC-0NB79322B9519403J");
        paypalActionResponse.setSdkData(data);
        mockPaypalResponse.setAction(paypalActionResponse);
        mockPaypalResponse.setTransactionTimestamp(LocalDateTime.now()
                .toString());
        return mockPaypalResponse;
    }

    public ATGAuthResponse buildCCATGAuthResponse(Optional<ATGAuthRequest> optRequest) {
        ATGAuthResponse mockCCResponse = new ATGAuthResponse();
        ATGAdditionalData ccAdditionalDataResponse = new ATGAdditionalData();
        String userEmail = null;
        if (optRequest.isPresent()) {
            userEmail = optRequest.get()
                    .getBillingAddress()
                    .getEmail();
        }
        if (StringUtils.equals(userEmail, "aeprodcc_mastercard@g3v8l0qg.mailosaur.net")) {
            ccAdditionalDataResponse.setAuthCode("034277");
            ccAdditionalDataResponse.setCreditCardType("masterCard");
            ccAdditionalDataResponse.setAvsResultRaw("2");
            ccAdditionalDataResponse.setMaskedCardNumber("555534XXXXXX1115");
            ccAdditionalDataResponse.setCardAlias("E062842104765074");

        } else if (StringUtils.equals(userEmail, "aeprodcc_discover@g3v8l0qg.mailosaur.net")) {
            ccAdditionalDataResponse.setAuthCode("019506");
            ccAdditionalDataResponse.setCardAlias("A174783508266577");
            ccAdditionalDataResponse.setAvsResult("4 AVS not supported for this card type");
            ccAdditionalDataResponse.setAvsResultRaw("4");
            ccAdditionalDataResponse.setCreditCardType("discover");
            ccAdditionalDataResponse.setMaskedCardNumber("601160XXXXXX6611");

        } else if (StringUtils.equals(userEmail, "aeprodcc_amex@g3v8l0qg.mailosaur.net")) {
            ccAdditionalDataResponse.setAuthCode("072742");
            ccAdditionalDataResponse.setMaskedCardNumber("370000XXXXX0002");
            ccAdditionalDataResponse.setAvsResult("4 AVS not supported for this card type");
            ccAdditionalDataResponse.setAvsResultRaw("4");
            ccAdditionalDataResponse.setCreditCardType("americanExpress");
            ccAdditionalDataResponse.setCardAlias("K809905957420970");

        } else {
            ccAdditionalDataResponse.setAuthCode("011660");
            ccAdditionalDataResponse.setAvsResult("2 Neither postal code nor address match");
            ccAdditionalDataResponse.setAvsResultRaw("2");
            ccAdditionalDataResponse.setCreditCardType("visa");
            ccAdditionalDataResponse.setMaskedCardNumber("411111XXXXXX1111");
            ccAdditionalDataResponse.setCardAlias("H986773047236788");

        }
        ccAdditionalDataResponse.setAuthToken("RW5HKBXZTHBH7H65");
        ccAdditionalDataResponse.setCvvResponseCode("1");
        ccAdditionalDataResponse.setExpirationMonth("03");
        ccAdditionalDataResponse.setExpirationYear("2030");
        ccAdditionalDataResponse.setFraudResultType("GREEN");
        ccAdditionalDataResponse.setFraudManualReview("false");
        ccAdditionalDataResponse.setPaymentMethod("creditCard");
        ccAdditionalDataResponse.setProfileId("3242323523532");
        ccAdditionalDataResponse.setPaymentAccountReference("dcrGFNCUM8qHRaf6qnByzW0O9jDZf");

        mockCCResponse.setAdditionalData(ccAdditionalDataResponse);
        Amount ccAmount = new Amount();
        ccAmount.setValue("1500");
        ccAmount.setCurrency("MXN");
        mockCCResponse.setAmount(ccAmount);
        FraudResult ccFraudResult = new FraudResult();
        ccFraudResult.setAccountScore(0);
        mockCCResponse.setFraudResult(ccFraudResult);
        mockCCResponse.setPspReference("M2DZTQQG7NK2WN82");
        if (optRequest.isPresent()) {
            mockCCResponse.setMerchantReference(String.join(PaymentsConstants.HYPEN, optRequest.get()
                    .getOrderNumber(),
                    optRequest.get()
                            .getPaymentGroupId()));
            mockCCResponse.setMerchantAccount(getMerchantAccount(optRequest.get()));
            mockCCResponse.setTransactionTimestamp(LocalDateTime.now()
                    .toString());
            if (optRequest.get()
                    .getAmount() != null) {
                mockCCResponse.getAmount()
                        .setValue(optRequest.get()
                                .getAmount()
                                .getValue());
                mockCCResponse.getAmount()
                        .setCurrency(optRequest.get()
                                .getAmount()
                                .getCurrency());
            }
            mockCCResponse.getAdditionalData()
                    .setProfileId(optRequest.get()
                            .getAtgProfileId());
        }
        return mockCCResponse;
    }

    public static ATGAuthResponse buildOxxoATGAuthResponse(Optional<ATGAuthRequest> optRequest) {
        ATGAuthResponse mockOxxoResponse = new ATGAuthResponse();
        Action actionOxxoResponse = new Action();
        actionOxxoResponse.setPaymentMethodType(PaymentsConstants.OXXO_PAYMENT_METHOD);
        actionOxxoResponse.setAlternativeReference("59161789165483");
        actionOxxoResponse.setDownloadUrl(
                "https://test.adyen.com/hpp/generationOxxoVoucher.shtml?data=zsB4Ydy7e2zTuBI1eXuQbpjRsp9Q58PZTdeB5y%2BAm5goZVkwB%2FwFrynQdt4KZzo9z6TUaTMpAuQ1J50BJVQdXljyReT%2FyyJxaWkBh1qkB9UxHP%2FFAZWWjWX7yNR4gkuL7bwuQej6peZhL0k%2FnsJ3bqEp0%2BNakOR6O%2Bd8SsK4KFg7owghCCJ0ww4sokuVYsFixpFONN1KWSl0%2F1FtpobCFdGRw9EsUChAblVdmBbZQkoY2MCBdw1L7wyko8mv5paomv9VDUpDn3Mxy7SrJjwh27r38mt8Ho3BpgBC7hMR26Qw3nHmCyoadgxFws7x4Atw");
        actionOxxoResponse.setExpiresAt("2021-09-17T00:00:00");
        actionOxxoResponse
                .setInstructionsUrl("https://checkoutshopper-test.adyen.com/checkoutshopper/voucherInstructions.shtml?txVariant=oxxo&shopperLocale=en_US");
        actionOxxoResponse.setMerchantName(MX_MERCHANT_ACCOUNT);
        actionOxxoResponse.setMerchantReference("23423569");
        mockOxxoResponse.setMerchantAccount(MX_MERCHANT_ACCOUNT);
        actionOxxoResponse.setReference("59591617891654832021091700300001");
        actionOxxoResponse.setType("voucher");
        Amount oxxoAmount = new Amount();
        oxxoAmount.setCurrency("MXN");
        oxxoAmount.setValue("3000");
        actionOxxoResponse.setTotalAmount(oxxoAmount);
        mockOxxoResponse.setAction(actionOxxoResponse);
        if (optRequest.isPresent()) {
            populateAmountInResponse(optRequest.get(), mockOxxoResponse);
        }
        return mockOxxoResponse;
    }

    @Override
    public ATGAuthResponse authorizeAplazoPayment(ATGAuthRequest authRequest) {
        log.info("Mock AplazoPaymentRequest {}", authRequest);
        ATGAuthResponse aplazoResponse = new ATGAuthResponse();
        aplazoResponse.setResultCode(PaymentsConstants.DECLINED);
        aplazoResponse.setTransactionTimestamp(LocalDateTime.now()
                .toString());
        log.info("Mock AplazoPaymentResponse {}", aplazoResponse);
        return aplazoResponse;
    }

    private String getMerchantAccount(ATGAuthRequest atgAuthRequest) {
        return paymentsConfig.getMerchantAccounts()
                .getOrDefault(atgAuthRequest.getWebStoreId(), atgAuthRequest.getWebStoreId());
    }

    public ATGAuthResponse buildApplePayAuthResponse(Optional<ATGAuthRequest> optRequest) {
        ATGAuthResponse applePayAuthResponse = new ATGAuthResponse();
        ATGAuthRequest authRequest = optRequest.isPresent() ? optRequest.get() : null;
        ATGAdditionalData additionalData = new ATGAdditionalData();
        additionalData.setAuthCode("062798");
        additionalData.setAvsResult("2 Neither postal code nor address match");
        additionalData.setAvsResultRaw("2");
        additionalData.setMaskedCardNumber("520424XXXXXX2136");
        additionalData.setCvvResponseCode("6");
        additionalData.setAuthToken("TH679DZJ64TZ3M65");
        additionalData.setExpirationMonth("08");
        additionalData.setExpirationYear("2026");
        additionalData.setFraudResultType("GREEN");
        additionalData.setCardAlias("F705716656946105");
        additionalData.setPaymentAccountReference("LxNBC24UdyeyIztMbpOnwJPHvzB99");
        additionalData.setFundingSource("DEBIT");
        if (null != authRequest) {
            additionalData.setPaymentMethod(authRequest.getPaymentMethod()
                    .getType());
            additionalData.setProfileId(authRequest.getAtgProfileId());
            applePayAuthResponse.setMerchantReference(String.join(PaymentsConstants.HYPEN, authRequest.getOrderNumber(), authRequest.getPaymentGroupId()));
            populateAmountInResponse(authRequest, applePayAuthResponse);
            String merchantAccount = getMerchantAccount(authRequest);
            applePayAuthResponse.setMerchantAccount(merchantAccount);
            if (CA_MERCHANT_ACCOUNT.equalsIgnoreCase(merchantAccount)) {
                additionalData.setCreditCardType("interac_applepay");
                additionalData.setCardPaymentMethod("interac_card");
                applePayAuthResponse.setResultCode(PaymentsConstants.SETTLED);
            } else {
                additionalData.setCreditCardType("mc_applepay");
            }
        }
        applePayAuthResponse.setAdditionalData(additionalData);
        applePayAuthResponse.setPspReference("SRTRDK59Q75ZGN82");
        applePayAuthResponse.setTransactionTimestamp(LocalDateTime.now()
                .toString());
        return applePayAuthResponse;
    }

    private static void populateAmountInResponse(ATGAuthRequest authRequest, ATGAuthResponse authResponse) {
        Amount amount = new Amount();
        if (null != authRequest.getAmount()) {
            amount.setValue(authRequest.getAmount()
                    .getValue());
            amount.setCurrency(authRequest.getAmount()
                    .getCurrency());
        }
        authResponse.setAmount(amount);
    }

}
