package com.example.currencyconverter.api.result;

public class CurrencyConversionRequest {
    private String fromCurrency;
    private Float amount;
    private String toCurrency;  // Optional

    // Default constructor
    public CurrencyConversionRequest() {
    }

    // Constructor with required fields
    public CurrencyConversionRequest(String fromCurrency, Float amount) {
        this.fromCurrency = fromCurrency;
        this.amount = amount;
    }

    // Constructor with all fields
    public CurrencyConversionRequest(String fromCurrency, Float amount, String toCurrency) {
        this.fromCurrency = fromCurrency;
        this.amount = amount;
        this.toCurrency = toCurrency;
    }

    // Getters and Setters
    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }
}
