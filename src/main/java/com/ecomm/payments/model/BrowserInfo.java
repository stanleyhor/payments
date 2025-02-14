package com.ecomm.payments.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class BrowserInfo {

    private String userAgent;
    private String acceptHeader;
    private String language;
    private int colorDepth;
    private int screenHeight;
    private int screenWidth;
    private int timeZoneOffset;
    private boolean javaEnabled;

}
