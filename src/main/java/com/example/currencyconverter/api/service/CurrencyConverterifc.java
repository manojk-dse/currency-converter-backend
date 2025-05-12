package com.example.currencyconverter.api.service;

import com.example.currencyconverter.api.result.CurrencyConversionRequest;
import com.example.currencyconverter.api.result.CurrencyConversionResult;

public interface CurrencyConverterifc {
    CurrencyConversionResult convertCurrencyToAll(CurrencyConversionRequest request);
    CurrencyConversionResult convertCurrency(CurrencyConversionRequest request);
}
