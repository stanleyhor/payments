package com.ecomm.payments.exception;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.CustomSourceErrorResponse;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.error.ClientErrorResponse;
import com.ecomm.payments.model.error.Error;
import com.ecomm.payments.model.error.ErrorResponse;
import com.ecomm.payments.util.ErrorType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

    public static final String ERROR_MESSAGE_RESPONSE_AND_EXCEPTION = "Error Message: {}, response: {} and exception: {}";

    @ExceptionHandler(value = AdyenClientException.class)
    @ResponseBody
    public ResponseEntity<Object> handleAdyenClientException(AdyenClientException e) {
        CustomSourceErrorResponse error = null;
        try {
            error = new Gson().fromJson(e.getMessage(), CustomSourceErrorResponse.class);
        } catch (JsonSyntaxException exp) {
            error = buildAuthErrorResponse(e);
        }
        error.setSource(PaymentsConstants.IDENTIFIER_ADYEN);
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, e.getMessage(), error, e);
        return new ResponseEntity<>(error, HttpStatus.valueOf(error.getStatus()));
    }

    @ExceptionHandler(value = AdyenClientFallbackException.class)
    @ResponseBody
    public ResponseEntity<Object> handleAdyenClientExceptionFallback(AdyenClientFallbackException e) {
        CustomSourceErrorResponse error = null;
        try {
            error = new Gson().fromJson(e.getMessage(), CustomSourceErrorResponse.class);
        } catch (JsonSyntaxException exp) {
            error = buildAuthErrorResponse(e);
        }
        error.setSource(PaymentsConstants.IDENTIFIER_PAYMENTS);
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, e.getMessage(), error, e);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = AuthorizationFailedException.class)
    @ResponseBody
    public ResponseEntity<PaymentDetailsResponse> handleAuthFailedException(AuthorizationFailedException e) {
        PaymentDetailsResponse response = new PaymentDetailsResponse();
        ErrorResponse errorResponse = new ErrorResponse();
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(ErrorType.AUTH_FAILED, new String[] { PaymentsConstants.PAYMENT_FIELD }));
        errorResponse.setErrors(errors);
        response.setError(errorResponse);
        response.setData(new ResponseData());
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, e.getMessage(), response, e);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = DuplicateAuthorizationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleDuplicateAuthorizationException(DuplicateAuthorizationException e) {
        CustomSourceErrorResponse error = new CustomSourceErrorResponse();
        error.setMessage(e.getMessage());
        error.setErrorCode(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()));
        error.setErrorType(HttpStatus.UNPROCESSABLE_ENTITY.name());
        error.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        error.setSource(PaymentsConstants.IDENTIFIER_PAYMENTS);
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, e.getMessage(), error, e);
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = ClientException.class)
    @ResponseBody
    public ResponseEntity<PaymentDetailsResponse> handleClientException(ClientException e) {
        List<Error> errors = new ArrayList<>();
        ClientErrorResponse clientError = e.getErrorResponse();
        if (clientError.getStatus() == 422) {
            errors.add(new Error(ErrorType.VALIDATION_FAILED, new String[] { PaymentsConstants.PAYMENT_FIELD }));
        } else {
            errors.add(new Error(ErrorType.INTERNAL_SERVER_ERROR, new String[] { PaymentsConstants.PAYMENT_FIELD }));
        }
        return buildDetailsErrorResponse(e, errors, HttpStatus.valueOf(clientError.getStatus()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value =
    { IllegalArgumentException.class, IllegalStateException.class, Exception.class })
    @ResponseBody
    public Object handlePaymentsAuthException(Exception e, WebRequest request) {
        // Below method added to cover 100% test cases
        return handleException(e, ((ServletWebRequest) request).getRequest()
                .getRequestURI());
    }

    public Object handleException(Exception e, String requestURI) {
        if (requestURI.contains(PaymentsConstants.DETAILS)) {
            List<Error> errors = new ArrayList<>();
            errors.add(new Error(ErrorType.INTERNAL_SERVER_ERROR, new String[] { PaymentsConstants.PAYMENT_FIELD }));
            return buildDetailsErrorResponse(e, errors, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return buildAuthErrorResponse(e);
        }
    }

    private CustomSourceErrorResponse buildAuthErrorResponse(Exception pException) {
        CustomSourceErrorResponse error = new CustomSourceErrorResponse();
        error.setMessage(String.valueOf(pException));
        error.setErrorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        error.setErrorType(HttpStatus.INTERNAL_SERVER_ERROR.name());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setSource(PaymentsConstants.IDENTIFIER_PAYMENTS);
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, pException.getMessage(), error, pException);
        return error;
    }

    private ResponseEntity<PaymentDetailsResponse> buildDetailsErrorResponse(Exception e, List<Error> pErrors, HttpStatus httpStatus) {
        PaymentDetailsResponse response = new PaymentDetailsResponse();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(pErrors);
        response.setError(errorResponse);
        response.setData(new ResponseData());
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, e.getMessage(), response, e);
        return new ResponseEntity<>(response, httpStatus);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public PaymentDetailsResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        PaymentDetailsResponse response = new PaymentDetailsResponse();
        ErrorResponse errorResponse = new ErrorResponse();
        List<Error> errors = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    fields.add(fieldName
                            + " "
                            + errorMessage);
                });
        errors.add(new Error("Request Validation", fields.toArray()));
        errorResponse.setErrors(errors);
        response.setError(errorResponse);
        response.setData(new ResponseData());
        log.error(ERROR_MESSAGE_RESPONSE_AND_EXCEPTION, ex.getMessage(), response, ex);
        return response;
    }

}
