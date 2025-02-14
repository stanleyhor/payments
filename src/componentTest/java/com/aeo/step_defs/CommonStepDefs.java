package com.aeo.step_defs;

import static com.aeo.constants.ScenarioVariables.ORDER_ID;
import static com.aeo.salad.stepDefinitions.shoppingcart.BaseApiAdditionalSteps.EMPTY_CARTID;

import com.aeo.salad.helpers.data.cart.BagView;

import io.cucumber.java.en.Given;
import io.restassured.response.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonStepDefs extends StepDefsConfigs {

    @Given("user received their orderId")
    public void givenUserReceivedTheirOrderId() {
        givenUserReceivedTheirOrderIdAndSavedInTheVariable(ORDER_ID);
    }

    @Given("user received their orderId and saved it in a context variable named {string}")
    public void givenUserReceivedTheirOrderIdAndSavedInTheVariable(String variableName) {
        String site = apiSteps.getSiteFromContext();
        Response response = viewCartSteps.responseFromViewCartWithCartIdAndSiteSuccessfullySaved(EMPTY_CARTID, site);
        log.info("Response: {}", response.asPrettyString());
        BagView bagView = response.as(BagView.class);
        String cartId = bagView.getData()
                .getId();
        scenarioContext.setVar(variableName, cartId);
    }

}
