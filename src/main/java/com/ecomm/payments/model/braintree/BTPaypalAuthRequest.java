package com.ecomm.payments.model.braintree;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BTPaypalAuthRequest {

    private String clientMutationId;

    private String paymentMethodId;

    private Transaction transaction;

    private Options options;

    @Data
    @ToString
    public static class Transaction {

        private String orderId;

        private String purchaseOrderNumber;

        private String amount;

        private String merchantAccountId;

        private Shipping shipping;

        private RiskData riskData;

        private String channel;

        private Vault vaultPaymentMethodAfterTransacting;

    }

    @Data
    @ToString
    public static class Shipping {

        private ShippingAddress shippingAddress;

    }

    @Data
    @ToString
    public static class RiskData {

        private String deviceData;

        private String customerIp;

        private String customerBrowser;

    }

    @Data
    @ToString
    public static class ShippingAddress {

        private String firstName;

        private String lastName;

        private String addressLine1;

        private String addressLine2;

        private String adminArea1;

        private String adminArea2;

        private String postalCode;

        private String countryCode;

    }

    @Data
    @ToString
    public static class Vault {

        private String when;

    }

    @Data
    @ToString
    public static class Options {

        private Payee payee;

    }

    @Data
    @ToString
    public static class Payee {

        private String email;

    }

}