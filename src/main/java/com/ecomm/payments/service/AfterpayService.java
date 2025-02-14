package com.ecomm.payments.service;

import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.afterpay.AfterpayAuthResponse;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;

public interface AfterpayService {

    AfterpayCheckoutResponse fetchToken(ATGAuthRequest authRequest);

    AfterpayAuthResponse authorizePayment(PaymentDetailsRequest detailsRequest, String siteId);

}
