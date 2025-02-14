package com.ecomm.payments.model.braintree;

import java.util.List;

import lombok.Data;

@Data
public class Transaction {

    private String id;

    private String legacyId;

    private String createdAt;

    private PaymentMethod paymentMethod;

    private Amount amount;

    private String merchantAccountId;

    private String orderId;

    private String purchaseOrderNumber;

    private String status;

    private List<StatusHistory> statusHistory;

    private String source;

    private Shipping shipping;

    private Address billingAddress;

    private PaymentMethodSnapshot paymentMethodSnapshot;

    private Customer customer;

    @Data
    public static class PaymentMethod {

        private String id;

        private String legacyId;

    }

    @Data
    public static class Amount {

        private String value;

        private String currencyIsoCode;

    }

    @Data
    public static class StatusHistory {

        private String status;

        private String timestamp;

        private Amount amount;

    }

    @Data
    public static class PaymentMethodSnapshot {

        private Payer payer;

        private String authorizationId;

        private String customField;

        private String payerStatus;

        private String sellerProtectionStatus;

        private String taxId;

        private String transactionFee;

        private String description;

    }

    @Data
    public static class Payer {

        private String payerId;

        private String email;

        private String billingAgreementId;

        private String firstName;

        private String lastName;

        private String payerStatus;

    }

    @Data
    public static class Shipping {

        private Address shippingAddress;

    }

    @Data
    public static class Address {

        private String fullName;

        private String company;

        private String addressLine1;

        private String addressLine2;

        private String adminArea1;

        private String adminArea2;

        private String postalCode;

        private String countryCode;

        private String phoneNumber;

    }

    @Data
    public static class Customer {

        private String id;

    }

}