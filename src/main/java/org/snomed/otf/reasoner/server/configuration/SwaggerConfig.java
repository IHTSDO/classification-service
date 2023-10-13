package org.snomed.otf.reasoner.server.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;


@Configuration
public class SwaggerConfig {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Bean
    public GroupedOpenApi apiDocs() {
        return GroupedOpenApi.builder()
                .group("classification-service")
                .packagesToScan("org.snomed.otf.reasoner.server")
                // Don't show the error or root endpoints in Swagger
                .pathsToExclude("/error", "/")
                .build();
    }

    @Bean
    public GroupedOpenApi springActuatorApi() {
        return GroupedOpenApi.builder()
                .group("actuator")
                .packagesToScan("org.springframework.boot.actuate")
                .pathsToMatch("/actuator/**")
                .build();
    }

    @Bean
    public OpenAPI apiInfo() {
        final String version = buildProperties != null ? buildProperties.getVersion() : "DEV";
        return new OpenAPI()
                .info(new Info()
                        .title("Classification Service")
                        .description("An open source standalone REST API for the classification of SNOMED CT ontologies")
                        .version(version)
                        .contact(new Contact().name("SNOMED International").url("https://www.snomed.org"))
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("See more about Classification Service in GitHub")
                        .url("https://github.com/IHTSDO/classification-service"));
    }
}
