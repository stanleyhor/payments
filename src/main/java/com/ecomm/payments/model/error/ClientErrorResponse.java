package com.ecomm.payments.model.error;

import com.ecomm.payments.constants.PaymentsConstants;
import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ClientErrorResponse {

    private int status;
    private String message;
    private String errorCode;
    private String errorKey;

    public static final ClientErrorResponse defaultErrorResponse = new ClientErrorResponse();
    static {
        defaultErrorResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        defaultErrorResponse.setErrorCode(PaymentsConstants.ERROR_CODE_503);
        defaultErrorResponse.setErrorKey(PaymentsConstants.ERROR_KEY);
        defaultErrorResponse.setMessage(PaymentsConstants.ERROR_MESSAGE);
    }

}
