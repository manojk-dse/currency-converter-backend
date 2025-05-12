package com.example.currencyconverter.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.currencyconverter.api.result.CurrencyConversionRequest;
import com.example.currencyconverter.api.result.CurrencyConversionResult;
import com.example.currencyconverter.data.CurrencyConversionMap;
import com.example.currencyconverter.data.model.Currency;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CurrencyConverterSvc implements CurrencyConverterifc {
    
    private final CurrencyConversionMap conversionMap;
    
    @Autowired
    public CurrencyConverterSvc(CurrencyConversionMap conversionMap) {
        this.conversionMap = conversionMap;
    }
    
    @Override
    public CurrencyConversionResult convertCurrency(CurrencyConversionRequest request) {
        log.debug("Processing currency conversion request from {} to {}", request.getFromCurrency(), request.getToCurrency());
        if (request == null || request.getAmount() == null || 
            request.getFromCurrency() == null || request.getToCurrency() == null) {
            return null;
        }
        
        String fromCurrencyCode = request.getFromCurrency();
        String toCurrencyCode = request.getToCurrency();
        Float amount = request.getAmount();
        
        Currency fromCurrency = conversionMap.getCurrency(fromCurrencyCode);
        Currency toCurrency = conversionMap.getCurrency(toCurrencyCode);
        
        if (fromCurrency == null || toCurrency == null) {
            return null;
        }
        
        LocalDateTime timestamp = conversionMap.getRateTimestamp(fromCurrencyCode, toCurrencyCode);
        CurrencyConversionResult result = new CurrencyConversionResult(fromCurrency, amount, timestamp);
        BigDecimal rate = conversionMap.getRate(fromCurrencyCode, toCurrencyCode);
        
        if (rate != null) {
            LocalDateTime targetTimestamp = conversionMap.getRateTimestamp(fromCurrencyCode, toCurrencyCode);
            result.addCurrencyValue(toCurrency, rate.floatValue() * amount, targetTimestamp);
        }
        
        return result;
    }
    
    @Override
    public CurrencyConversionResult convertCurrencyToAll(CurrencyConversionRequest request) {
        log.debug("Starting currency conversion request processing");
        
        if (request == null) {
            log.error("Conversion request is null");
            return null;
        }
        
        if (request.getAmount() == null || request.getFromCurrency() == null) {
            log.error("Invalid request - amount: {}, fromCurrency: {}", request.getAmount(), request.getFromCurrency());
            return null;
        }
        
        String fromCurrencyCode = request.getFromCurrency();
        Float amount = request.getAmount();
        log.debug("Converting {} {}", amount, fromCurrencyCode);
        
        Currency fromCurrency = conversionMap.getCurrency(fromCurrencyCode);
        if (fromCurrency == null) {
            log.error("Source currency not found: {}", fromCurrencyCode);
            return null;
        }
        
        LocalDateTime timestamp = conversionMap.getRateTimestamp(fromCurrencyCode, fromCurrencyCode);
        CurrencyConversionResult result = new CurrencyConversionResult(fromCurrency, amount, timestamp);
        Map<String, BigDecimal> rates = conversionMap.getRatesForCurrency(fromCurrencyCode);
        log.debug("Found {} conversion rates for {}", rates.size(), fromCurrencyCode);
        
        // Calculate all conversion values
        int successfulConversions = 0;
        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            Currency targetCurrency = conversionMap.getCurrency(entry.getKey());
            if (targetCurrency != null) {
                float convertedAmount = entry.getValue().floatValue() * amount;
                LocalDateTime targetTimestamp = conversionMap.getRateTimestamp(fromCurrencyCode, targetCurrency.getCode());
                result.addCurrencyValue(targetCurrency, convertedAmount, targetTimestamp);
                log.trace("Converted {} {} to {} {}", amount, fromCurrencyCode, convertedAmount, targetCurrency.getCode());
                successfulConversions++;
            } else {
                log.warn("Target currency not found: {}", entry.getKey());
            }
        }
        log.info("Successfully converted {} {} to {} currencies", amount, fromCurrencyCode, successfulConversions);
        
        return result;
    }
}
