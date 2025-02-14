package com.aeo.model.cucumber_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationRequestInfo {

    private String orderNumber;
    private String cartId;
    private String currencyCode;
    private String channelType;
    private String profileId;
    private String address1;
    private String city;
    private String country;
    private String firstName;
    private String lastName;
    private String postalCode;
    private String state;
    private String shippingMethod;
    private String email;
    private String phoneNo;
    private boolean afterpayEligible;
    private boolean cashAppPayEligible;
    private String returnUrl;
    private String paypalToken;
    private String amount;

}
