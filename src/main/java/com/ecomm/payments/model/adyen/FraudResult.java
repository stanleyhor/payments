package com.ecomm.payments.model.adyen;

import java.util.List;

import lombok.Data;

@Data
public class FraudResult {

    private int accountScore;
    private List<FraudCheckResult> results;

}
