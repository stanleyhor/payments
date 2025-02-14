package com.ecomm.payments.model.atg;

import lombok.Data;

@Data
public class CommerceItem {

    private String commerceItemId;
    private String quantity;
    private String salePrice;
    private String displayName;
    private String productUPC;
    private String sku;
    // Added for ATGCOM-1697
    private String recipientName;
    private String recipientEmail;
    private String recipientMobile;
    private String giftMessage;
    private String itemType;

}
