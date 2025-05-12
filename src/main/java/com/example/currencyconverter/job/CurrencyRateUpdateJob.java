package com.example.currencyconverter.job;

/**
 * Scheduled job for updating currency exchange rates.
 *
 * This job is responsible for:
 * 1. Periodic fetching of current exchange rates
 * 2. Updating the local database with new rates
 * 3. Refreshing the in-memory rate cache
 *
 * Job Schedule:
 * - Runs hourly (configurable via cron expression)
 * - Only fetches from API if rates are older than 24 hours
 * - Always refreshes the in-memory cache
 *
 * Implementation Details:
 * - Uses Spring's @Scheduled annotation for timing
 * - Delegates actual rate fetching to CurrencyRateService
 * - Updates CurrencyConversionMap for caching
 * - Logs job execution status and timing
 *
 * @see com.example.currencyconverter.service.CurrencyRateService
 * @see com.example.currencyconverter.data.CurrencyConversionMap
 */

import com.example.currencyconverter.data.CurrencyConversionMap;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyRateUpdateJob {
    private final CurrencyRateService currencyRateService;
    private final CurrencyConversionMap conversionMap;

    @PostConstruct
    public void init() {
        log.info("Initializing currency rates on startup");
        updateCurrencyRates();
    }

    /**
     * Run every hour but only fetch from API if rates are older than 24 hours.
     * This ensures we have fresh rates while respecting API limits.
     */
    @Scheduled(cron = "0 0 * * * *") // Run at the start of every hour
    public void updateCurrencyRates() {
        log.info("Starting currency rate update job");
        
        // This will only fetch from API if rates are older than 24 hours
        currencyRateService.fetchAndStoreCurrencyRates();
        
        // Always reload the cache to ensure we have the latest rates
        conversionMap.reloadRates();
        
        log.info("Completed currency rate update job");
    }
}
