package com.ecomm.payments.service.impl;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.exception.AuthorizationFailedException;
import com.ecomm.payments.exception.DuplicateAuthorizationException;
import com.ecomm.payments.model.braintree.AuthorizationRequest;
import com.ecomm.payments.model.braintree.AuthorizeResponse;
import com.ecomm.payments.model.braintree.BTPaypalAuthResponse;
import com.ecomm.payments.model.braintree.ClientTokenResponse;
import com.ecomm.payments.model.braintree.PaypalAuthResponse;
import com.ecomm.payments.service.BTPaypalService;
import com.ecomm.payments.util.BTPaypalMapper;
import com.ecomm.payments.util.BTPaypalRepoWrapper;
import com.google.gson.Gson;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BTPaypalServiceImpl implements BTPaypalService {

    private final BTClientServiceImpl btClientService;

    private final BTPaypalMapper btPaypalMapper;

    private final BTPaypalRepoWrapper btPaypalRepoWrapper;

    private static final String NO_REPONSE = "No Response from service";

    public ResponseEntity<Object> makeAuthCall(AuthorizationRequest authRequest, String siteId) {

        log.info("BTPaypalService.makeAuthCall() :: orderNumber: {}, paymentMethod: {}, siteId: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)),
                v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        AuthorizeResponse authResponse = null;

        ClientGraphQlResponse graphQlResponse = btClientService.makeAuthCall(authRequest, siteId);

        if (!ObjectUtils.isEmpty(graphQlResponse)) {

            authResponse = graphQlResponse.toEntity(AuthorizeResponse.class);

            log.debug("BTPaypalService.makeAuthCall() :: orderNumber: {}, paymentMethod: {}, siteId: {}, authResponse: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)),
                    v(PaymentsConstants.LOG_KEY_SITE_ID, siteId), new Gson().toJson(authResponse));

            if (!ObjectUtils.isEmpty(graphQlResponse.getErrors())) {

                log.error("BTPaypalService.makeAuthCall() :: orderNumber: {}, paymentMethod: {}, siteId: {}, errorResponse: {}",
                        v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                        v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                        graphQlResponse.getErrors());

                throw new DuplicateAuthorizationException(graphQlResponse.getErrors()
                        .get(0)
                        .getMessage());

            }

        } else {

            log.error("BTPaypalService.makeAuthCall() :: orderNumber: {}, graphQlResponse is empty",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)),
                    v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

            throw new AuthorizationFailedException(NO_REPONSE);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(authResponse);
    }

    public ResponseEntity<Object> makePayPalAuthCall(AuthorizationRequest authRequest, String siteId) {

        log.info("BTPaypalService.makePayPalAuthCall() :: orderNumber: {}, paymentMethod: {}, siteId: {}",
                v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)),
                v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        ClientGraphQlResponse graphQlResponse = btClientService.makePayPalAuthCall(authRequest, siteId);

        PaypalAuthResponse authResponse;

        if (!ObjectUtils.isEmpty(graphQlResponse)) {

            BTPaypalAuthResponse btPaypalAuthResponse = graphQlResponse.toEntity(BTPaypalAuthResponse.class);

            log.debug("BTPaypalService.makePayPalAuthCall() :: orderNumber: {},  paymentMethod: {}, siteId: {}, authResponse: {}",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)),
                    v(PaymentsConstants.LOG_KEY_SITE_ID, siteId), new Gson().toJson(btPaypalAuthResponse));

            authResponse = btPaypalMapper.prepareAuthResponse(authRequest, btPaypalAuthResponse);

            if (!ObjectUtils.isEmpty(graphQlResponse.getErrors())) {

                log.error("BTPaypalService.makePayPalAuthCall() :: orderNumber: {}, paymentMethod: {}, siteId: {}, errorResponse: {}",
                        v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                        v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)), v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                        graphQlResponse.getErrors());

                authResponse = btPaypalMapper.prepareAuthErrorResponse(authRequest, graphQlResponse);
            }

            btPaypalRepoWrapper.storeTransactionalData(authRequest, authResponse, siteId);

        } else {

            log.error("BTPaypalService.makePayPalAuthCall() :: orderNumber: {}, graphQlResponse is empty",
                    v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()), v(PaymentsConstants.LOG_KEY_PAYMENT_METHOD, getPaymentMethod(authRequest)),
                    v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

            throw new AuthorizationFailedException(NO_REPONSE);
        }

        log.info("BTPaypalService.makePayPalAuthCall() :: orderNumber: {}, AuthResponse: {}", v(CommonKeys.ORDER_NUMBER.key(), authRequest.getOrderNumber()),
                new Gson().toJson(authResponse));

        return ResponseEntity.status(HttpStatus.OK)
                .body(authResponse);
    }

    @Override
    public ResponseEntity<Object> getPayPalClientToken(String siteId) {

        log.info("BTPaypalService.getPayPalClientToken() :: siteId: {}", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

        ClientGraphQlResponse graphQlResponse = btClientService.makeCreateClientTokenCall(siteId);

        ClientTokenResponse clientTokenResponse = null;

        if (!ObjectUtils.isEmpty(graphQlResponse)) {

            clientTokenResponse = graphQlResponse.toEntity(ClientTokenResponse.class);

            log.debug("BTPaypalService.getPayPalClientToken() :: siteId: {}, Client Token Response: {}", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                    new Gson().toJson(clientTokenResponse));

            if (!ObjectUtils.isEmpty(graphQlResponse.getErrors())) {

                log.error("BTPaypalService.makePayPalAuthCall() :: siteId: {}, errorResponse: {}", clientTokenResponse, graphQlResponse.getErrors());

                throw new DuplicateAuthorizationException(graphQlResponse.getErrors()
                        .get(0)
                        .getMessage());
            }

        } else {

            log.error("BTPaypalService.getPayPalClientToken() :: siteId: {}, graphQlResponse is empty", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId));

            throw new AuthorizationFailedException(NO_REPONSE);
        }

        log.debug("BTClientService.getPayPalClientToken() :: siteId: {}, ClientGraphQlResponse: {}", v(PaymentsConstants.LOG_KEY_SITE_ID, siteId),
                clientTokenResponse);

        return ResponseEntity.status(HttpStatus.OK)
                .body(clientTokenResponse);
    }

    private String getPaymentMethod(AuthorizationRequest authRequest) {
        return null != authRequest.getPaymentMethod() ? authRequest.getPaymentMethod()
                .getType() : "";
    }

}
