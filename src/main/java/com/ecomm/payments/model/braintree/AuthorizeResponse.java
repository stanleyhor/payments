package com.ecomm.payments.model.braintree;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import graphql.GraphQLError;

import java.util.List;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AuthorizeResponse {

    private AuthorizePaymentMethod authorizePaymentMethod;

    private List<GraphQLError> error;

    @Data
    public static class AuthorizePaymentMethod {

        private String clientMutationId;

        private Transaction transaction;

    }

}
