package com.example.currencyconverter.api.result;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CurrencyValue {
    private String code;
    private String name;
    private String description;
    private Float value;
}
