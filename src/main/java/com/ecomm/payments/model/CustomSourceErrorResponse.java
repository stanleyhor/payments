package com.ecomm.payments.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true,
        includeFieldNames = true)
@EqualsAndHashCode(callSuper = true)
public class CustomSourceErrorResponse extends CustomErrorResponse {

    private String source;

}
