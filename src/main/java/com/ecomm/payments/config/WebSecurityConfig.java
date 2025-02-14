package com.ecomm.payments.config;

import com.ecomm.payments.util.AuthorizationKeyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private DefaultAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired(required = false)
    private BasicAuthRequestFilter basicAuthRequestFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        if (basicAuthRequestFilter != null) {

            httpSecurity.csrf(csrf -> csrf.disable())
                    .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(basicAuthRequestFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
                    .authorizeHttpRequests(auth -> auth.requestMatchers("/payments/v1/auth")
                            .hasAuthority(AuthorizationKeyType.INTERNAL_APP.name())
                            .requestMatchers("/", "/authenticate", "/actuator/**", "/v3/api-docs/", "/v3/api-docs/**", "/configuration/ui",
                                    "/swagger-resources/**", "/configuration/security", "/swagger-ui/", "/swagger-ui/**", "/webjars/**", "/payments/v1/details",
                                    "/pubsub/**", "/internal/payments/v1/authorization", "/payments/v1/auth/applePay", "/payments/v1/afterpay/token",
                                    "/payments/v1/afterpay/auth", "/payments/v1/paypal/auth", "/payments/v1/paypal/authorize", "/payments/v1/paypal/token",
                                    "/payments/v1/auth/paypal", "payments/v2/auth", "/payments/v2/auth/applePay", "/payments/v2/auth/paypal",
                                    "/payments/v2/details", "/**")
                            .permitAll());

        } else {

            httpSecurity.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/", "/swagger-ui/", "/swagger-ui/**", "/v3/api-docs/", "/v3/api-docs/**", "/**", "/payments/v1/details",
                                    "/pubsub/**", "/internal/payments/v1/authorization", "/payments/v1/auth/applePay", "/payments/v1/afterpay/token",
                                    "/payments/v1/afterpay/auth", "/payments/v1/paypal/auth", "/payments/v1/paypal/authorize", "/payments/v1/paypal/token",
                                    "/payments/v1/auth/paypal", "payments/v2/auth", "/payments/v2/auth/applePay", "/payments/v2/auth/paypal",
                                    "/payments/v2/details", "/**")
                            .permitAll());

        }
        return httpSecurity.build();
    }

}
