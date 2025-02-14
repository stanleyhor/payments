package com.aeo.client;

import com.aeo.salad.cucumber.api.CucumberScenario;
import com.aeo.salad.helpers.rest.RequestParam;
import com.aeo.salad.helpers.rest.RequestParamType;
import com.aeo.salad.stepDefinitions.ApiSteps;
import com.aeo.utils.Configuration;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;

public final class TestClient {

    private static final Configuration configuration = new Configuration();
    private static TestClient instance;
    private final ApiSteps apiSteps = new ApiSteps();
    private final CucumberScenario context = CucumberScenario.getInstance();

    private TestClient() {
    }

    public static TestClient getInstance() {
        return instance == null ? new TestClient() : instance;
    }

    public Response sendHTTPRequest(RequestMethod method, String address, String body) {
        return sendRequest(method, address, new RequestParam(RequestParamType.HEADER, "Authorization", configuration.getToken()),
                new RequestParam(RequestParamType.HEADER, "aeSite", (String) context.getVar("site")),
                new RequestParam(RequestParamType.HEADER, "Content-Type", ContentType.JSON.getContentTypeStrings()[0]),
                new RequestParam(RequestParamType.BODY, "body", body));
    }

    public Response sendHTTPRequest(RequestMethod method, String address) {
        return sendRequest(method, address, new RequestParam(RequestParamType.HEADER, "Authorization", configuration.getToken()),
                new RequestParam(RequestParamType.HEADER, "aeSite", (String) context.getVar("site")));
    }

    private Response sendRequest(RequestMethod method, String address, RequestParam... params) {
        String responseVarName = "response";
        return apiSteps.sendHttpRequestSaveResponse(method.name(), address, responseVarName, Arrays.asList(params));

    }

}
