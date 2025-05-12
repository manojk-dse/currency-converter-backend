package com.example.currencyconverter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI documentation.
 * This sets up the Swagger UI and OpenAPI documentation for the API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI currencyConverterOpenAPI() {
        Server localServer = new Server()
            .url("http://localhost:8080")
            .description("Local Development Server");

        Contact contact = new Contact()
            .name("Currency Converter Team")
            .email("support@currencyconverter.com");

        License mitLicense = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
            .title("Currency Converter API")
            .version("1.0.0")
            .description("REST API for currency conversion and exchange rate management")
            .contact(contact)
            .license(mitLicense);

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer));
    }
}
