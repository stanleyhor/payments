package com.aeo.step_libraries;

import static com.aeo.constants.Constants.AFTERPAY;
import static com.aeo.constants.Constants.CREDIT_CARD;
import static com.aeo.constants.ScenarioVariables.AUTHORIZE_PAYMENT_RESPONSE;
import static com.aeo.salad.helpers.configuration.ScenarioVariables.CURRENT_GC_PAYMENT_INFO_RESPONSE;
import static com.aeo.salad.helpers.configuration.ScenarioVariables.CURRENT_ORDER_NUMBER;
import static com.aeo.salad.helpers.configuration.ScenarioVariables.CURRENT_PAYMENT_INFO_RESPONSE;
import static com.aeo.salad.helpers.rest.RequestParametersUtils.getRequestParameters;
import static com.aeo.salad.stepDefinitions.ApiSteps.DEFAULT_RESPONSE_VARIABLE;
import static com.ecomm.payments.constants.PaymentsConstants.GIFT_CARD;
import static com.google.api.client.http.HttpMethods.POST;

import com.aeo.constants.CheckedPaymentMethods;
import com.aeo.model.cucumber_model.AuthorizationRequestInfo;
import com.aeo.model.request.AuthorizationRequest;
import com.aeo.model.request.CartFlags;
import com.aeo.model.request.RedirectUrls;
import com.aeo.model.response.PaymentsAuthDBResponseInfo;
import com.aeo.model.response.afterPay.AfterPayAuthResponse;
import com.aeo.salad.helpers.data.order.ATGOrderApiResponse;
import com.aeo.salad.helpers.data.payment.response.PaymentInfoResponse;
import com.aeo.salad.helpers.data.payment.response.db_validations.PaymentAuthDbResponse;
import com.aeo.salad.helpers.data.payment.response.db_validations.PaymentDetails;
import com.aeo.salad.helpers.data.payment.response.db_validations.PaymentHeader;
import com.aeo.step_defs.StepDefsConfigs;
import com.aeo.utils.JsonUtils;
import com.aeo.utils.TestDataUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthStepLibrary extends StepDefsConfigs {

    public ObjectNode getAuthRequest(CheckedPaymentMethods checkedPaymentMethods) throws IOException, URISyntaxException {
        return JsonUtils.readFromJson(checkedPaymentMethods.getPathToRequest(), ObjectNode.class);
    }

    @Step("Authorizing the payment")
    public void authorizePayment(String orderIdFromContext, Response viewBagResponse, List<AuthorizationRequestInfo> authorizationRequestInfoList) {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        Double totalSum = Double.parseDouble(viewBagResponse.jsonPath()
                .get("data.summary.total")
                .toString());
        Double giftCardTotal = Double.parseDouble(viewBagResponse.jsonPath()
                .get("data.summary.giftCardTotal")
                .toString());
        for (AuthorizationRequestInfo authorizationRequestInfo : authorizationRequestInfoList) {
            authorizationRequest = buildPaymentsAuthorizationRequest(authorizationRequestInfo, orderIdFromContext, totalSum, giftCardTotal);
            log.info("authorization request: \n"
                    + authorizationRequest);
        }
        Response response = baseMethods.sendRequest(POST, INTERNAL_PAYMENTS_AUTHORIZE_PATH, getRequestParameters(), authorizationRequest);
        log.info("payments authorize response:\n"
                + response.prettyPrint());
        scenarioContext.setVar(AUTHORIZE_PAYMENT_RESPONSE, response);
        scenarioContext.setVar(DEFAULT_RESPONSE_VARIABLE, response);
        scenarioContext.setVar(CURRENT_ORDER_NUMBER, authorizationRequest.getOrderSummary()
                .getOrderNumber());
    }

    @Step("Building payments authorization request")
    public AuthorizationRequest buildPaymentsAuthorizationRequest(
            AuthorizationRequestInfo authorizationRequestInfo, String orderIdFromContext, Double totalSum, Double giftCardTotal
    ) {
        return AuthorizationRequest.builder()
                .orderSummary(TestDataUtils.buildOrderSummary(authorizationRequestInfo, orderIdFromContext, totalSum, giftCardTotal))
                .shippingDetail(Collections.singletonList(TestDataUtils.buildShippingDetails(authorizationRequestInfo)))
                .adyenFraudDetail(TestDataUtils.buildAdyenFraudDetail())
                .commerceItems(TestDataUtils.buildCommerceItems(authorizationRequestInfo))
                .flags(CartFlags.builder()
                        .afterpayEligible(authorizationRequestInfo.isAfterpayEligible())
                        .cashAppPayEligible(authorizationRequestInfo.isCashAppPayEligible())
                        .build())
                .redirectUrls(RedirectUrls.builder()
                        .build())
                .altPayDetails(TestDataUtils.buildAltPayDetails(authorizationRequestInfo))
                .build();
    }

    /**
     * After a payment is applied to order, the response of POST /paymentInfo is stored in the context. This method extracts the /paymentInfo responses from the context, based on the paymentVariation
     * specified in the .feature file. EG.: | paymentVariation | | afterPay | Then, puts them in the List<PaymentInfoResponse>.
     *
     * @param expectedAuthDbResponseList - the expected payments specified in the feature file
     * @return expectedResponseList - the list of /paymentInfo responses
     */
    @Step("Getting the expected payment info response list from context")
    public List<PaymentInfoResponse> getExpectedPaymentInfoResponseListFromContext(final List<PaymentsAuthDBResponseInfo> expectedAuthDbResponseList) {

        List<PaymentInfoResponse> expectedResponseList = new ArrayList<>();

        if (!onlyGiftCardsArePresent(expectedAuthDbResponseList)) {
            PaymentInfoResponse paymentInfoResponse = (PaymentInfoResponse) scenarioContext.getVar(CURRENT_PAYMENT_INFO_RESPONSE);
            verifyTheExpectedPaymentIsPresentInThePaymentInfoResponse(expectedAuthDbResponseList, paymentInfoResponse.getData()
                    .getAttributes()
                    .getPaymentVariation());

            expectedResponseList.add(paymentInfoResponse);
        }

        addGiftCardsToExpectedResponseListIfPresent(expectedAuthDbResponseList, expectedResponseList);

        return expectedResponseList;
    }

    private boolean onlyGiftCardsArePresent(List<PaymentsAuthDBResponseInfo> expectedAuthDbResponseList) {
        return expectedAuthDbResponseList.stream()
                .map(PaymentsAuthDBResponseInfo::getPaymentVariation)
                .allMatch(variation -> variation.equalsIgnoreCase(GIFT_CARD));
    }

    /**
     * The /paymentInfo response for the GiftCards are stored in context in a separate variable from other payments. This method adds the /paymentInfo responses for the giftCards to
     * List<PaymentInfoResponse> if the "giftCard" payment variation is present in the .feature file. EG.: | paymentVariation | | giftCard |
     */
    @SuppressWarnings("unchecked")
    private void addGiftCardsToExpectedResponseListIfPresent(
            List<PaymentsAuthDBResponseInfo> expectedAuthDbResponseList, List<PaymentInfoResponse> expectedResponseList
    ) {
        boolean giftCardsArePresent = expectedAuthDbResponseList.stream()
                .map(PaymentsAuthDBResponseInfo::getPaymentVariation)
                .anyMatch(variation -> variation.equalsIgnoreCase(GIFT_CARD));

        if (giftCardsArePresent) {
            List<PaymentInfoResponse> giftCardPaymentInfoResponseList = (List<PaymentInfoResponse>) scenarioContext.getVar(CURRENT_GC_PAYMENT_INFO_RESPONSE);
            expectedResponseList.addAll(giftCardPaymentInfoResponseList);
        }
    }

    /**
     * The /paymentInfo response for the GiftCards are stored in context in a separate variable from other payments. This method verifies paymentInfoResponse contains the paymentVariation specified in the
     * .feature file: EG.: | paymentVariation | | creditCard |
     */
    private void verifyTheExpectedPaymentIsPresentInThePaymentInfoResponse(
            List<PaymentsAuthDBResponseInfo> expectedAuthDbResponseList, String actualPaymentVariation
    ) {

        Optional<String> expectedPaymentVariations = expectedAuthDbResponseList.stream()
                .map(PaymentsAuthDBResponseInfo::getPaymentVariation)
                .filter(paymentVariation -> !paymentVariation.equalsIgnoreCase("giftCard"))
                .findFirst();

        if (!expectedPaymentVariations.get()
                .equalsIgnoreCase(actualPaymentVariation)) {
            throw new IllegalArgumentException("Specified payment variation ["
                    + expectedPaymentVariations.get()
                    + "] is not found in the /paymentInfo response object");
        }
    }

    @Step("Building the expected PaymentAuthDbResponse object from /paymentInfo response and /checkout Response")
    public PaymentAuthDbResponse buildExpectedPaymentAuthDbResponseForCcAndGC(
            List<PaymentInfoResponse> actualPaymentInfoResponseList, ATGOrderApiResponse actualCheckoutResponse
    ) {
        PaymentAuthDbResponse expectedPaymentAuthDbResponse = new PaymentAuthDbResponse();
        expectedPaymentAuthDbResponse.setOrderNumber(actualCheckoutResponse.getData()
                .getOrderNumber());

        List<PaymentHeader> expectedAuthDbResponseList = new ArrayList<>();
        actualPaymentInfoResponseList.forEach(actualPaymentInfoResponse -> {

            PaymentHeader actualPaymentHeader = new PaymentHeader();
            actualPaymentHeader.setPaymentHeaderId(actualPaymentInfoResponse.getData()
                    .getId());
            actualPaymentHeader.setPaymentMethod(actualPaymentInfoResponse.getData()
                    .getAttributes()
                    .getPaymentMethod());
            actualPaymentHeader.setOrderNumber(actualCheckoutResponse.getData()
                    .getOrderNumber());
            actualPaymentHeader.setOrderRef(actualCheckoutResponse.getData()
                    .getOrderNumber());
            actualPaymentHeader.setSiteId(actualCheckoutResponse.getData()
                    .getSiteId());
            actualPaymentHeader.setProfileId(actualCheckoutResponse.getData()
                    .getProfileId());

            PaymentDetails actualPaymentDetails = new PaymentDetails();
            if (actualPaymentInfoResponse.getData()
                    .getAttributes()
                    .getPaymentVariation()
                    .equalsIgnoreCase(CREDIT_CARD)) {
                actualPaymentDetails.setCardType(actualPaymentInfoResponse.getData()
                        .getAttributes()
                        .getCreditCardType());
            }

            actualPaymentHeader.setPaymentDetails(actualPaymentDetails);

            expectedAuthDbResponseList.add(actualPaymentHeader);
        });
        expectedPaymentAuthDbResponse.setPaymentHeaders(expectedAuthDbResponseList);

        return expectedPaymentAuthDbResponse;
    }

    @Step("Building the expected PaymentAuthDbResponse object from /paymentInfo response and /authorize Response")
    public PaymentAuthDbResponse buildExpectedPaymentAuthDbResponseForAfterPay(
            List<PaymentInfoResponse> paymentInfoResponseList, AfterPayAuthResponse afterPayAuthorizeResponse
    ) {

        PaymentAuthDbResponse expectedPaymentAuthDbResponse = new PaymentAuthDbResponse();
        expectedPaymentAuthDbResponse.setOrderNumber(afterPayAuthorizeResponse.getData()
                .getOrderNumber());

        List<PaymentHeader> expectedAuthDbResponseList = new ArrayList<>();
        paymentInfoResponseList.forEach(paymentInfoResponse -> {

            PaymentHeader actualPaymentHeader = new PaymentHeader();
            actualPaymentHeader.setPaymentHeaderId(paymentInfoResponse.getData()
                    .getId());
            actualPaymentHeader.setPaymentMethod(paymentInfoResponse.getData()
                    .getAttributes()
                    .getPaymentVariation());
            actualPaymentHeader.setOrderNumber(afterPayAuthorizeResponse.getData()
                    .getOrderNumber());
            actualPaymentHeader.setOrderRef(afterPayAuthorizeResponse.getData()
                    .getOrderNumber());
            actualPaymentHeader.setGatewayIndicator(AFTERPAY);

            Response response = (Response) scenarioContext.getVar(AUTHORIZE_PAYMENT_RESPONSE);
            AfterPayAuthResponse afterPayAuthResponse = response.as(AfterPayAuthResponse.class);

            PaymentDetails actualPaymentDetails = new PaymentDetails();
            actualPaymentDetails.setSourceType(paymentInfoResponse.getData()
                    .getAttributes()
                    .getPaymentVariation());
            actualPaymentDetails.setAfterPayToken(afterPayAuthResponse.getData()
                    .getToken());

            actualPaymentHeader.setPaymentDetails(actualPaymentDetails);

            expectedAuthDbResponseList.add(actualPaymentHeader);
        });
        expectedPaymentAuthDbResponse.setPaymentHeaders(expectedAuthDbResponseList);

        return expectedPaymentAuthDbResponse;
    }

}