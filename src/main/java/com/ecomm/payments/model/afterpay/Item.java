package com.ecomm.payments.model.afterpay;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private String name;
    private String sku;
    private long quantity;
    private String pageUrl;
    private String imageUrl;
    private AfterpayAmount price;
    private List<List<String>> categories;

}
