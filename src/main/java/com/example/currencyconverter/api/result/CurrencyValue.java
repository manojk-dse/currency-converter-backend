package com.example.currencyconverter.api.result;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CurrencyValue {
    private String code;
    private String name;
    private String description;
    private Float value;
    private LocalDateTime lastUpdated;
}
