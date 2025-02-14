package com.ecomm.payments.model.afterpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consumer {

    private String phoneNumber;
    private String givenNames;
    private String surname;
    private String email;

}
