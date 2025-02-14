package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.ClientException;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.service.BTClientService;
import com.ecomm.payments.util.BTPaypalMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BTClientServiceImpl implements BTClientService {

    private final BTPaypalMapper btPaypalMapper;

    private final HttpGraphQlClient httpGraphQlclient;

    @CircuitBreaker(name = "bTAuthCallService",
            fallbackMethod = "fallbackMakeAuthCall")
    public ClientGraphQlResponse makeAuthCall(AuthorizationRequest authRequest, String siteId) {

        log.debug("BTClientService.makeAuthCall() :: orderNumber: {}", authRequest.getOrderNumber());

        String docName = "authorizepayment";
        Map<String, Object> authRequestMap = btPaypalMapper.prepareAuthRequest(authRequest, siteId);

        ClientGraphQlResponse graphQlResponse = callBTGraphQlClient(docName, authRequestMap);

        log.info("BTClientService.makeAuthCall() :: orderNumber: {}, ClientGraphQlResponse: {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                graphQlResponse);

        return graphQlResponse;

    }

    protected ClientGraphQlResponse fallbackMakeAuthCall(AuthorizationRequest authRequest, String siteId, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: BT auth call with orderId {} :: {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "BTClientService.fallbackMakeAuthCall()"));
        }

        log.error("BTClientService POST Fallback method triggered for auth call with orderId {} siteId {} and exception {} ",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), siteId, exception);

        ClientErrorResponse response = ClientErrorResponse.defaultErrorResponse;
        throw new ClientException(response);
    }

    @CircuitBreaker(name = "bTPaypalAuthCallService",
            fallbackMethod = "fallbackMakePayPalAuthCall")
    public ClientGraphQlResponse makePayPalAuthCall(AuthorizationRequest authRequest, String siteId) {

        log.debug("BTClientService.makePayPalAuthCall() :: orderNumber: {}", authRequest.getOrderNumber());

        String docName = "authorizepaypalpayment";
        Map<String, Object> authRequestMap = btPaypalMapper.prepareAuthRequest(authRequest, siteId);

        ClientGraphQlResponse graphQlResponse = callBTGraphQlClient(docName, authRequestMap);

        log.info("BTClientService.makePayPalAuthCall() :: orderNumber: {}, ClientGraphQlResponse: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), graphQlResponse);

        return graphQlResponse;

    }

    protected ClientGraphQlResponse fallbackMakePayPalAuthCall(AuthorizationRequest authRequest, String siteId, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: BT PayPal Auth call with orderId {} :: {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "BTClientService.fallbackMakePayPalAuthCall()"));
        }

        log.error("BTClientService POST Fallback method triggered for PayPal Auth call with orderId {} siteId {} and exception {} ",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), siteId, exception);

        ClientErrorResponse response = ClientErrorResponse.defaultErrorResponse;
        throw new ClientException(response);
    }

    @CircuitBreaker(name = "bTCreateClientTokenCallService",
            fallbackMethod = "fallbackMakeCreateClientTokenCall")
    public ClientGraphQlResponse makeCreateClientTokenCall(String siteId) {

        log.debug("BTClientService.makeCreateClientTokenCall() :: siteId: {}", siteId);

        String docName = "createclienttoken";
        Map<String, Object> clientTokenMap = btPaypalMapper.prepareCreateClientTokenRequest(siteId);

        ClientGraphQlResponse graphQlResponse = callBTGraphQlClient(docName, clientTokenMap);

        log.info("BTClientService.makeCreateClientTokenCall() :: ClientGraphQlResponse: {}", graphQlResponse);

        return graphQlResponse;

    }

    public ClientGraphQlResponse fallbackMakeCreateClientTokenCall(String siteId, Exception exception) {

        if (exception instanceof CallNotPermittedException) {
            log.warn("circuit-breaker-open :: BT PayPal Client Token call with siteId {} :: {}", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                    v(PaymentsConstants.LOG_KEY_METHOD_NAME, "BTClientService.fallbackMakeCreatePalClientTokenCall()"));
        }

        log.error("BTClientService POST Fallback method triggered for PayPal Client Token call with siteId {} and exception {} ",
                v(PaymentsConstants.LOG_KEY_SITE_ID, siteId), exception);

        ClientErrorResponse response = ClientErrorResponse.defaultErrorResponse;
        throw new ClientException(response);
    }

    private ClientGraphQlResponse callBTGraphQlClient(String docName, Map<String, Object> requestMap) {

        Mono<ClientGraphQlResponse> clientGraphQlResponse = httpGraphQlclient.documentName(docName)
                .variables(requestMap)
                .execute();

        return clientGraphQlResponse.block();
    }

}
