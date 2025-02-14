package com.ecomm.payments.model;

import com.ecomm.payments.model.adyen.AdditionalDataRequest;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.Installments;
import com.ecomm.payments.model.atg.AdyenEnhancedSchemeData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.atg.FraudDetail;
import com.ecomm.payments.model.atg.RequestContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ATGAuthRequest {

    private String paymentGroupId;
    private String atgProfileId;
    private String orderNumber;
    private Amount amount;
    private AtgBillingAddress billingAddress;
    private AtgPaymentMethod paymentMethod;
    private boolean storePaymentMethod;
    @NotEmpty
    private String shopperInteraction;
    private String returnUrl;
    @JsonProperty("context")
    private RequestContext requestContext;
    private FraudDetail adyenFraudDetail;
    private AdditionalDataRequest additionalData;
    private String webStoreId;
    private String idempotencyKey;
    // Added for 3DS
    private BrowserInfo browserInfo;
    private String executeThreeD;
    private String channel;
    // added for PAYMS-127
    private String shopperLocale;
    private String siteId;
    private AdyenEnhancedSchemeData enhancedSchemeData;
    private boolean reauth;
    private Installments installments;
    private boolean retroCharge;
    private boolean editAddress;
    private String checkoutType;
    private String afterpayShippingOptionIdentifier;

}
