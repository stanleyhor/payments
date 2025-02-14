package com.ecomm.payments.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("customhealthcheck")
public class CustomHealthCheckController {

    @GetMapping
    public ResponseEntity<String> getCustomHealthCheck() {
        return new ResponseEntity<>("UP", HttpStatus.OK);
    }

}