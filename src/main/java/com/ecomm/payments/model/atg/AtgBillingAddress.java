package com.ecomm.payments.model.atg;

import lombok.Data;

@Data
public class AtgBillingAddress {

    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String email;

}
