package com.ecomm.payments.model.braintree;

import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.braintree.Transaction.Payer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PaypalAuthResponse {

    private String orderNumber;

    private String paymentHeaderId;

    private String authToken;

    private String authCode;

    private String graphqlId;

    private String transactionLegacyId;

    private String customerId;

    private String resultCode;

    private String merchantAccount;

    private String refusalReason;

    private String refusalReasonCode;

    private String transactionTimestamp;

    private Amount amount;

    private Payer payer;

    private Address billingAddress;

}
