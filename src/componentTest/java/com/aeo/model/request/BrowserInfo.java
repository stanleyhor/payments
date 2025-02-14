package com.aeo.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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