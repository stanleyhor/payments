package com.ecomm.payments.model.adyen;

import lombok.Data;

@Data
public class DeliveryAddress {

    private String houseNumberOrName;
    private String street;
    private String city;
    private String stateOrProvince;
    private String postalCode;
    private String country;

}
