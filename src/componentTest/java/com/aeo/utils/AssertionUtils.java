package com.aeo.utils;

import static com.aeo.constants.Constants.CREDIT_CARD;
import static com.aeo.salad.helpers.configuration.ScenarioVariables.CURRENT_ORDER_NUMBER;
import static com.aeo.salad.helpers.utils.Constants.DEFAULT_PLACE_ORDER_RESPONSE_VARIABLE;

import com.aeo.model.response.afterPay.AfterPayAuthResponse;
import com.aeo.salad.helpers.data.order.ATGOrderApiResponse;
import com.aeo.salad.helpers.data.payment.model.PaymentGroupsItem;
import com.aeo.salad.helpers.data.payment.response.db_validations.PaymentAuthDbResponse;
import com.aeo.salad.helpers.data.payment.response.db_validations.PaymentHeader;
import com.aeo.step_defs.CommonStepDefs;

import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import java.math.BigDecimal;
import java.util.List;

public class AssertionUtils extends CommonStepDefs {

    public void assertTheAfterpayCheckoutResponse(
            AfterpayCheckoutResponse expectedAfterpayCheckoutResponse, AfterPayAuthResponse actualAfterPayAuthorizeResponse
    ) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actualAfterPayAuthorizeResponse.getData()
                    .getOrderNumber())
                    .as("The expected orderNumber ["
                            + expectedAfterpayCheckoutResponse.getOrderNumber()
                            + "] is not equal to the actual one ["
                            + actualAfterPayAuthorizeResponse.getData()
                                    .getOrderNumber()
                            + "]")
                    .isEqualTo("fromContext".equals(expectedAfterpayCheckoutResponse.getOrderNumber()) ? scenarioContext
                            .getVar(CURRENT_ORDER_NUMBER) : expectedAfterpayCheckoutResponse.getOrderNumber());
            softly.assertThat(actualAfterPayAuthorizeResponse.getData()
                    .getToken())
                    .isNotNull();
            softly.assertThat(actualAfterPayAuthorizeResponse.getData()
                    .getExpires())
                    .isNotNull();
            softly.assertThat(actualAfterPayAuthorizeResponse.getData()
                    .getRedirectCheckoutUrl())
                    .as("The expected redirectCheckoutUrl ["
                            + REDIRECT_CHECKOUT_URL.apply(actualAfterPayAuthorizeResponse.getData()
                                    .getToken())
                            + "] is not equal to the actual one ["
                            + actualAfterPayAuthorizeResponse.getData()
                                    .getRedirectCheckoutUrl()
                            + "]")
                    .isEqualTo(REDIRECT_CHECKOUT_URL.apply(actualAfterPayAuthorizeResponse.getData()
                            .getToken()));
            softly.assertThat(actualAfterPayAuthorizeResponse.getData()
                    .getResultCode())
                    .isEqualTo(expectedAfterpayCheckoutResponse.getResultCode());
            softly.assertAll();
        });
    }

    @Step("Asserting the actual and expected PaymentAuthDbResponse objects")
    public void assertDataInDBAfterPlacingTheCcGcOrder(PaymentAuthDbResponse expectedPaymentAuthDbResponse, PaymentAuthDbResponse actualPaymentAuthDbResponse) {

        ATGOrderApiResponse actualCheckoutResponse = ((Response) scenarioContext.getVar(DEFAULT_PLACE_ORDER_RESPONSE_VARIABLE)).as(ATGOrderApiResponse.class);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualPaymentAuthDbResponse.getOrderNumber())
                .isEqualTo(expectedPaymentAuthDbResponse.getOrderNumber());

        expectedPaymentAuthDbResponse.getPaymentHeaders()
                .forEach(expectedPayment -> {
                    PaymentHeader actualPayment = getActualPaymentByExpectedPaymentHeaderId(expectedPayment.getPaymentHeaderId(),
                            actualPaymentAuthDbResponse.getPaymentHeaders());
                    softly.assertThat(actualPayment.getPaymentHeaderId())
                            .isEqualTo(expectedPayment.getPaymentHeaderId());
                    softly.assertThat(actualPayment.getPaymentMethod())
                            .isEqualTo(expectedPayment.getPaymentMethod());
                    softly.assertThat(actualPayment.getOrderNumber())
                            .isEqualTo(expectedPayment.getOrderNumber());
                    softly.assertThat(actualPayment.getOrderRef())
                            .isEqualTo(expectedPayment.getOrderRef());
                    softly.assertThat(actualPayment.getSiteId())
                            .isEqualTo(expectedPayment.getSiteId());
                    softly.assertThat(actualPayment.getProfileId())
                            .isEqualTo(expectedPayment.getProfileId());
                    softly.assertThat(actualPayment.getGatewayIndicator())
                            .isNotNull();
                    softly.assertThat(actualPayment.getSubmittedDate())
                            .isNotNull();
                    softly.assertThat(actualPayment.getState())
                            .isNotNull();
                    softly.assertThat(actualPayment.getPaymentDetails())
                            .isNotNull();
                    softly.assertThat(actualPayment.getPaymentEvents())
                            .isNotNull();
                    softly.assertThat(actualPayment.getBillingAddress())
                            .isNotNull();
                    softly.assertThat(actualPayment.getLastModifiedBy())
                            .isNotNull();
                    softly.assertThat(actualPayment.getLastModifiedDate())
                            .isNotNull();

                    if (expectedPayment.getPaymentMethod()
                            .equalsIgnoreCase(CREDIT_CARD)) {
                        PaymentGroupsItem expectedPaymentHeader = getPaymentGroupFromCheckoutResponseByPaymentType(CREDIT_CARD, actualCheckoutResponse);

                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getCardType())
                                .isEqualTo(expectedPayment.getPaymentDetails()
                                        .getCardType());
                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getCardNumber())
                                .isEqualTo(expectedPaymentHeader.getCreditCardNumber());
                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getExpirationMonth())
                                .isEqualTo(expectedPaymentHeader.getExpirationMonth());
                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getExpirationYear())
                                .isEqualTo(expectedPaymentHeader.getExpirationYear());
                        softly.assertThat(actualPayment.getCurrencyCode())
                                .isEqualTo(expectedPaymentHeader.getCurrencyCode());
                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getCardAlias())
                                .isNotNull();
                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getFundingSource())
                                .isNotNull();
                        softly.assertThat(actualPayment.getPaymentDetails()
                                .getCardHolderName())
                                .isNotNull();
                        var paymentEvent = actualPayment.getPaymentEvents()
                                .get(0);
                        softly.assertThat(paymentEvent.getPaymentEventId())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getSubmittedDate())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getTxnType())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getTxnState())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getAmount())
                                .isGreaterThan(BigDecimal.ZERO);
                        softly.assertThat(paymentEvent.getPaymentReference())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getMerchantAccountName())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getMerchantReference())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getEventDetails())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getLastModifiedBy())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getLastModifiedDate())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getEventDetails()
                                .getAvsResponseCode())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getEventDetails()
                                .getCvvResponseCode())
                                .isNotNull();
                        softly.assertThat(paymentEvent.getEventDetails()
                                .getAuthCode())
                                .isNotNull();

                        var actualBillingAddress = actualPayment.getBillingAddress();
                        softly.assertThat(actualBillingAddress.getBillingAddressId())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getFirstName())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getLastName())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getAddress1())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getCity())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getCountry())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getPostalCode())
                                .isNotNull();
                        softly.assertThat(actualBillingAddress.getState())
                                .isNotNull();

                    }
                });

        softly.assertAll();
    }

    @Step("Asserting the actual and expected PaymentAuthDbResponse objects after AfterPay authorization")
    public void assertDataInDBAfterAuthorizingTheOrder(PaymentAuthDbResponse expectedPaymentAuthDbResponse, PaymentAuthDbResponse actualPaymentAuthDbResponse) {

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualPaymentAuthDbResponse.getOrderNumber())
                .isEqualTo(expectedPaymentAuthDbResponse.getOrderNumber());

        expectedPaymentAuthDbResponse.getPaymentHeaders()
                .forEach(expectedPayment -> {
                    PaymentHeader actualPayment = getActualPaymentByExpectedPaymentHeaderId(expectedPayment.getPaymentHeaderId(),
                            actualPaymentAuthDbResponse.getPaymentHeaders());
                    softly.assertThat(actualPayment.getPaymentHeaderId())
                            .isEqualTo(expectedPayment.getPaymentHeaderId());
                    softly.assertThat(actualPayment.getPaymentMethod())
                            .isEqualTo(expectedPayment.getPaymentMethod());
                    softly.assertThat(actualPayment.getOrderNumber())
                            .isEqualTo(expectedPayment.getOrderNumber());
                    softly.assertThat(actualPayment.getOrderRef())
                            .isEqualTo(expectedPayment.getOrderRef());
                    softly.assertThat(actualPayment.getGatewayIndicator())
                            .isEqualTo(expectedPayment.getGatewayIndicator());
                    softly.assertThat(actualPayment.getPaymentDetails()
                            .getAfterPayToken())
                            .isEqualTo(expectedPayment.getPaymentDetails()
                                    .getAfterPayToken());

                });
        softly.assertAll();
    }

    private PaymentHeader getActualPaymentByExpectedPaymentHeaderId(String expectedPaymentHeaderId, List<PaymentHeader> actualPayments) {
        return actualPayments.stream()
                .filter(payment -> payment.getPaymentHeaderId()
                        .equalsIgnoreCase(expectedPaymentHeaderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    private PaymentGroupsItem getPaymentGroupFromCheckoutResponseByPaymentType(String paymentType, ATGOrderApiResponse actualCheckoutResponse) {
        return actualCheckoutResponse.getData()
                .getPaymentGroups()
                .getItems()
                .stream()
                .filter(payment -> payment.getPaymentGroupClassType()
                        .equalsIgnoreCase(paymentType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment type ["
                        + paymentType
                        + "] not found"));
    }

}
