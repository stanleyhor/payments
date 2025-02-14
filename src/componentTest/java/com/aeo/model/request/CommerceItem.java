package com.aeo.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CommerceItem {

    private String productUPC;
    private String displayName;
    private int quantity;
    private String commerceItemId;
    private String salePrice;
    private String recipientEmail;
    private String recipientName;
    private String giftMessage;
    private String recipientMobile;
    private String sku;

}
