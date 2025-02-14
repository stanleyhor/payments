package com.ecomm.payments.util;

import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;
import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;

import java.nio.file.Paths;

public class AdyenTestUtils {

    public static ATGAuthRequest getAuthRequest() {
        String path = Paths.get("payload", "creditcard", "CreditCardAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthRequest.class);
    }

    public static ATGAuthRequest getAuthRequestForMX() {
        String path = Paths.get("payload", "creditcard", "MXAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthRequest.class);
    }

    public static ATGAuthResponse getAuthResponse() {
        String path = Paths.get("payload", "creditcard", "CreditCardAuthResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthResponse.class);
    }

    public static AdyenAuthRequest getAdyenAuthRequest() {
        String path = Paths.get("payload", "creditcard", "AdyenAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, AdyenAuthRequest.class);
    }

    public static AdyenAuthResponse getAdyenAuthResponse() {
        String path = Paths.get("payload", "creditcard", "AdyenAuthResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, AdyenAuthResponse.class);
    }

    public static PaymentDetailsRequest getPaymentDetailsRequest() {
        String path = Paths.get("payload", "creditcard", "PaymentDetailsRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, PaymentDetailsRequest.class);
    }

    public static PaymentDetailsResponse getPaymentDetailsResponse() {
        String path = Paths.get("payload", "creditcard", "PaymentDetailsResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, PaymentDetailsResponse.class);
    }

    public static ATGAuthRequest getAdyenPaypalRequest() {
        String path = Paths.get("payload", "paypal", "AdyenPaypalAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthRequest.class);
    }

    public static ATGAuthRequest getInvalidAuthRequest() {
        String path = Paths.get("payload", "creditcard", "InvalidAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthRequest.class);
    }

    public static AdyenDetailsResponse getAdyenDetailsResponse() {
        String path = Paths.get("payload", "creditcard", "AdyenDetailsResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, AdyenDetailsResponse.class);
    }

    public static ATGAuthRequest getApplePayAuthRequest() {
        String path = Paths.get("payload", "applepay", "ApplePayAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthRequest.class);
    }

    public static ATGAuthRequest getApplePayInteracAuthRequest() {
        String path = Paths.get("payload", "applepay", "ApplePayInteracAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthRequest.class);
    }

    public static AdyenAuthResponse getAdyenApplePayInteracAuthResponse() {
        String path = Paths.get("payload", "applepay", "AdyenApplePayInteracAuthorizedResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, AdyenAuthResponse.class);
    }

    public static ATGAuthResponse getApplePayInteracAuthResponse() {
        String path = Paths.get("payload", "applepay", "ApplePayInteracAuthResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, ATGAuthResponse.class);
    }

    public static AdyenAuthResponse getAdyenApplePayInteracRefusedResponse() {
        String path = Paths.get("payload", "applepay", "AdyenApplePayInteracRefusedResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, AdyenAuthResponse.class);
    }

}
