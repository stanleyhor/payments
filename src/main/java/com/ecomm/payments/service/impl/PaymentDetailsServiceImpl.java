package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.config.PaymentsConfig;
import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.service.PaymentDetailsService;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.CommonUtils;
import com.ecomm.payments.util.PaymentDetailsRequestResponseMapper;
import com.google.gson.Gson;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentDetailsServiceImpl implements PaymentDetailsService {

    private final PaymentDetailsRequestResponseMapper converter;
    private final PaymentsConfig paymentsConfig;
    private final RestTemplate restTemplate;
    private final AuthorizationUtils authorizationUtils;

    @Override
    @CircuitBreaker(name = "authorizationDetailEndPoint",
            fallbackMethod = "fallbackGetPaymentDetails")
    public PaymentDetailsResponse retrieveDetailsResponse(PaymentDetailsRequest detailsRequest) {

        log.debug("PaymentDetailsServiceImpl.retrieveDetailsResponse() :: orderNumber: {} and PaymentDetailsRequest: {} ",
                v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), new Gson().toJson(detailsRequest));

        AdyenDetailsRequest adyenDetailsRequest = converter.convertDetailsRequest(detailsRequest);

        log.info("PaymentDetailsServiceImpl.retrieveDetailsResponse() :: orderNumber: {} and AdyenDetailsRequest: {} ",
                v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), new Gson().toJson(adyenDetailsRequest, AdyenDetailsRequest.class));

        AdyenDetailsResponse adyenDetailsResponse = callService(adyenDetailsRequest).getBody();

        log.info("PaymentDetailsServiceImpl.retrieveDetailsResponse() :: orderNumber: {} and AdyenDetailsResponse: {} ",
                v(CommonKeys.ORDER_NUMBER.key(), CommonUtils.getOrderNumber(adyenDetailsResponse)), new Gson().toJson(adyenDetailsResponse),
                v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, CommonUtils.getPaymentHeaderId(adyenDetailsResponse)),
                v(PaymentsConstants.LOG_KEY_RESULT_CODE, CommonUtils.getDetailsResultCode(adyenDetailsResponse)),
                v(PaymentsConstants.LOG_KEY_FRAUD_RESULT_TYPE, CommonUtils.getDetailsFraudResultType(adyenDetailsResponse)));

        return authorizationUtils.processAndStoreAdyenAuthDetailsResponse(detailsRequest, adyenDetailsResponse);
    }

    public ResponseEntity<AdyenDetailsResponse> callService(AdyenDetailsRequest request) {
        ResponseEntity<AdyenDetailsResponse> response = null;
        try {
            response = restTemplate.exchange(paymentsConfig.getPaymentsDetailsEndPoint(), HttpMethod.POST, new HttpEntity<>(request),
                    AdyenDetailsResponse.class);
        } catch (RestClientResponseException e) {
            log.error("Exception in AdyenDetail CallService: {} ", e.getResponseBodyAsString());
            ClientErrorResponse clientErrorResponse = new Gson().fromJson(e.getResponseBodyAsString(), ClientErrorResponse.class);
            throw new ClientException(clientErrorResponse);
        }
        return response;
    }

    public PaymentDetailsResponse fallbackGetPaymentDetails(PaymentDetailsRequest detailsRequest, CallNotPermittedException ex) {
        log.warn("circuit-breaker-open :: fallback method triggered for {}",
                v(PaymentsConstants.LOG_KEY_METHOD_NAME, "PaymentDetailsServiceImpl.retrieveDetailsResponse()"));
        log.error("PaymentDetailsServiceImpl POST Fallback method triggered for request with paymentData: {} and exception: {}",
                detailsRequest.getPaymentData(), ex);
        ClientErrorResponse response = ClientErrorResponse.defaultErrorResponse;
        throw new ClientException(response);
    }

}
