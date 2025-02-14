package com.ecomm.payments.model.atg;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FraudDetail {

    private String deviceFingerPrint;
    private String customerIp;
    private String orderDiscountAmount;
    private String couponCode;
    private String commerceItemCount;
    private List<CommerceItem> commerceItems;
    private ContactInfo contactInfo;
    private ShippingDetail shippingDetail;
    private Integer totalItems;
    private boolean hasLoyalty;
    private Integer daysSinceRegistration;

}
