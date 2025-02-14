package com.ecomm.payments.model.braintree;

public record ClientTokenResponse(CreateClientToken createClientToken) {

    public record CreateClientToken(String clientMutationId, String clientToken) {

    }

}
