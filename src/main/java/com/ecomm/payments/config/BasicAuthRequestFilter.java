package com.ecomm.payments.config;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.util.AuthorizationKeyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Profile("!chaos-monkey")
@Component
@ConfigurationProperties
public class BasicAuthRequestFilter extends OncePerRequestFilter {

    @Value("${basicAuthKeys}")
    private List<String> basicAuthKeys;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authKey = request.getHeader(PaymentsConstants.AUTHORIZATION);

        if (authKey != null
                && authKey.startsWith("Basic ")) {
            String key = authKey.substring(6);
            if (basicAuthKeys.contains(key)) {
                AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken(key, new AnonymousUser(key),
                        Arrays.asList(new SimpleGrantedAuthority(AuthorizationKeyType.INTERNAL_APP.name())));
                anonymousAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(anonymousAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    public List<String> getBasicAuthKeys() {
        return basicAuthKeys;
    }

    public void setBasicAuthKeys(List<String> basicAuthKeys) {
        this.basicAuthKeys = basicAuthKeys;
    }

    @Getter
    static class AnonymousUser {

        private final String key;

        public AnonymousUser(String key) {
            this.key = key;
        }

    }

}
