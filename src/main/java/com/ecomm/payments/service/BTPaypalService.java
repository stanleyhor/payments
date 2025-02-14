package com.ecomm.payments.service;

import com.ecomm.payments.model.braintree.AuthorizationRequest;
import org.springframework.http.ResponseEntity;

public interface BTPaypalService {

    ResponseEntity<Object> makeAuthCall(AuthorizationRequest authRequest, String siteId);

    ResponseEntity<Object> makePayPalAuthCall(AuthorizationRequest authRequest, String siteId);

    ResponseEntity<Object> getPayPalClientToken(String siteId);

}
