package com.ecomm.payments.service;

import com.ecomm.payments.model.braintree.AuthorizationRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;

public interface BTClientService {

    ClientGraphQlResponse makeAuthCall(AuthorizationRequest authRequest, String siteId);

    ClientGraphQlResponse makePayPalAuthCall(AuthorizationRequest authRequest, String siteId);

    ClientGraphQlResponse makeCreateClientTokenCall(String siteId);

}
