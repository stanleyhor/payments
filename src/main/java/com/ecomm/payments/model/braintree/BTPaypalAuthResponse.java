package com.ecomm.payments.model.braintree;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BTPaypalAuthResponse {

    private AuthorizePayPalAccount authorizePayPalAccount;

    @Data
    @NoArgsConstructor
    public static class AuthorizePayPalAccount {

        private String clientMutationId;

        private BillingAgreementWithPurchasePaymentMethod billingAgreementWithPurchasePaymentMethod;

        private Transaction transaction;

    }

    @Data
    @NoArgsConstructor
    public static class BillingAgreementWithPurchasePaymentMethod {

        private String id;

        private String legacyId;

        private String usage;

        private String createdAt;

        private Details details;

    }

    @Data
    @NoArgsConstructor
    public static class Details {

        private String billingAgreementId;

        private String email;

        private String phone;

        private String payerId;

        private String cobrandedCardLabel;

        private String origin;

        private String limitedUseOrderId;

        private String selectedFinancingOption;

    }

}
