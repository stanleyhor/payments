package com.ecomm.payments.util;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.braintree.Address;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.Options;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.Payee;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.RiskData;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.Shipping;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.ShippingAddress;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.Transaction;
import com.ecomm.payments.model.braintree.BTPaypalAuthRequest.Vault;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse.AuthorizePayPalAccount;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse.BillingAgreementWithPurchasePaymentMethod;
import com.ecomm.payments.model.braintree.ClientTokenRequest;
import com.ecomm.payments.model.braintree.ClientTokenRequest.ClientToken;
import com.ecomm.payments.model.braintree.PaypalAuthResponse;
import com.ecomm.payments.model.braintree.Transaction.Payer;
import com.ecomm.payments.model.braintree.Transaction.PaymentMethodSnapshot;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BTPaypalMapper {

    private final PaymentsConfig paymentsConfig;

    public Map<String, Object> prepareAuthRequest(AuthorizationRequest authRequest, String siteId) {

        log.debug("BTPaypalMapper.prepareAuthRequest() - orderNumber: {}, siteId: {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        BTPaypalAuthRequest paypalAuthRequest = new BTPaypalAuthRequest();
        paypalAuthRequest.setClientMutationId(authRequest.getIdempotencyKey());

        paypalAuthRequest.setPaymentMethodId(authRequest.getPaymentMethod()
                .getPaypalToken());

        Transaction transaction = new Transaction();
        transaction.setOrderId(authRequest.getOrderNumber());
        transaction.setPurchaseOrderNumber(authRequest.getPaymentHeaderId());

        transaction.setMerchantAccountId(getBraintreeMerchantAccount(siteId));
        transaction.setChannel(authRequest.getContext()
                .getChannelType());
        transaction.setAmount(authRequest.getAmount()
                .getValue());

        Address requestShippingAddress = authRequest.getShippingAddress();

        if (requestShippingAddress != null) {
            Shipping shipping = new Shipping();
            ShippingAddress shippingAddress = new ShippingAddress();

            shippingAddress.setFirstName(requestShippingAddress.getFirstName());
            shippingAddress.setLastName(requestShippingAddress.getLastName());
            shippingAddress.setAddressLine1(requestShippingAddress.getAddress1());
            shippingAddress.setAddressLine2(requestShippingAddress.getAddress2());
            shippingAddress.setAdminArea2(requestShippingAddress.getCity());
            shippingAddress.setAdminArea1(requestShippingAddress.getState());
            shippingAddress.setCountryCode(requestShippingAddress.getCountry());
            shippingAddress.setPostalCode(requestShippingAddress.getPostalCode());

            shipping.setShippingAddress(shippingAddress);

            transaction.setShipping(shipping);
        }

        RiskData riskData = new RiskData();
        riskData.setDeviceData(authRequest.getDeviceData());
        transaction.setRiskData(riskData);

        Vault vault = new Vault();
        vault.setWhen("ON_SUCCESSFUL_TRANSACTION");
        transaction.setVaultPaymentMethodAfterTransacting(vault);

        paypalAuthRequest.setTransaction(transaction);

        if (paymentsConfig.isSendPaypalPayeeEmail()) {
            Options options = new Options();
            Payee payee = new Payee();

            payee.setEmail(getPaypalPayeeEmail(siteId));
            options.setPayee(payee);

            paypalAuthRequest.setOptions(options);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("input", paypalAuthRequest);

        log.info("BTPaypalMapper.prepareAuthRequest() :: BT PAYPAL Auth request orderNumber: {}  Request: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), new Gson().toJson(paypalAuthRequest));

        return map;

    }

    public String getBraintreeMerchantAccount(String siteId) {

        return paymentsConfig.getBraintreeSiteIdMerchantIdMap()
                .get(siteId);

    }

    public String getBraintreeResultCode(String status) {

        return paymentsConfig.getBraintreeResultCodeMap()
                .get(status);

    }

    public String getPaypalPayeeEmail(String siteId) {

        return paymentsConfig.getPaypalPayeeEmailMap()
                .get(siteId);

    }

    public PaypalAuthResponse prepareAuthResponse(AuthorizationRequest authRequest, BTPaypalAuthResponse btAuthPaypalResponse) {

        log.debug("BTPaypalMapper.prepareAuthResponse() - orderNumber: {}, siteId: {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()));

        PaypalAuthResponse paypalAuthResponse = new PaypalAuthResponse();

        if (ObjectUtils.isNotEmpty(btAuthPaypalResponse)
                && ObjectUtils.isNotEmpty(btAuthPaypalResponse.getAuthorizePayPalAccount())) {

            AuthorizePayPalAccount authorizePayPalAccount = btAuthPaypalResponse.getAuthorizePayPalAccount();

            BillingAgreementWithPurchasePaymentMethod billingAgreementWithPurchasePaymentMethod = authorizePayPalAccount
                    .getBillingAgreementWithPurchasePaymentMethod();

            if (ObjectUtils.isNotEmpty(billingAgreementWithPurchasePaymentMethod)) {

                paypalAuthResponse.setAuthToken(billingAgreementWithPurchasePaymentMethod.getId());

            }

            com.ecomm.payments.model.braintree.Transaction transaction = authorizePayPalAccount.getTransaction();

            if (ObjectUtils.isNotEmpty(transaction)) {

                paypalAuthResponse.setOrderNumber(authRequest.getOrderNumber());
                paypalAuthResponse.setPaymentHeaderId(authRequest.getPaymentHeaderId());

                paypalAuthResponse.setMerchantAccount(transaction.getMerchantAccountId());

                paypalAuthResponse.setGraphqlId(transaction.getId());

                paypalAuthResponse.setTransactionLegacyId(transaction.getLegacyId());

                paypalAuthResponse.setResultCode(getBraintreeResultCode(transaction.getStatus()));

                paypalAuthResponse.setTransactionTimestamp(transaction.getCreatedAt());

                if (ObjectUtils.isNotEmpty(transaction.getPaymentMethodSnapshot())) {

                    paypalAuthResponse.setAuthCode(transaction.getPaymentMethodSnapshot()
                            .getAuthorizationId());

                    paypalAuthResponse.setPayer(getPayer(transaction.getPaymentMethodSnapshot()));

                    setBillingAddressEmail(authRequest, transaction.getPaymentMethodSnapshot());
                    paypalAuthResponse.setBillingAddress(authRequest.getBillingAddress());

                }

                if (ObjectUtils.isNotEmpty(transaction.getCustomer())) {

                    paypalAuthResponse.setCustomerId(transaction.getCustomer()
                            .getId());
                }

                paypalAuthResponse.setAmount(getAmount(transaction));

            }

            log.info("BTPaypalMapper.prepareAuthResponse() :: Paypal Auth Response orderNumber: {}  Response: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), new Gson().toJson(paypalAuthResponse));

        }

        return paypalAuthResponse;
    }

    private Payer getPayer(PaymentMethodSnapshot paymentMethodSnapshot) {

        if (ObjectUtils.isNotEmpty(paymentMethodSnapshot.getPayer())) {

            paymentMethodSnapshot.getPayer()
                    .setPayerStatus(paymentMethodSnapshot.getPayerStatus());
        }

        return paymentMethodSnapshot.getPayer();

    }

    private Address setBillingAddressEmail(AuthorizationRequest authRequest, PaymentMethodSnapshot paymentMethodSnapshot) {

        if (ObjectUtils.isNotEmpty(paymentMethodSnapshot.getPayer())
                && ObjectUtils.isEmpty(authRequest.getBillingAddress()
                        .getEmail())) {
            authRequest.getBillingAddress()
                    .setEmail(paymentMethodSnapshot.getPayer()
                            .getEmail());
        }
        return authRequest.getBillingAddress();
    }

    public PaypalAuthResponse prepareAuthErrorResponse(AuthorizationRequest authRequest, ClientGraphQlResponse graphQlResponse) {

        PaypalAuthResponse paypalAuthResponse = new PaypalAuthResponse();

        paypalAuthResponse.setOrderNumber(authRequest.getOrderNumber());
        paypalAuthResponse.setPaymentHeaderId(authRequest.getPaymentHeaderId());
        paypalAuthResponse.setResultCode(PaymentsConstants.AUTH_REFUSED);
        paypalAuthResponse.setRefusalReason(graphQlResponse.getErrors()
                .get(0)
                .getMessage());
        paypalAuthResponse.setBillingAddress(authRequest.getBillingAddress());

        return paypalAuthResponse;
    }

    private Amount getAmount(com.ecomm.payments.model.braintree.Transaction transaction) {

        Amount amount = new Amount();
        amount.setValue(transaction.getAmount()
                .getValue());
        amount.setCurrency(transaction.getAmount()
                .getCurrencyIsoCode());

        return amount;
    }

    public Map<String, Object> prepareCreateClientTokenRequest(String siteId) {

        log.debug("BTPaypalMapper.prepareCreateClientTokenRequest() - siteId: {}", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        ClientToken clientToken = new ClientToken(getBraintreeMerchantAccount(siteId));

        ClientTokenRequest clientTokenRequest = new ClientTokenRequest(UUID.randomUUID()
                .toString(), clientToken);

        Map<String, Object> map = new HashMap<>();
        map.put("input", clientTokenRequest);

        log.info("BTPaypalMapper.prepareCreateClientTokenRequest() :: BT PAYPAL Client Token Request: {}", new Gson().toJson(clientTokenRequest));

        return map;
    }

}
