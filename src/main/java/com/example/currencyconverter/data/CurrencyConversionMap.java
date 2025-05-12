package com.example.currencyconverter.data;

import com.example.currencyconverter.data.model.Currency;
import com.example.currencyconverter.data.model.CurrencyExchangeRate;
import com.example.currencyconverter.data.model.RateStatus;
import com.example.currencyconverter.data.repository.CurrencyExchangeRateRepository;
import com.example.currencyconverter.data.repository.CurrencyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyConversionMap {
    
    private final CurrencyExchangeRateRepository rateRepository;
    private final CurrencyRepository currencyRepository;
    private final Map<String, Map<String, BigDecimal>> conversionRates = new ConcurrentHashMap<>();
    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();
    
    /**
     * Reloads all currency conversion rates from the database.
     * This method is thread-safe and can be called by multiple threads.
     */
    public void reloadRates() {
        log.info("Reloading currencies and conversion rates into cache");
        
        // Load all currencies first
        List<Currency> allCurrencies = currencyRepository.findAll();
        Map<String, Currency> newCurrencies = new ConcurrentHashMap<>();
        allCurrencies.forEach(currency -> newCurrencies.put(currency.getCode(), currency));
        currencies.clear();
        currencies.putAll(newCurrencies);
        log.info("Loaded {} currencies into cache", currencies.size());
        
        // Load latest exchange rates
        List<CurrencyExchangeRate> latestRates = rateRepository.findAllByStatus(RateStatus.LATEST);
        
        // Create a new map to hold the updated rates
        Map<String, Map<String, BigDecimal>> newRates = new ConcurrentHashMap<>();
        
        // Process all rates
        for (CurrencyExchangeRate rate : latestRates) {
            newRates.computeIfAbsent(rate.getBaseCurrency(), k -> new ConcurrentHashMap<>())
                    .put(rate.getTargetCurrency(), rate.getExchangeRate());
        }
        
        // Add direct conversion rates (1.0) for same currency
        for (String currency : newRates.keySet()) {
            newRates.get(currency).put(currency, BigDecimal.ONE);
        }
        
        // Replace the old rates with new ones
        conversionRates.clear();
        conversionRates.putAll(newRates);
        
        log.info("Successfully reloaded {} base currencies with their conversion rates", newRates.size());
    }
    
    /**
     * Gets the conversion rate from one currency to another.
     * @param from source currency code
     * @param to target currency code
     * @return the conversion rate or null if not found
     */
    public BigDecimal getRate(String from, String to) {
        Map<String, BigDecimal> rates = conversionRates.get(from);
        return rates != null ? rates.get(to) : null;
    }
    
    /**
     * Gets all conversion rates for a given base currency.
     * @param baseCurrency the base currency code
     * @return map of target currencies to their rates, or empty map if base currency not found
     */
    public Map<String, BigDecimal> getRatesForCurrency(String baseCurrency) {
        return new HashMap<>(conversionRates.getOrDefault(baseCurrency, new HashMap<>()));
    }
    
    /**
     * Gets all available currencies.
     * @return map of currency codes to Currency objects
     */
    public Map<String, Currency> getAllCurrencies() {
        return new HashMap<>(currencies);
    }
    
    /**
     * Gets a specific currency by its code.
     * @param code the currency code
     * @return the Currency object or null if not found
     */
    public Currency getCurrency(String code) {
        return currencies.get(code);
    }
    
    /**
     * Gets the conversion rate from one currency to another.
     * This method is provided for backward compatibility.
     * @param fromCurrency source currency code
     * @param toCurrency target currency code
     * @return the conversion rate as a Float, or null if not found
     */
    public Float getConversionRate(String fromCurrency, String toCurrency) {
        BigDecimal rate = getRate(fromCurrency.toUpperCase(), toCurrency.toUpperCase());
        return rate != null ? rate.floatValue() : null;
    }
    
    /**
     * Gets all conversion rates for USD.
     * This method is provided for backward compatibility.
     */
    public Map<String, Float> getUsdConversionMap() {
        return convertToFloatMap(getRatesForCurrency("USD"));
    }
    
    /**
     * Gets all conversion rates for EUR.
     * This method is provided for backward compatibility.
     */
    public Map<String, Float> getEurConversionMap() {
        return convertToFloatMap(getRatesForCurrency("EUR"));
    }
    
    /**
     * Gets all conversion rates for JPY.
     * This method is provided for backward compatibility.
     */
    public Map<String, Float> getJpyConversionMap() {
        return convertToFloatMap(getRatesForCurrency("JPY"));
    }
    
    private Map<String, Float> convertToFloatMap(Map<String, BigDecimal> bigDecimalMap) {
        Map<String, Float> floatMap = new HashMap<>();
        bigDecimalMap.forEach((key, value) -> floatMap.put(key, value.floatValue()));
        return floatMap;
    }
}
