package com.ecomm.payments.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecomm.payments.config.TestPaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AuthorizationFailedException;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.adyen.AdyenAdditionalData;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.adyen.BillingAddress;
import com.ecomm.payments.model.adyen.FraudResult;
import com.ecomm.payments.model.atg.ATGAdditionalData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
public class PaymentDetailsRequestResponseMapperTest {

    public static final String APPLE_PAY = "applepay";
    public static final String DISCOVER_APPLEPAY = "discover_applepay";
    private static PaymentDetailsRequestResponseMapper mapper;

    private HttpHeaders headers;
    private PaymentDetailsRequest paymentDetailsRequest;
    private AdyenDetailsResponse adyenDetailsResponse;

    @BeforeAll
    static void initSetUp() {
        mapper = new PaymentDetailsRequestResponseMapper(TestPaymentsConfig.getPaymentConfig(), new CommonUtils(TestPaymentsConfig.getPaymentConfig()));
    }

    @BeforeEach
    public void setUpDetailsData() {
        // Details Request
        paymentDetailsRequest = new PaymentDetailsRequest();
        paymentDetailsRequest.setPaymentData("Ab02b4c0!BQABAgBfCrSUe16NDHS+1/TwWsBvleWljic8sWJCGuXYGRcE+b.....");
        // Details Data
        Details details = new Details();
        details.setBillingToken(null);
        details.setFacilitatorAccessToken("A21AAJTTtypi1DyMeo_b5HWaBbCBLr1Nsbxe5Z9jysphR...");
        details.setOrderID("EC-0BY583878Y0568026");
        details.setPayerID("9P5BDS6BFY4K8");
        details.setPaymentID("123333");
        paymentDetailsRequest.setDetails(details);

        // Adyen Details Response
        adyenDetailsResponse = new AdyenDetailsResponse();
        adyenDetailsResponse.setPspReference("852597229203094H");
        adyenDetailsResponse.setResultCode("Authorised");
        adyenDetailsResponse.setMerchantReference("3242323523512-pg1234556");

        AdyenAdditionalData additionalData = new AdyenAdditionalData();
        additionalData.setAuthCode("012097");
        additionalData.setFraudManualReview("false");
        additionalData.setFraudResultType("GREEN");
        additionalData.setPaypalEmail("paypaltest@adyen.com");
        additionalData.setPaypalPayerId("LF5HCWWBRV2KL");
        additionalData.setPaypalPayerResidenceCountry("NL");
        additionalData.setPaypalPayerStatus("unverified");
        additionalData.setPaypalProtectionEligibility("Ineligible");
        additionalData.setAuthorisedAmountValue("10000");
        additionalData.setAuthorisedAmountCurrency("MXN");
        // Paypal Billing Address
        BillingAddress address = new BillingAddress();
        address.setHouseNumberOrName("Sur. 77");
        address.setStreet("Colonia Santa Anita");
        address.setCity("Mexico City");
        address.setStateOrProvince("CDMX");
        address.setPostalCode("08300");
        address.setCountry("MX");
        additionalData.setBillingAddress(address);
        adyenDetailsResponse.setAdditionalData(additionalData);

        Amount amount = new Amount();
        amount.setValue("1500");
        amount.setCurrency("MXN");
        adyenDetailsResponse.setAmount(amount);

        FraudResult fraudResult = new FraudResult();
        fraudResult.setAccountScore(50);
        adyenDetailsResponse.setFraudResult(fraudResult);

        headers = new HttpHeaders();
        headers.add(PaymentsConstants.SITE_ID, "AEO_MX");
    }

    @Test
    public void getNullDetailsRequest() {

        AdyenDetailsRequest convertDetailsRequest = mapper.convertDetailsRequest(null);

        assertThat(convertDetailsRequest).isNull();
    }

    @Test
    public void getNotNullPaymentDataForDetailsAPI() {

        AdyenDetailsRequest convertDetailsRequest = mapper.convertDetailsRequest(paymentDetailsRequest);

        assertThat(convertDetailsRequest.getPaymentData()).isNotNull();
    }

    @Test
    public void getNullPaymentDataForDetailsAPI() {
        paymentDetailsRequest.setPaymentData(null);
        AdyenDetailsRequest convertDetailsRequest = mapper.convertDetailsRequest(paymentDetailsRequest);

        assertThat(convertDetailsRequest.getPaymentData()).isNull();
    }

    @Test
    public void getNullPaymentDetailsResponseForPaypal() {
        adyenDetailsResponse = null;
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertNull(convertDetailsResponse);
    }

    @Test
    public void getOrderPaymentDetailsCurrencyCodeForPaypal() {
        adyenDetailsResponse.setAmount(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getCurrencyCode()).isNotEmpty();
    }

