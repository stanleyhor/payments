package com.ecomm.payments.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import org.junit.jupiter.api.Test;

public class MockPaymentDetailsServiceImplTest {

    private MockPaymentDetailsServiceImpl mockServiceImpl = new MockPaymentDetailsServiceImpl();

    @Test
    void testAuthorize() throws Exception {
        PaymentDetailsResponse detailsResponse = mockServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertThat(detailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber()).isEqualTo("3242323523512");
    }

    @Test
    void testAuthorizeWithNullInput() throws Exception {
        PaymentDetailsResponse detailsResponse = mockServiceImpl.retrieveDetailsResponse(null);

        assertThat(detailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()).isNotEmpty();
    }

    @Test
    void testAuthorizeWithNullPaymentData() throws Exception {

        PaymentDetailsRequest request = getPaymentDetailsRequest();
        request.setPaymentData(null);
        PaymentDetailsResponse detailsResponse = mockServiceImpl.retrieveDetailsResponse(request);

        assertThat(detailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber()).isEqualTo("3242323523512");
    }

    @Test
    void testAuthorizeWithNonPaypalData() throws Exception {

        PaymentDetailsRequest request = getPaymentDetailsRequest();
        request.getDetails()
                .setPayerID(null);
        PaymentDetailsResponse detailsResponse = mockServiceImpl.retrieveDetailsResponse(request);

        assertThat(detailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber()).isEqualTo("3242323523512");
    }

    private PaymentDetailsRequest getPaymentDetailsRequest() {
        PaymentDetailsRequest req = new PaymentDetailsRequest();
        req.setPaymentData("Ab02b4c0!BQABAgBfCrSUe16NDHS+1/TwWsBvleWljic8sWJCGuXYGRcE+b.....");
        // Details Data
        Details details = new Details();
        details.setBillingToken(null);
        details.setFacilitatorAccessToken("A21AAJTTtypi1DyMeo_b5HWaBbCBLr1Nsbxe5Z9jysphR...");
        details.setOrderID("EC-0BY583878Y0568026");
        details.setPayerID("9P5BDS6BFY4K8");
        details.setPaymentID("123333");
        req.setDetails(details);
        return req;
    }

}
