package com.ecomm.payments.service;

import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;

public interface PaymentDetailsService {

    PaymentDetailsResponse retrieveDetailsResponse(PaymentDetailsRequest input);

}
