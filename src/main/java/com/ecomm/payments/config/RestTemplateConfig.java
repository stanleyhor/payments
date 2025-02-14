package com.ecomm.payments.config;

import com.ecomm.payments.constants.PaymentsConstants;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final PaymentsConfig paymentsConfig;

    @Bean(name = "restTemplate")
    RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        RestTemplate restTemplate = templateBuilder().rootUri(paymentsConfig.getPaymentsURL())
                .defaultHeader(PaymentsConstants.API_KEY, paymentsConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        restTemplate.setRequestFactory(clientHttpRequestFactory(paymentsConfig.getReadTimeoutMillis(), paymentsConfig.getConnectTimeoutMillis()));

        return restTemplate;
    }

    @Bean(name = "afterpayServiceRestTemplate")
    RestTemplate getAfterpayServiceRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        RestTemplate restTemplate = templateBuilder().rootUri(paymentsConfig.getAfterpayServiceURL())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        restTemplate
                .setRequestFactory(clientHttpRequestFactory(paymentsConfig.getAfterpayReadTimeoutMillis(), paymentsConfig.getAfterpayConnectTimeoutMillis()));

        return restTemplate;
    }

    @Bean(name = "adyenV2AuthRestTemplate")
    RestTemplate getAdyenV2AuthTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        RestTemplate restTemplate = templateBuilder().rootUri(paymentsConfig.getPaymentsAuthV2URL())
                .defaultHeader(PaymentsConstants.API_KEY, paymentsConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        restTemplate.setRequestFactory(clientHttpRequestFactory(paymentsConfig.getReadTimeoutMillis(), paymentsConfig.getConnectTimeoutMillis()));

        return restTemplate;
    }

    private static RestTemplateBuilder templateBuilder() {
        return new RestTemplateBuilder().errorHandler(new RestTemplateErrorHandler());
    }

    private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(
            int readTimeoutMillis, int connectTimeoutMillis
    ) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS)
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .build();

        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMillis))
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionKeepAlive(TimeValue.ofMilliseconds(readTimeoutMillis))
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .disableAutomaticRetries()
                .disableCookieManagement()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setDefaultSocketConfig(socketConfig)
                        .setMaxConnTotal(100) // default is 20
                        .setDefaultConnectionConfig(connConfig)
                        .setMaxConnPerRoute(20) // default is 2 per host
                        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                        .build())
                .setConnectionManagerShared(true)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeoutMillis);
        clientHttpRequestFactory.setHttpClient(httpClient);

        return clientHttpRequestFactory;
    }

}
