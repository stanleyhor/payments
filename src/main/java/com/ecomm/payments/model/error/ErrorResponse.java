package com.ecomm.payments.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private List<Error> errors;

}
