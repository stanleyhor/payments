package com.ecomm.payments.config;

import org.apache.catalina.Context;
import org.apache.catalina.webresources.ExtractingRoot;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {

        return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {

            @Override
            public void customize(TomcatServletWebServerFactory container) {
                container.addContextCustomizers(new TomcatContextCustomizer() {

                    @Override
                    public void customize(Context cntxt) {
                        cntxt.setReloadable(false);
                        cntxt.setResources(new ExtractingRoot());
                    }

                });
            }

        };
    }

}
