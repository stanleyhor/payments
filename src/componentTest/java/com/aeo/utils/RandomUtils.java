package com.aeo.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomUtils {

    public String randomIdempotencyKey() {
        return RandomStringUtils.randomAscii(35);
    }

    public String randomWebStoreId() {
        return RandomStringUtils.randomNumeric(5);
    }

}