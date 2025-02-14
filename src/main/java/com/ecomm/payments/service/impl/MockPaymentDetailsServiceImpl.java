package com.ecomm.payments.service.impl;

import com.ecomm.payments.model.OrderPaymentDetails;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.PaymentStatus;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.service.PaymentDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MockPaymentDetailsServiceImpl implements PaymentDetailsService {

    @Override
    public PaymentDetailsResponse retrieveDetailsResponse(PaymentDetailsRequest input) {
        log.info("PaymentDetailsResponse {}", input);
        PaymentDetailsResponse response = buildMockATGDetailsResponse();
        log.info("Mock PaymentDetailsResponse {}", response);
        return response;
    }

    private PaymentDetailsResponse buildMockATGDetailsResponse() {
        PaymentDetailsResponse response = new PaymentDetailsResponse();
        ResponseData data = new ResponseData();
        OrderPaymentDetails orderDetails = new OrderPaymentDetails();
        orderDetails.setCurrencyCode("MXN");
        orderDetails.setOrderNumber("3242323523512");
        // Payment Status
        List<PaymentStatus> status = new ArrayList<>();
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.setPaymentGroupId("pg1234556");
        paymentStatus.setPaymentType("paypal");
        paymentStatus.setResultCode("AUTHORIZED");
        paymentStatus.setAmountAuthorized(1000);
        paymentStatus.setCurrencyCode("MXN");
        paymentStatus.setPspReference("852597229203094H");
        paymentStatus.setIdempotencyKey("UID-12345");
        paymentStatus.setFraudManualReview(false);

        AtgBillingAddress address = new AtgBillingAddress();
        address.setAddress1("Colonia Santa Anita");
        address.setAddress2("Sur. 77");
        address.setCity("Mexico City");
        address.setState("CDMX");
        address.setPostalCode("08300");
        address.setCountry("MX");
        address.setFirstName("FirstName");
        address.setLastName("LastName");
        address.setPhoneNumber("+525536018270");
        paymentStatus.setBillingAddress(address);

        ATGAdditionalData additionalData = new ATGAdditionalData();
        additionalData.setPaypalEmail("paypaltest@adyen.com");
        additionalData.setPaypalPayerId("LF5HCWWBRV2KL");
        additionalData.setPaypalPayerResidenceCountry("NL");
        additionalData.setPaypalPayerStatus("unverified");
        additionalData.setPaypalProtectionEligibility("Ineligible");
        additionalData.setAuthCode("012097");
        additionalData.setFraudResultType("GREEN");
        additionalData.setFraudScore(50);
        paymentStatus.setAdditionalData(additionalData);

        status.add(paymentStatus);
        orderDetails.setPaymentStatus(status);
        data.setOrderPaymentDetails(orderDetails);
        response.setData(data);
        return response;
    }

}
