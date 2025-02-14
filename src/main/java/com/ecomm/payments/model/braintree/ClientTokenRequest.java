package com.ecomm.payments.model.braintree;

public record ClientTokenRequest(String clientMutationId, ClientToken clientToken) {

    public record ClientToken(String merchantAccountId) {

    }

}
