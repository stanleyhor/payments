package com.ecomm.payments.model.braintree;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Address {

    private String firstName;

    private String lastName;

    private String address1;

    private String address2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private String neighborhood;

    private String phoneNumber;

    private String email;

}
