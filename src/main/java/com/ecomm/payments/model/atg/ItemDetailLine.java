package com.ecomm.payments.model.atg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ItemDetailLine {

    private String commodityCode;
    private String description;
    private String productCode;
    private int quantity;
    private String unitOfMeasure;
    private long unitPrice;
    private long discountAmount;
    private long totalAmount;

}
