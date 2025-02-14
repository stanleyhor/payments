package com.ecomm.payments.service;

import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.ATGAuthResponse;

public interface PaymentAuthorizationService {

    ATGAuthResponse authorizePayment(ATGAuthRequest input);

    ATGAuthResponse authorizeAplazoPayment(ATGAuthRequest authRequest);

}
