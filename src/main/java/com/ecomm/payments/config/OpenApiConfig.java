package com.ecomm.payments.config;

import com.ecomm.payments.constants.OpenApiConstants;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI springShopOpenAPI() {
        return new OpenAPI().info(new Info().title(OpenApiConstants.OPENAPICONSTANTS_PROJECT_TITLE)
                .version(OpenApiConstants.OPENAPICONSTANTS_PROJECT_API_VERSION)
                .license(new License().name(OpenApiConstants.OPENAPICONSTANTS_PROJECT_LICENSE)))
                .externalDocs(new ExternalDocumentation().description(OpenApiConstants.OPENAPICONSTANTS_PROJECT_DOCUMENTATION)
                        .url(OpenApiConstants.OPENAPICONSTANTS_PROJECT_URL));
    }

}