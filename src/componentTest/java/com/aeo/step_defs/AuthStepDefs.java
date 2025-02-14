package com.aeo.step_defs;

import static com.aeo.constants.ScenarioVariables.AUTHORIZE_PAYMENT_RESPONSE;
import static com.aeo.constants.ScenarioVariables.PAYMENT_METHOD_FIELD_NAME;
import static com.aeo.salad.helpers.configuration.ScenarioVariables.CURRENT_PAYMENT_AUTH_DB_RESPONSE;
import static com.aeo.salad.helpers.utils.Constants.DEFAULT_PLACE_ORDER_RESPONSE_VARIABLE;

import com.aeo.constants.CheckedPaymentMethods;
import com.aeo.constants.CreditCards;
import com.aeo.model.cucumber_model.AuthorizationRequestInfo;
import com.aeo.model.response.PaymentsAuthDBResponseInfo;
import com.aeo.model.response.afterPay.AfterPayAuthResponse;
import com.aeo.salad.helpers.data.order.ATGOrderApiResponse;
import com.aeo.salad.helpers.data.payment.response.PaymentInfoResponse;
import com.aeo.salad.helpers.data.payment.response.db_validations.PaymentAuthDbResponse;
import com.aeo.step_libraries.AuthStepLibrary;
import com.aeo.utils.AssertionUtils;
import com.aeo.utils.JsonUtils;

