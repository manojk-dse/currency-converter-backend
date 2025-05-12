package com.example.currencyconverter.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class CurrencyApiResponse {
    @JsonProperty("data")
    private Map<String, Double> rates;
}
