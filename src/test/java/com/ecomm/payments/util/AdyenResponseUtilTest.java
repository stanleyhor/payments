package com.ecomm.payments.util;

import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdyenResponseUtilTest {

    public static String getAdyenAuthResponseString() {
        return """
            					{
                "additionalData": {
                    "authCode": "074950",
                    "avsResult": "1 Address matches, postal code doesn\u0027t",
                    "avsResultRaw": "1",
                    "cardSummary": "1111",
                    "expiryDate": "3/2030",
                    "cvcResult": "1 Matches",
                    "cardBin": "555544",
                    "recurringDetailReference": "JFDWFGW8G3M84H82",
                    "recurringProcessingModel": "CardOnFile",
                    "paymentMethod": "mc",
                    "shopperReference": "ugp2450600816",
                    "cardPaymentMethod": "mc",
                    "cardIssuingCountry": "GB",
                    "scaExemptionRequested": "transactionRiskAnalysis",
                    "alias": "E271147590277301",
                    "paymentAccountReference": "wk1mJ5PTTAaRiQUey31aln1izdkSH",
                    "issuerBin": "55554444"
                },
                "pspReference": "NT2N4NRFQV5X8N82",
                "resultCode": "Authorised",
                "amount": {
                    "value": "26438",
                    "currency": "USD"
                },
                "merchantReference": "0003190228-pg396680344",
                "status": 0
            } """;
    }

    public static AdyenAuthResponse getAdyenAuthResponse() {
        try {
            var mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(getAdyenAuthResponseString(), AdyenAuthResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAdyenDetailsResponseString() {
        return """
            					{
                "additionalData": {
                    "authCode": "074950",
                    "avsResult": "1 Address matches, postal code doesn\u0027t",
                    "avsResultRaw": "1",
                    "cardSummary": "1111",
                    "expiryDate": "3/2030",
                    "cvcResult": "1 Matches",
                    "cardBin": "555544",
                    "recurringDetailReference": "JFDWFGW8G3M84H82",
                    "recurringProcessingModel": "CardOnFile",
                    "paymentMethod": "mc",
                    "shopperReference": "ugp2450600816",
                    "cardPaymentMethod": "mc",
                    "cardIssuingCountry": "GB",
                    "scaExemptionRequested": "transactionRiskAnalysis",
                    "alias": "E271147590277301",
                    "paymentAccountReference": "wk1mJ5PTTAaRiQUey31aln1izdkSH",
                    "issuerBin": "55554444",
                    "fraudResultType": "GREEN"
                },
                "pspReference": "NT2N4NRFQV5X8N82",
                "resultCode": "Authorised",
                "amount": {
                    "value": "26438",
                    "currency": "USD"
                },
                "merchantReference": "0003190228-pg396680344"
            } """;
    }

    public static AdyenDetailsResponse getAdyenDetailsResponse() {
        try {
            var mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(getAdyenDetailsResponseString(), AdyenDetailsResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
