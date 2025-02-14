package com.ecomm.payments.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestPaymentsConfig {

    public static PaymentsConfig getPaymentConfig() {
        PaymentsConfig config = new PaymentsConfig();

        HashMap<String, String> resultCodeMap = new HashMap<>();
        resultCodeMap.put("Authorised", "AUTHORIZED");
        resultCodeMap.put("Refused", "AUTHORIZE_REFUSED");
        resultCodeMap.put("Error", "ERROR");
        config.setResultCodeMap(resultCodeMap);

        HashMap<String, String> merchantAccounts = new HashMap<>();
        merchantAccounts.put("09021", "AE_MX_Ecom");
        merchantAccounts.put("02953", "AE_US_Ecom");
        merchantAccounts.put("01790", "AE_CA_Ecom");
        merchantAccounts.put("02954", "AE_INTL_Ecom");
        config.setMerchantAccounts(merchantAccounts);

        HashMap<String, String> paymentMethodType = new HashMap<>();
        paymentMethodType.put("creditcard", "scheme");
        paymentMethodType.put("giftcard", "svs");
        paymentMethodType.put("paypal", "paypal");
        config.setPaymentMethodType(paymentMethodType);

        HashMap<String, String> channelType = new HashMap<>();
        paymentMethodType.put("WEB", "Web");
        paymentMethodType.put("IOS_APP", "iOS");
        paymentMethodType.put("ANDROID_APP", "Android");
        config.setChannelType(channelType);

        List<String> authSuccessResultCodes = new ArrayList<String>();
        authSuccessResultCodes.add("Authorised");
        authSuccessResultCodes.add("Pending");
        authSuccessResultCodes.add("Received");
        config.setAuthSuccessResultCodes(authSuccessResultCodes);

        var dbAllowedResultCodes = new ArrayList<String>();
        dbAllowedResultCodes.add("AUTHORIZED");
        dbAllowedResultCodes.add("PENDING");
        config.setDbAllowedResultCodes(dbAllowedResultCodes);

        HashMap<String, List<String>> skipAddress2ForAuth = new HashMap<>();
        List<String> paymentsList = new ArrayList<>();
        paymentsList.add("creditcard");
        skipAddress2ForAuth.put("01790", paymentsList);
        skipAddress2ForAuth.put("02954", paymentsList);
        List<String> paymentsList1 = new ArrayList<>();
        paymentsList1.add("creditcard");
        paymentsList1.add("applepay");
        skipAddress2ForAuth.put("02953", paymentsList1);
        skipAddress2ForAuth.put("09021", new ArrayList<>());
        config.setSkipAddress2ForAuth(skipAddress2ForAuth);

        HashMap<String, String> map = new HashMap<>();
        map.put("visa", "visa");
        config.setCreditCardType(map);

        config.setPaymentsURL("http://localhost:");

        var allowedPayments = new ArrayList<String>();
        allowedPayments.add("paypal");
        allowedPayments.add("applePay");
        config.setAllowedReauthCapturePayments(allowedPayments);

        var allowedPaymentsForAuthTokenValidation = new ArrayList<String>();
        allowedPaymentsForAuthTokenValidation.add("CREDITCARD");
        config.setAllowedPaymentsForAuthTokenValidation(allowedPaymentsForAuthTokenValidation);

        config.setAfterpayServiceURL("http://localhost:9081/");
        config.setAfterpayAuthEndPoint("/payments/auth");
        config.setAfterpayTokenEndPoint("/checkouts");

        HashMap<String, String> afterpayResultCodeMap = new HashMap<>();
        afterpayResultCodeMap.put("AUTH_APPROVED", "AUTHORIZED");
        afterpayResultCodeMap.put("AUTH_DECLINED", "DECLINED");
        config.setAfterpayResultCodeMap(afterpayResultCodeMap);
        config.setAfterpayReverseAuthEndPoint("/payments/token:");
        config.setAfterpayReverseAuthURI("/reversal");

        config.setBraintreeServiceURL("https://payments.sandbox.braintree-api.com/graphql");
        config.setBraintreeAuth("Basic auth");

        HashMap<String, String> btMerchantIds = new HashMap<>();
        btMerchantIds.put("AEO_US", "americaneagle");
        btMerchantIds.put("AEO_CA", "americaneagleCA");
        btMerchantIds.put("AEO_MX", "americaneagleMX");
        config.setBraintreeSiteIdMerchantIdMap(btMerchantIds);

        HashMap<String, String> braintreeResultCodeMap = new HashMap<>();
        braintreeResultCodeMap.put("AUTHORIZED", "AUTHORIZED");
        braintreeResultCodeMap.put("GATEWAY_REJECTED", "AUTHORIZE_REFUSED");
        config.setBraintreeResultCodeMap(braintreeResultCodeMap);

        HashMap<String, String> siteIdAuthMap = new HashMap<>();
        siteIdAuthMap.put("AEO_US", "Basic 912aba1312ea9f2");
        siteIdAuthMap.put("AEO_CA", "Basic f4ed5a300a548ab");
        config.setAfterpaySiteIdAuthMap(siteIdAuthMap);

        config.setSendPaypalPayeeEmail(false);

        HashMap<String, String> siteIdPayeeEmailMap = new HashMap<>();
        siteIdPayeeEmailMap.put("AEO_US", "american_eagle_biz@gmail.com");
        siteIdPayeeEmailMap.put("AEO_CA", "american_eagle_canada_biz@gmail.com");
        config.setPaypalPayeeEmailMap(siteIdPayeeEmailMap);

        config.setPaymentsDetailsEndPoint("/payments/detail");

        var synchronyCreditCards = new ArrayList<String>();
        synchronyCreditCards.add("synchrony_plcc");
        synchronyCreditCards.add("synchrony_cbcc");
        synchronyCreditCards.add("synchrony_cbcc_applepay");
        synchronyCreditCards.add("synchrony_plcc_applepay");
        config.setSynchronyCreditCards(synchronyCreditCards);

        var saleTransactionPaymentMethods = new ArrayList<String>();
        saleTransactionPaymentMethods.add("interac_applepay");
        config.setSaleTransactionPaymentMethods(saleTransactionPaymentMethods);

        return config;
    }

}
