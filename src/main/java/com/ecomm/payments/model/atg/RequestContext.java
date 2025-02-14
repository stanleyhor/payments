package com.ecomm.payments.model.atg;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestContext {

    private String source;
    private String eventType;
    private String channelType;
    private String gateway;

}
