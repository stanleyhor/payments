package com.ecomm.payments.service;

import com.ecomm.payments.model.AdyenAuthRequest;
import com.ecomm.payments.model.AdyenAuthResponse;

public interface AdyenCallService {

    AdyenAuthResponse callService(AdyenAuthRequest convertedRequest, String idempotencyKey);

}
