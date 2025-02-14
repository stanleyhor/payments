package com.ecomm.payments.service.impl.v2;

import static net.logstash.logback.argument.StructuredArguments.v;

import com.aeo.logging.CommonKeys;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.service.PaymentDetailsService;
import com.ecomm.payments.util.AuthorizationUtils;
import com.ecomm.payments.util.CommonUtils;
import com.ecomm.payments.util.PaymentDetailsRequestResponseMapper;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentDetailsV2ServiceImpl implements PaymentDetailsService {

    private final PaymentDetailsRequestResponseMapper requestResponseMapper;

    private final AdyenV2CallServiceImpl adyenV2CallServiceImpl;

    private final AuthorizationUtils authorizationUtils;

    public PaymentDetailsResponse retrieveDetailsResponse(PaymentDetailsRequest detailsRequest) {

        log.debug("PaymentDetailsV2ServiceImpl.retrieveAuthorizationDetails() :: orderNumber: {} and PaymentDetailsRequest: {} ",
                v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), new Gson().toJson(detailsRequest));

        PaymentDetailsResponse detailsResponse = null;

        var adyenDetailsRequest = requestResponseMapper.convertDetailsRequest(detailsRequest);

        if (null != adyenDetailsRequest) {
            log.info("PaymentDetailsV2ServiceImpl.retrieveAuthorizationDetails() :: orderNumber: {} and AdyenDetailsRequest: {} ",
                    v(CommonKeys.ORDER_NUMBER.key(), detailsRequest.getOrderNumber()), new Gson().toJson(adyenDetailsRequest, AdyenDetailsRequest.class));

            var adyenDetailsResponseEntity = adyenV2CallServiceImpl.retrieveAuthDetails(adyenDetailsRequest);

            if (null != adyenDetailsResponseEntity
                    && null != adyenDetailsResponseEntity.getBody()) {
                var adyenDetailsResponse = adyenDetailsResponseEntity.getBody();
                log.info("PaymentDetailsV2ServiceImpl.retrieveAuthorizationDetails() :: orderNumber: {} and AdyenDetailsResponse: {} ",
                        v(CommonKeys.ORDER_NUMBER.key(), CommonUtils.getOrderNumber(adyenDetailsResponse)), new Gson().toJson(adyenDetailsResponse),
                        v(PaymentsConstants.LOG_KEY_PAYMENT_HEADER_ID, CommonUtils.getPaymentHeaderId(adyenDetailsResponse)),
                        v(PaymentsConstants.LOG_KEY_RESULT_CODE, CommonUtils.getDetailsResultCode(adyenDetailsResponse)),
                        v(PaymentsConstants.LOG_KEY_FRAUD_RESULT_TYPE, CommonUtils.getDetailsFraudResultType(adyenDetailsResponse)));

                detailsResponse = authorizationUtils.processAndStoreAdyenAuthDetailsResponse(detailsRequest, adyenDetailsResponse);
            }
        }
        return detailsResponse;
    }

}
