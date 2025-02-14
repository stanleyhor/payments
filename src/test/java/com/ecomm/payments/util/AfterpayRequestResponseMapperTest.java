package com.ecomm.payments.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecomm.payments.model.atg.AdyenEnhancedSchemeData;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AfterpayRequestResponseMapperTest {

    private static AfterpayRequestResponseMapper mapper;

    @BeforeAll
    static void initSetUp() {
        mapper = new AfterpayRequestResponseMapper();
    }

    @Test
    void testConvertToCheckoutAfterpayRequest() {
        var request = RequestResponseUtilTest.getAfterpayAuthRequest();

        var paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType("afterpay");
        request.setPaymentMethod(paymentMethod);
        var checkoutAfterpayRequest = mapper.convertToCheckoutRequest(request);
        assertNotNull(checkoutAfterpayRequest);

        request.setCheckoutType("REGULAR");
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingMethod("2DAY");
        checkoutAfterpayRequest = mapper.convertToCheckoutRequest(request);
        assertNotNull(checkoutAfterpayRequest);

        var enhancedSchemeData = new AdyenEnhancedSchemeData();
        enhancedSchemeData.setTotalTaxAmount(0);
        request.setEnhancedSchemeData(enhancedSchemeData);
        paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType("cashApp");
        request.setPaymentMethod(paymentMethod);
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingMethod("STD");
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        request.getAdyenFraudDetail()
                .setCouponCode(null);
        checkoutAfterpayRequest = mapper.convertToCheckoutRequest(request);
        assertNotNull(checkoutAfterpayRequest);

        request.getAdyenFraudDetail()
                .setCouponCode("promo200001,1894VCAF27B9WG6JM38G");
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingMethod("STD");
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        checkoutAfterpayRequest = mapper.convertToCheckoutRequest(request);
        assertNotNull(checkoutAfterpayRequest);

        request.getAdyenFraudDetail()
                .setCouponCode(
                        "promo200001,1894VCAF27B9WG6JM38G,promo290029,promo950001,promo940004,promo1640011,promo380008,1U62UVAE9MA8PA9SV94W,1M99WUAF2RJ3DG6VE6PS");
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingMethod("STD");
        request.getAdyenFraudDetail()
                .getShippingDetail()
                .setShippingAddress(null);
        checkoutAfterpayRequest = mapper.convertToCheckoutRequest(request);
        assertNotNull(checkoutAfterpayRequest);
    }

    @Test
    void testBuildCheckoutAfterpayResponse() {
        var response = mapper.buildCheckoutResponse(RequestResponseUtilTest.getCheckoutResponseString(), "0012345678");
        assertNotNull(response);
        assertNotNull(response.getOrderNumber());
        assertEquals("0012345678", response.getOrderNumber());
        assertNotNull(response.getToken());
        assertEquals("002.il3hr22qtllhetnu8ahmg4an3jbm3dhldokppafo3ncimkj4", response.getToken());
        assertNotNull(response.getRedirectCheckoutUrl());
        assertNotNull(response.getExpires());
    }

    @Test
    void testConvertToAuthorizeRequest() {
        var authRequest = RequestResponseUtilTest.getAuthDetailsRequest();

        var authorizeRequest = mapper.convertToAuthorizeRequest(authRequest);
        assertNotNull(authorizeRequest);
        assertNotNull(authorizeRequest.getRequestId());
        assertNotNull(authorizeRequest.getToken());
        assertNotNull(authorizeRequest.getMerchantReference());
        assertEquals("00012345678", authorizeRequest.getMerchantReference());
        assertNotNull(authorizeRequest.getAmount());
        assertEquals("50.0", authorizeRequest.getAmount()
                .getAmount());

        authRequest.setOrderNumber(null);
        authorizeRequest = mapper.convertToAuthorizeRequest(authRequest);
        assertNull(authorizeRequest.getMerchantReference());
    }

}
