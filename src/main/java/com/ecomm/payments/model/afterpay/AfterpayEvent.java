package com.ecomm.payments.model.afterpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterpayEvent {

    private String id;
    private String created;
    private String expires;
    private String type;
    private AfterpayAmount amount;

}
