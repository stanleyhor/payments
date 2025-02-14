package com.ecomm.payments.service;

import com.ecomm.payments.model.afterpay.AfterpayAuthRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutRequest;
import org.springframework.http.ResponseEntity;

public interface AfterpayCallService {

    ResponseEntity<String> createCheckout(AfterpayCheckoutRequest request, String siteId);

    ResponseEntity<String> authorize(AfterpayAuthRequest request, String siteId);

    ResponseEntity<String> reverseAuth(AfterpayAuthRequest request, String siteId);

}
