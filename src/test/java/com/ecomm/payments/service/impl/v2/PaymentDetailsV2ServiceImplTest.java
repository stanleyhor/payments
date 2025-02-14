package com.ecomm.payments.service.impl.v2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.OrderPaymentDetails;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.PaymentStatus;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.util.AdyenResponseUtilTest;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.PaymentDetailsRequestResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PaymentDetailsV2ServiceImplTest {

    @InjectMocks
    private PaymentDetailsV2ServiceImpl paymentDetailsV2ServiceImpl;

    @Mock
    private PaymentDetailsRequestResponseMapper requestResponseMapper;

    @Mock
    private AdyenV2CallServiceImpl adyenV2CallServiceImpl;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @BeforeEach
    void initialize() {
        MockitoAnnotations.openMocks(this);
        paymentDetailsV2ServiceImpl = new PaymentDetailsV2ServiceImpl(requestResponseMapper, adyenV2CallServiceImpl, authorizationUtils);
    }

    @Test
    void testRetrieveAuthorizationDetails() {

        var paymentDetailsResponse = paymentDetailsV2ServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        Mockito.when(requestResponseMapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(new AdyenDetailsRequest());

        Mockito.when(adyenV2CallServiceImpl.retrieveAuthDetails(any(AdyenDetailsRequest.class)))
                .thenReturn(null);

        paymentDetailsResponse = paymentDetailsV2ServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());
        assertNull(paymentDetailsResponse);

        Mockito.when(adyenV2CallServiceImpl.retrieveAuthDetails(any(AdyenDetailsRequest.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        paymentDetailsResponse = paymentDetailsV2ServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());
        assertNull(paymentDetailsResponse);

        Mockito.when(adyenV2CallServiceImpl.retrieveAuthDetails(any(AdyenDetailsRequest.class)))
                .thenReturn(new ResponseEntity<AdyenDetailsResponse>(AdyenResponseUtilTest.getAdyenDetailsResponse(), HttpStatus.OK));

        Mockito.when(authorizationUtils.processAndStoreAdyenAuthDetailsResponse(any(PaymentDetailsRequest.class), any(AdyenDetailsResponse.class)))
                .thenReturn(paymentDetailsMockedResponse());

        paymentDetailsResponse = paymentDetailsV2ServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse);
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

    private PaymentDetailsResponse paymentDetailsMockedResponse() {
        PaymentDetailsResponse response = new PaymentDetailsResponse();
        ResponseData data = new ResponseData();
        OrderPaymentDetails orderDetails = new OrderPaymentDetails();
        orderDetails.setCurrencyCode("MXN");
        orderDetails.setOrderNumber("3242323523512");
        // Payment Status
        List<PaymentStatus> status = new ArrayList<PaymentStatus>();
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

        com.ecomm.payments.model.atg.ATGAdditionalData additionalData = new com.ecomm.payments.model.atg.ATGAdditionalData();
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
