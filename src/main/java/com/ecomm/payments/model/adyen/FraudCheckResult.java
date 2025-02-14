package com.ecomm.payments.model.adyen;

import lombok.Data;

@Data
public class FraudCheckResult {

    private int accountScore;
    private int checkId;
    private String name;

}
