package com.ecomm.payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;

import lombok.Data;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "input")
@Data
public class PaymentsConfig {

    private String name;

    private String environment;

    private String apiKey;

    @Value("${input.paymentsAuthURL}")
    private String paymentsURL;

    private Integer connectTimeoutMillis;

    private Integer readTimeoutMillis;

    private boolean useMock;

    private Integer sleepTimeoutMillis;

    private HashMap<String, String> merchantAccounts;

    private HashMap<String, String> resultCodeMap;

    private HashMap<String, String> paymentMethodType;

    private HashMap<String, String> channelType;

    private HashMap<String, String> creditCardType;

    private String paymentsDetailsEndPoint;

    private List<String> authSuccessResultCodes;

    private String defaultOxxoShopperLocale;

    private List<String> dbAllowedResultCodes;

    private List<String> allowedReauthCapturePayments;

    private HashMap<String, List<String>> skipAddress2ForAuth;

    private List<String> allowedPaymentsForAuthTokenValidation;

    private List<String> emailsToBypassAuthCall;

    private String afterpayServiceURL;

    private String afterpayTokenEndPoint;

    private String afterpayAuthEndPoint;

    private Integer afterpayConnectTimeoutMillis;

    private Integer afterpayReadTimeoutMillis;

    private HashMap<String, String> afterpayResultCodeMap;

    private String afterpayReverseAuthEndPoint;

    private String afterpayReverseAuthURI;

    private String braintreeServiceURL;

    private String braintreeAuth;

    private String braintreeVersion;

    private Integer braintreeConnectTimeoutMillis;

    private Integer braintreeReadTimeoutMillis;

    private HashMap<String, String> braintreeSiteIdMerchantIdMap;

    private HashMap<String, String> braintreeResultCodeMap;

    private HashMap<String, String> afterpaySiteIdAuthMap;

    private boolean sendPaypalPayeeEmail;

    private HashMap<String, String> paypalPayeeEmailMap;

    private String paymentsAuthV2URL;

    private List<String> synchronyCreditCards;

    private List<String> saleTransactionPaymentMethods;

}
