package com.example.currencyconverter.api.result;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import com.example.currencyconverter.data.model.Currency;

import lombok.Data;

@Data
public class CurrencyConversionResult {
    private CurrencyValue fromCurrency;
    private Float amount;
    private Map<String, CurrencyValue> currencyValues;

    // Default constructor
    public CurrencyConversionResult() {
        this.currencyValues = new HashMap<>();
    }

    // Constructor with all fields
    public CurrencyConversionResult(Currency fromCurrency, Float amount, LocalDateTime timestamp) {
        this.fromCurrency = new CurrencyValue(fromCurrency.getCode(), 
                                            fromCurrency.getName(), 
                                            fromCurrency.getDescription(), 
                                            amount,
                                            timestamp);
        this.amount = amount;
        this.currencyValues = new HashMap<>();
    }

    // Utility method to add a single currency conversion
    public void addCurrencyValue(Currency currency, Float value, LocalDateTime timestamp) {
        this.currencyValues.put(currency.getCode(), 
            new CurrencyValue(currency.getCode(), 
                            currency.getName(), 
                            currency.getDescription(), 
                            value,
                            timestamp));
    }
}
