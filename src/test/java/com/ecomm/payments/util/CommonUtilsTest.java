package com.ecomm.payments.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import org.junit.jupiter.api.Test;

public class CommonUtilsTest {

    @Test
    void testPaymentMethod() {
        var paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType(PaymentsConstants.PAYPAL_PAYMENT_METHOD);
        assertNotNull(CommonUtils.getPaymentMethod(paymentMethod));
        assertNull(CommonUtils.getPaymentMethod(null));
    }

    @Test
    void testGetDetailsRequestChannelType() {
        assertNotNull(CommonUtils.getDetailsRequestChannelType(null));
        var detailsRequest = AdyenTestUtils.getPaymentDetailsRequest();
        assertNotNull(CommonUtils.getDetailsRequestChannelType(detailsRequest));
        detailsRequest.setOrderSummary(null);
        assertNotNull(CommonUtils.getDetailsRequestChannelType(detailsRequest));
    }

    @Test
    void testGetDetailsRequestCheckoutType() {
        var checkoutType = CommonUtils.getDetailsRequestCheckoutType(null);
        assertNotNull(checkoutType);
        assertEquals("regular", checkoutType);
        var detailsRequest = AdyenTestUtils.getPaymentDetailsRequest();
        detailsRequest.getOrderSummary()
                .setCheckoutType("EXPRESS");
        checkoutType = CommonUtils.getDetailsRequestCheckoutType(detailsRequest);
        assertNotNull(checkoutType);
        assertEquals("EXPRESS", checkoutType);
        detailsRequest.setOrderSummary(null);
        checkoutType = CommonUtils.getDetailsRequestCheckoutType(detailsRequest);
        assertNotNull(checkoutType);
        assertEquals("regular", checkoutType);
    }

}
