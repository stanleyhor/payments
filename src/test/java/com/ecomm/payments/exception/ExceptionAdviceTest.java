package com.ecomm.payments.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.rest.PaymentAuthController;
import com.ecomm.payments.service.PaymentAuthorizationService;
import com.ecomm.payments.service.PaymentDetailsService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class ExceptionAdviceTest {

    private MockMvc mockMvc;

    @Mock
    PaymentAuthorizationService paymentAuthorizationServiceImpl;

    @Mock
    PaymentsConfig paymentConfig;

    @InjectMocks
    private PaymentAuthController paymentAuthController;

    @Mock
    PaymentDetailsService paymentDetailsServiceImpl;

    @Mock
    ExceptionAdvice advice;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentAuthController)
                .setControllerAdvice(new ExceptionAdvice())
                .build();
    }

    @Test
    public void testGlobalExceptionHandlerError() throws Exception {

        String exceptionResponse = "{\"message\": \"Billing address problem (Street)\","
                + "\"status\": 422,"
                + "\"errorCode\": \"132\","
                + "\"errorType\": \"validation\"}";

        Mockito.when(paymentAuthorizationServiceImpl.authorizePayment(any(ATGAuthRequest.class)))
                .thenThrow(new AdyenClientException(exceptionResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getATGAuthRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testGlobalExceptionHandlerFallback() throws Exception {

        String exceptionResponse = "{\"message\": \"Billing address problem (Street)\","
                + "\"status\": 422,"
                + "\"errorCode\": \"132\","
                + "\"errorType\": \"validation\"}";

        Mockito.when(paymentAuthorizationServiceImpl.authorizePayment(any(ATGAuthRequest.class)))
                .thenThrow(new AdyenClientFallbackException(exceptionResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getATGAuthRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testJsonExceptionHandlerError() throws Exception {

        String exceptionResponse = "000 Service 123 not present";

        Mockito.when(paymentAuthorizationServiceImpl.authorizePayment(any(ATGAuthRequest.class)))
                .thenThrow(new AdyenClientException(exceptionResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getATGAuthRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testJsonExceptionHandlerErrorforFallBack() throws Exception {

        String exceptionResponse = "Authorize Service not present";

        Mockito.when(paymentAuthorizationServiceImpl.authorizePayment(any(ATGAuthRequest.class)))
                .thenThrow(new AdyenClientFallbackException(exceptionResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getATGAuthRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testDuplicateAuthorizationException() throws Exception {

        String exceptionResponse = "Duplicate authorizaton";

        Mockito.when(paymentAuthorizationServiceImpl.authorizePayment(any(ATGAuthRequest.class)))
                .thenThrow(new DuplicateAuthorizationException(exceptionResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getATGAuthRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testGlobalPaymentsExceptionHandlerError() throws Exception {

        Mockito.when(paymentAuthorizationServiceImpl.authorizePayment(any(ATGAuthRequest.class)))
                .thenThrow(new NullPointerException("java.lang.NullPointerException"));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getATGAuthRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testValidateExceptionHandlerError() throws Exception {

        ATGAuthRequest atgAuthRequest = getATGAuthRequest();
        atgAuthRequest.setShopperInteraction(null);
        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/auth").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(atgAuthRequest)))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testGlobalExceptionHandlerErrorForDetailsAPI() throws Exception {

        String exceptionResponse = "{\"message\": \"PaymentData Issue\","
                + "\"status\": 422,"
                + "\"errorCode\": \"132\","
                + "\"errorType\": \"validation\"}";

        Mockito.when(paymentDetailsServiceImpl.retrieveDetailsResponse(any(PaymentDetailsRequest.class)))
                .thenThrow(new AdyenClientException(exceptionResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/details").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getPaymentDetailsRequest())))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testGlobalPaymentsExceptionHandlerErrorForDetailsAPI() throws Exception {

        Mockito.when(paymentDetailsServiceImpl.retrieveDetailsResponse(any(PaymentDetailsRequest.class)))
                .thenThrow(new NullPointerException("java.lang.NullPointerException"));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/details").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getPaymentDetailsRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testAuthFailedExceptionHandlerErrorForDetailsAPI() throws Exception {

        Mockito.when(paymentDetailsServiceImpl.retrieveDetailsResponse(any(PaymentDetailsRequest.class)))
                .thenThrow(new AuthorizationFailedException("error.checkout.placeOrder.paymentInfo.authorizationFailed"));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/details").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getPaymentDetailsRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testClientExceptionFor422Code() throws Exception {

        ClientErrorResponse clientErrorResponse = new ClientErrorResponse();
        clientErrorResponse.setStatus(422);
        Mockito.when(paymentDetailsServiceImpl.retrieveDetailsResponse(any(PaymentDetailsRequest.class)))
                .thenThrow(new ClientException(clientErrorResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/details").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getPaymentDetailsRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void testClientExceptionForOtherStatusCode() throws Exception {

        ClientErrorResponse clientErrorResponse = new ClientErrorResponse();
        clientErrorResponse.setStatus(500);
        Mockito.when(paymentDetailsServiceImpl.retrieveDetailsResponse(any(PaymentDetailsRequest.class)))
                .thenThrow(new ClientException(clientErrorResponse));

        MockHttpServletResponse response = mockMvc.perform(post("/payments/v1/details").contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getPaymentDetailsRequest())))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void testGlobalExceptionForDetails() throws Exception {
        String apiURL = "/payments/v1/details";
        Mockito.when(paymentDetailsServiceImpl.retrieveDetailsResponse(any(PaymentDetailsRequest.class)))
                .thenThrow(new ClientException(getClientErrorResponse()));
        MockHttpServletResponse response = mockMvc.perform(post(apiURL).contentType(MediaType.APPLICATION_JSON)
                .headers(getHeaders())
                .content(new Gson().toJson(getPaymentDetailsRequest())))
                .andReturn()
                .getResponse();
        if (apiURL.contains("details")) {
            assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        assertThat(response.getContentAsString()).isNotEmpty();
    }

    @Test
    public void testGlobalExceptionWithRequestURI() throws Exception {
        Object handleException = advice.handleException(new Exception("Internal Server Error"), "/v1/details");
        assertNull(handleException);
    }

    private ClientErrorResponse getClientErrorResponse() {
        ClientErrorResponse response = new ClientErrorResponse();
        response.setErrorCode("101");
        response.setErrorKey("error.checkout.placeOrder.paymentInfo.internalException");
        response.setMessage("Internal Server Error");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
    }

    private ATGAuthRequest getATGAuthRequest() {

        ATGAuthRequest atgAuthRequest = new ATGAuthRequest();
        atgAuthRequest.setPaymentGroupId("0032433433");
        atgAuthRequest.setAtgProfileId("3242323523532");
        atgAuthRequest.setOrderNumber("3242323523532");
        atgAuthRequest.setIdempotencyKey("UID-1");

        Amount amount = new Amount();
        amount.setValue("1500");
        amount.setCurrency("MXN");
        atgAuthRequest.setAmount(amount);

        AtgPaymentMethod paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType("creditcard");
        paymentMethod.setEncryptedCardNumber("test_4111111111111111");
        paymentMethod.setEncryptedExpiryMonth("test_03");
        paymentMethod.setEncryptedExpiryYear("test_2030");
        paymentMethod.setEncryptedCardNumber("test_737");
        paymentMethod.setStoredPaymentMethodId("8315901675746062");
        atgAuthRequest.setPaymentMethod(paymentMethod);

        AtgBillingAddress billingAddress = new AtgBillingAddress();
        billingAddress.setAddress1("22");
        billingAddress.setAddress2("Hot Metal st");
        billingAddress.setFirstName("Test");
        billingAddress.setLastName("User");
        billingAddress.setPhoneNumber("+14005885236");
        billingAddress.setPostalCode("15220");
        billingAddress.setState("PA");
        billingAddress.setCity("Pittsburgh");
        billingAddress.setCountry("US");
        atgAuthRequest.setBillingAddress(billingAddress);

        atgAuthRequest.setStorePaymentMethod(true);
        atgAuthRequest.setShopperInteraction("Ecommerce");
        atgAuthRequest.setReturnUrl("www.ae.com");

        return atgAuthRequest;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("aeSite", "AEO_MX");
        headers.add(PaymentsConstants.SITE_ID, "AEO_MX");
        headers.add(PaymentsConstants.IDEMPOTENCY_KEY, "UID-1");

        return headers;
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