    @Test
    public void getNullAmoutForPaypalDetailsAPI() {
        adyenDetailsResponse.setAmount(null);
        adyenDetailsResponse.getAdditionalData()
                .setAuthorisedAmountCurrency(null);
        adyenDetailsResponse.getAdditionalData()
                .setAuthorisedAmountValue(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getCurrencyCode()).isNull();
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAmountAuthorized()).isLessThan(1);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getCurrencyCode()).isNull();
    }

    @Test
    public void getNotNullAmoutForPaypalDetailsAPI() {
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getCurrencyCode()).isEqualTo("MXN");
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAmountAuthorized()).isEqualTo(1500);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getCurrencyCode()).isEqualTo("MXN");
    }

    @Test
    public void getNotNullMerchantReferenceForPaypalDetailsAPI() {
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);

        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentGroupId()).isEqualTo("pg1234556");
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber()).isEqualTo("3242323523512");
    }

    @Test
    public void getNullMerchantReferenceForPaypalDetailsAPI() {
        adyenDetailsResponse.setMerchantReference(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);

        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentGroupId()).isNull();
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber()).isNull();
    }

    @Test
    public void getPGIdMerchantReferenceForPaypalDetailsAPI() {
        adyenDetailsResponse.setMerchantReference("pg1234556");
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);

        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentGroupId()).isEqualTo("pg1234556");
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getOrderNumber()).isNull();
    }

    @Test
    public void getFraudManual() {
        adyenDetailsResponse.getAdditionalData()
                .setFraudManualReview(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .isFraudManualReview()).isEqualTo(false);
    }

    @Test
    public void getTrueFraudManualForPaypal() {
        adyenDetailsResponse.getAdditionalData()
                .setFraudManualReview("true");
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .isFraudManualReview()).isEqualTo(true);
    }

    @Test
    public void getAdditionalDataNullForPaypal() {
        adyenDetailsResponse.setAdditionalData(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .isFraudManualReview()).isEqualTo(false);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAdditionalData()).isNotNull();
    }

    @Test
    public void getAdditionalDataForPaypal() {
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        ATGAdditionalData additionalData = convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAdditionalData();
        assertThat(additionalData.getPaypalEmail()).isNotBlank();
        assertThat(additionalData.getPaypalPayerId()).isNotBlank();
        assertThat(additionalData.getPaypalPayerResidenceCountry()).isNotBlank();
        assertThat(additionalData.getPaypalPayerStatus()).isNotBlank();
        assertThat(additionalData.getPaypalProtectionEligibility()).isNotBlank();
    }

    @Test
    public void getAtgBillingAddressForPaypal() {
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getBillingAddress()
                .getAddress1()).isNotEmpty();
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getBillingAddress()
                .getAddress2()).isNotEmpty();
    }

    @Test
    public void getNullAtgBillingAddressForPaypal() {
        adyenDetailsResponse.getAdditionalData()
                .setBillingAddress(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getBillingAddress()).isNull();
    }

    @Test
    public void getNullFraudScoreForPaypalDetailsAPI() {
        adyenDetailsResponse.setFraudResult(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAdditionalData()
                .getFraudScore()).isEqualTo(0);
    }

    @Test
    public void getFraudScoreForPaypalDetailsAPI() {
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAdditionalData()
                .getFraudScore()).isEqualTo(50);
    }

    @Test
    public void getNullAuthFailedResultCode() {
        adyenDetailsResponse.setResultCode(null);
        PaymentDetailsResponse convertDetailsResponse = null;
        try {
            convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        } catch (AuthorizationFailedException e) {
            assertThat(e.getMessage()).isNotNull();
        }
        assertThat(convertDetailsResponse).isNull();
    }

    @Test
    public void getAuthFailedResultCode() {
        adyenDetailsResponse.setResultCode("Cancelled");
        PaymentDetailsResponse convertDetailsResponse = null;
        try {
            convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        } catch (AuthorizationFailedException e) {
            assertThat(e.getMessage()).isNotNull();
        }
        assertThat(convertDetailsResponse).isNull();
    }

    @Test
    public void getPaymentTypeForPaypal() {
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentType()).isEqualTo("paypal");
    }

    @Test
    public void getPaymentTypeFor3Ds() {
        Details details = paymentDetailsRequest.getDetails();
        details.setPayerID(null);
        details.setMd("Ab02b4c0!BQABAgCW5sxB4e");
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentType()).isEqualTo("creditCard");
    }

    @Test
    public void getPaymentTypeForNullDetails() {
        paymentDetailsRequest.setDetails(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentType()).isEqualTo("creditCard");
    }

    @Test
    public void checkFraudManualReviewTypeNull() {
        adyenDetailsResponse.getAdditionalData()
                .setFraudResultType(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getAdditionalData()
                .getFraudResultType()).isEqualTo("GREEN");
    }

    @Test
    public void checkPayerIDNull() {
        Details details = paymentDetailsRequest.getDetails();
        details.setPayload("Ab02b4c0!BQABAgCW5sxB4e");
        details.setPayerID(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentType()).isEqualTo("paypal");
    }

    @Test
    public void checkPayLoadNull() {
        Details details = paymentDetailsRequest.getDetails();
        details.setPayload(null);
        details.setPayerID("Ab02b4c0!BQABAgCW5sxB4e");
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentType()).isEqualTo("paypal");
    }

    @Test
    public void checkPayloadANDPayderIDNull() {
        Details details = paymentDetailsRequest.getDetails();
        details.setPayload(null);
        details.setPayerID(null);
        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(adyenDetailsResponse, paymentDetailsRequest);
        assertThat(convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0)
                .getPaymentType()).isEqualTo("creditCard");
    }

    @Test
    public void testvalidateAuthToken() {

        var request = paymentDetailsRequest;
        request.setDetails(null);

        var response = adyenDetailsResponse;

        response.getAdditionalData()
                .setRecurringDetailReference(null);

        PaymentDetailsResponse convertDetailsResponse = mapper.convertDetailsResponse(response, request);

        var paymentStatus = convertDetailsResponse.getData()
                .getOrderPaymentDetails()
                .getPaymentStatus()
                .get(0);

        assertThat(paymentStatus.getPaymentType()).isEqualTo("creditCard");

        assertThat(paymentStatus.getResultCode()).isEqualTo(PaymentsConstants.AUTH_PENDING);
    }

}
