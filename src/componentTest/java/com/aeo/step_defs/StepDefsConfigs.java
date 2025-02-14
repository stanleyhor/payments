package com.aeo.step_defs;

import static com.aeo.constants.Constants.INTERNAL_AUTHORIZATION_ENDPOINT;
import static com.aeo.constants.Constants.REDIRECT_CHECKOUT_BASE_URL;

import com.aeo.client.TestClient;
import com.aeo.salad.cucumber.api.CucumberScenario;
import com.aeo.salad.stepDefinitions.ApiSteps;
import com.aeo.salad.stepDefinitions.BaseMethods;
import com.aeo.salad.stepDefinitions.atg.BaseGetViewAtgCartSteps;
import com.aeo.salad.stepDefinitions.shoppingcart.BaseViewCartSteps;
import com.aeo.utils.Configuration;
import com.aeo.utils.RandomUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.function.Function;

public class StepDefsConfigs {

    protected static final Configuration configuration = new Configuration();
    protected static final String INTERNAL_PAYMENTS_AUTHORIZE_PATH = configuration.getProperty("checkoutServiceUrl") + INTERNAL_AUTHORIZATION_ENDPOINT;
    protected static final Function<String, String> REDIRECT_CHECKOUT_URL = (value) -> REDIRECT_CHECKOUT_BASE_URL + value;

    protected final CucumberScenario scenarioContext = CucumberScenario.getInstance();
    protected final TestClient testClient = TestClient.getInstance();
    protected final RandomUtils randomUtils = new RandomUtils();
    protected final String address = configuration.getServiceUrl()
            + "payments/v1/auth";
    protected final BaseViewCartSteps viewCartSteps = new BaseViewCartSteps();
    protected final ApiSteps apiSteps = new ApiSteps();
    protected final BaseGetViewAtgCartSteps baseGetViewAtgCartSteps = new BaseGetViewAtgCartSteps();
    protected final BaseMethods baseMethods = BaseMethods.getInstance();
    protected ObjectNode requestBody;
    protected ObjectNode unconvertedResponse;

}