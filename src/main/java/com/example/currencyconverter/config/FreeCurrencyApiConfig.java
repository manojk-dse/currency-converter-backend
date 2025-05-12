package com.example.currencyconverter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "freecurrencyapi")
public class FreeCurrencyApiConfig {
    private String apiKey;
    private String baseUrl;
}
