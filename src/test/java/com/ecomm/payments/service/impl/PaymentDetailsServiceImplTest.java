package com.ecomm.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.exception.AdyenClientException;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.OrderPaymentDetails;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.PaymentStatus;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.util.AdyenResponseUtilTest;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.PaymentDetailsRequestResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PaymentDetailsServiceImplTest {

    @Mock
    private PaymentDetailsRequestResponseMapper mapper;

    @Mock
    private RestTemplate restTemplate;

    public static ObjectMapper objMapper;

    private PaymentDetailsServiceImpl paymentDetailsServiceImpl;

    private static PaymentsConfig paymentsConfig;

    @Mock
    private ClientErrorResponse clientErrorResponse;

    @Mock
    private ClientException clientException;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @BeforeAll
    static void setUp() throws IOException {
        objMapper = new ObjectMapper();
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        paymentsConfig = new PaymentsConfig();
        paymentsConfig.setPaymentsDetailsEndPoint("http://localhost:");
    }

    @BeforeEach
    void initialize() throws AdyenClientException {

        paymentDetailsServiceImpl = new PaymentDetailsServiceImpl(mapper, paymentsConfig, restTemplate, authorizationUtils);
    }

    @Test
    void getAdyenDetailsResponse() throws Exception {

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(AdyenResponseUtilTest.getAdyenDetailsResponse()));

        ResponseEntity<AdyenDetailsResponse> adyenMono = paymentDetailsServiceImpl.callService(mockedRequest());

        assertNotNull(adyenMono);

    }

    @Test
    void getAdyenDetailsErrorResponse() throws Exception {

        RestClientResponseException ex = new RestClientResponseException("", 422, null, null, objMapper.writeValueAsBytes(getClientErrorResponse()), null);

        clientErrorResponse = new Gson().fromJson(ex.getResponseBodyAsString(), ClientErrorResponse.class);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenThrow(ex);

        assertThrows(ClientException.class, () -> paymentDetailsServiceImpl.callService(mockedRequest()));

        assertNotNull(new ClientException(clientErrorResponse));
    }

    @Test
    void getPaymentDetailsResponse() throws Exception {

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(AdyenResponseUtilTest.getAdyenDetailsResponse()));

        Mockito.when(authorizationUtils.processAndStoreAdyenAuthDetailsResponse(any(PaymentDetailsRequest.class), any(AdyenDetailsResponse.class)))
                .thenReturn(paymentDetailsMockedResponse());

        PaymentDetailsResponse paymentDetailsResponse = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse);

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        var detailsResponse1 = paymentDetailsMockedResponse();

        detailsResponse1.getData()
                .setOrderPaymentDetails(null);

        var adyenDetailsResponse1 = AdyenResponseUtilTest.getAdyenDetailsResponse();
        adyenDetailsResponse1.getAdditionalData()
                .setFraudResultType(null);

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(adyenDetailsResponse1));

        PaymentDetailsResponse paymentDetailsResponse5 = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse5);

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        var detailsResponse3 = paymentDetailsMockedResponse();

        detailsResponse3.getData()
                .getOrderPaymentDetails()
                .setPaymentStatus(null);

        var adyenDetailsResponse = AdyenResponseUtilTest.getAdyenDetailsResponse();
        adyenDetailsResponse.setAdditionalData(null);
        adyenDetailsResponse.setMerchantReference("0023456325");

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(adyenDetailsResponse));

        PaymentDetailsResponse paymentDetailsResponse6 = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse6);

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        var paymentDetails4 = paymentDetailsMockedResponse();

        paymentDetails4.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .set(0, null);

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(new AdyenDetailsResponse()));

        PaymentDetailsResponse paymentDetailsResponse7 = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse7);

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(null));

        PaymentDetailsResponse paymentDetailsResponse3 = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNull(paymentDetailsResponse3);

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        var detailsResponse = paymentDetailsMockedResponse();

        detailsResponse.setData(null);

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(new AdyenDetailsResponse()));

        PaymentDetailsResponse paymentDetailsResponse4 = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse4);

        Mockito.when(mapper.convertDetailsRequest(any(PaymentDetailsRequest.class)))
                .thenReturn(mockedRequest());

        var paymentDetails6 = paymentDetailsMockedResponse();

        paymentDetails6.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .setBillingAddress(null);

        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(AdyenDetailsResponse.class)))
                .thenReturn(ResponseEntity.status(200)
                        .body(new AdyenDetailsResponse()));

        PaymentDetailsResponse paymentDetailsResponse8 = paymentDetailsServiceImpl.retrieveDetailsResponse(getPaymentDetailsRequest());

        assertNotNull(paymentDetailsResponse8);

    }

    @Test
    public void fallback() {

        clientErrorResponse = ClientErrorResponse.defaultErrorResponse;

        assertThrows(ClientException.class, () -> paymentDetailsServiceImpl.fallbackGetPaymentDetails(getPaymentDetailsRequest(), null));

        assertNotNull(clientErrorResponse);
    }

    private AdyenDetailsRequest mockedRequest() {
        AdyenDetailsRequest detailsRequest = new AdyenDetailsRequest();
        return detailsRequest;
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

    private ClientErrorResponse getClientErrorResponse() {
        ClientErrorResponse response = new ClientErrorResponse();
        response.setErrorCode("101");
        response.setErrorKey("error.checkout.placeOrder.paymentInfo.internalException");
        response.setMessage("Internal Server Error");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
    }

}