import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import org.junit.Assert;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class AuthStepDefs extends StepDefsConfigs {

    private final AuthStepLibrary authStepLibrary = new AuthStepLibrary();
    private final AssertionUtils assertionUtils = new AssertionUtils();

    @Given("read from file {string}")
    public void getAuthData(String pm) throws IOException, URISyntaxException {
        CheckedPaymentMethods checkedPaymentMethods = CheckedPaymentMethods.valueOf(pm);
        scenarioContext.setVar(PAYMENT_METHOD_FIELD_NAME, checkedPaymentMethods);
        requestBody = authStepLibrary.getAuthRequest(checkedPaymentMethods);

    }

    @Given("set credit card type {string}")
    public void setPaymentMethod(String cc) throws IOException, URISyntaxException {
        CreditCards creditCardType = CreditCards.valueOf(cc);
        ObjectNode paymentMethod = JsonUtils.readFromJson(creditCardType.getFileName(), ObjectNode.class);
        requestBody.remove(PAYMENT_METHOD_FIELD_NAME);
        requestBody.setAll(paymentMethod);
    }

    @Given("User tries to get response")
    public void getAuthData() {
        testClient.sendHTTPRequest(RequestMethod.GET, address);
    }

    @When("user sets prop {string} = {string}")
    public void setAmountProperty(String property, String value) {
        JsonUtils.changeJsonProperty(requestBody, property, value);
    }

    @When("user sets random idempotencyKey")
    public void idempotencyKeyRandom() {
        requestBody.put("idempotencyKey", randomUtils.randomIdempotencyKey());
    }

    @When("user sets random webstoreId")
    public void setRandonWebstoreId() {
        requestBody.put("webStoreId", randomUtils.randomWebStoreId());
    }

    @Given("User tries to post request")
    @Attachment
    public ResponseOptions<Response> postAuth() {
        return testClient.sendHTTPRequest(RequestMethod.POST, address, requestBody.toString());
    }

    @Then("User tries to get request without paymentGroupId")
    public void getAuthDataWithoutSomeField() {
        testClient.sendHTTPRequest(RequestMethod.GET, address);
    }

    @Then("property {string} is {string}")
    public void checkProperty(String property, String value) {
        Assert.assertEquals(value, JsonUtils.getPropertyByHierarchy(unconvertedResponse, property)
                .textValue());
    }

    @Then("property {string} is not empty")
    public void checkPropertyEmptiness(String property) {
        String propValue = JsonUtils.getPropertyByHierarchy(unconvertedResponse, property)
                .textValue();
        Assert.assertNotNull(propValue);
        Assert.assertFalse(propValue.isEmpty());
    }

    @Then("compare schema of saved and received response for {string}")
    public void checkResponse(String pm) throws IOException, URISyntaxException {
        ObjectNode savedResponse = JsonUtils.readFromJson(CheckedPaymentMethods.valueOf(pm)
                .getPathToResponse(), ObjectNode.class);
        Allure.addAttachment("Saved response", savedResponse.toPrettyString());
        Allure.addAttachment("Received response", unconvertedResponse.toPrettyString());
        Assert.assertTrue(JsonUtils.compareJsonStructures(savedResponse, unconvertedResponse));
    }

    @And("get data from scenarioContext")
    public void getDataFromScenarioContext() {
        Response response = (Response) scenarioContext.getVar("response");
        unconvertedResponse = response.as(ObjectNode.class);
    }

    @When("user views the bag")
    public void whenUserViewsTheBag() {
        Response viewBagResponse = viewCartSteps.responseFromViewCartWithRefererAndInventoryCheckSuccessfullySaved(true);
        scenarioContext.setVar("viewBagResponse", viewBagResponse);
    }

    @When("user views the atg cart")
    public void whenUserTheAtgCart() {
        String site = apiSteps.getSiteFromContext();
        Response response = baseGetViewAtgCartSteps.responseFromAtgViewCartForSiteAndInventoryCheckSavedToVariable(site, "true", "cartResponse");
        scenarioContext.setVar("viewAtgCardResponse", response);
    }

    @When("user authorizes the payment with the following data")
    public void userAuthorizesThePaymentWithTheFollowingData(final List<AuthorizationRequestInfo> authorizationRequestInfoList) {
        String orderIdFromContext = (String) scenarioContext.getVar("orderId");
        Response viewBagResponse = (Response) scenarioContext.getVar("viewBagResponse");
        authStepLibrary.authorizePayment(orderIdFromContext, viewBagResponse, authorizationRequestInfoList);
    }

    @Then("the authorization response contains the following data")
    public void theResponseContainsTheFollowingData(final AfterpayCheckoutResponse expectedAfterpayCheckoutResponse) {
        Response response = (Response) scenarioContext.getVar(AUTHORIZE_PAYMENT_RESPONSE);
        AfterPayAuthResponse actualAfterPayAuthorizeResponse = response.as(AfterPayAuthResponse.class);
        assertionUtils.assertTheAfterpayCheckoutResponse(expectedAfterpayCheckoutResponse, actualAfterPayAuthorizeResponse);
    }

    @Then("the correct data is returned after placing the order")
    public void theCorrectDataIsReturnedAfterPlacingTheOrder(final List<PaymentsAuthDBResponseInfo> expectedAuthDbResponseList) {
        List<PaymentInfoResponse> actualPaymentInfoResponseList = authStepLibrary.getExpectedPaymentInfoResponseListFromContext(expectedAuthDbResponseList);
        ATGOrderApiResponse actualCheckoutResponse = ((Response) scenarioContext.getVar(DEFAULT_PLACE_ORDER_RESPONSE_VARIABLE)).as(ATGOrderApiResponse.class);
        PaymentAuthDbResponse expectedPaymentAuthDbResponse = authStepLibrary.buildExpectedPaymentAuthDbResponseForCcAndGC(actualPaymentInfoResponseList,
                actualCheckoutResponse);
        PaymentAuthDbResponse actualPaymentAuthDbResponse = (PaymentAuthDbResponse) scenarioContext.getVar(CURRENT_PAYMENT_AUTH_DB_RESPONSE);
        assertionUtils.assertDataInDBAfterPlacingTheCcGcOrder(expectedPaymentAuthDbResponse, actualPaymentAuthDbResponse);
    }

    @Then("the correct data is returned after authorizing the order")
    public void theCorrectDataIsReturnedAfterAuthorizingTheOrder(final List<PaymentsAuthDBResponseInfo> expectedAuthDbResponseList) {
        List<PaymentInfoResponse> paymentInfoResponseList = authStepLibrary.getExpectedPaymentInfoResponseListFromContext(expectedAuthDbResponseList);
        Response response = (Response) scenarioContext.getVar(AUTHORIZE_PAYMENT_RESPONSE);
        AfterPayAuthResponse afterPayAuthorizeResponse = response.as(AfterPayAuthResponse.class);
        PaymentAuthDbResponse expectedPaymentAuthDbResponse = authStepLibrary.buildExpectedPaymentAuthDbResponseForAfterPay(paymentInfoResponseList,
                afterPayAuthorizeResponse);
        PaymentAuthDbResponse actualPaymentAuthDbResponse = (PaymentAuthDbResponse) scenarioContext.getVar(CURRENT_PAYMENT_AUTH_DB_RESPONSE);
        assertionUtils.assertDataInDBAfterAuthorizingTheOrder(expectedPaymentAuthDbResponse, actualPaymentAuthDbResponse);
    }

}
