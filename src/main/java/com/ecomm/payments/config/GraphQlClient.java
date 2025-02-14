package com.ecomm.payments.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class GraphQlClient {

    private final PaymentsConfig paymentsConfig;

    @Bean
    HttpGraphQlClient httpGraphQlclient() {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(paymentsConfig.getBraintreeReadTimeoutMillis()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, paymentsConfig.getBraintreeConnectTimeoutMillis());

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(paymentsConfig.getBraintreeServiceURL())
                .defaultHeader("Authorization", paymentsConfig.getBraintreeAuth())
                .defaultHeader("Braintree-Version", paymentsConfig.getBraintreeVersion())
                .build();

        return HttpGraphQlClient.create(webClient);

    }

}
