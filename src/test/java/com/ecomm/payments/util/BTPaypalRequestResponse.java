package com.ecomm.payments.util;

import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse;
import com.ecomm.payments.model.braintree.PaypalAuthResponse;

import java.nio.file.Paths;

public class BTPaypalRequestResponse {

    public static AuthorizationRequest authroizationRequest() {
        String path = Paths.get("payload", "braintree", "PaypalAuthRequest.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, AuthorizationRequest.class);
    }

    public static PaypalAuthResponse authroizationResponse() {
        String path = Paths.get("payload", "braintree", "PaypalAuthResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, PaypalAuthResponse.class);
    }

    public static BTPaypalAuthResponse authroizationBTPaypalResponse() {
        String path = Paths.get("payload", "braintree", "BTPaypalAuthResponse.json")
                .toString();
        return TestUtil.getResponseFromJsonPath(path, BTPaypalAuthResponse.class);
    }

}
