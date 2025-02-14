package com.aeo.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class AuthorizationRequest {

    private OrderSummary orderSummary;
    private List<ShippingDetails> shippingDetail;
    private FraudDetail adyenFraudDetail;
    private List<CommerceItem> commerceItems;
    private RedirectUrls redirectUrls;
    private AltPayDetails altPayDetails;
    private CartFlags flags;

}
