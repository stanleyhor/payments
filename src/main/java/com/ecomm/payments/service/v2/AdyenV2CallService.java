package com.ecomm.payments.service.v2;

import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;
import com.ecomm.payments.model.AdyenDetailsRequest;
import com.ecomm.payments.model.AdyenDetailsResponse;
import org.springframework.http.ResponseEntity;

public interface AdyenV2CallService {

    ResponseEntity<AdyenAuthResponse> authorize(AdyenAuthRequest convertedRequest, String idempotencyKey);

    ResponseEntity<AdyenDetailsResponse> retrieveAuthDetails(AdyenDetailsRequest adyenDetailsRequest);

}
