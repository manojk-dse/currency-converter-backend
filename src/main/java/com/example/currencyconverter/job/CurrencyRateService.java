package com.example.currencyconverter.job;

/**
 * Service responsible for managing currency exchange rates.
 * This service handles:
 * - Fetching current exchange rates from external API
 * - Storing rates in the database
 * - Tracking job execution history
 * - Managing rate update schedules
 *
 * The service ensures that:
 * 1. Exchange rates are regularly updated
 * 2. API rate limits are respected
 * 3. Job execution is properly tracked
 * 4. Data consistency is maintained through transactions
 */

import com.example.currencyconverter.config.FreeCurrencyApiConfig;
import com.example.currencyconverter.data.dto.CurrencyApiResponse;
import com.example.currencyconverter.data.model.Currency;
import com.example.currencyconverter.data.model.CurrencyExchangeRate;
import com.example.currencyconverter.data.model.JobExecutionHistory;
import com.example.currencyconverter.data.model.JobStatus;
import com.example.currencyconverter.data.model.RateStatus;
import com.example.currencyconverter.data.repository.CurrencyExchangeRateRepository;
import com.example.currencyconverter.data.repository.CurrencyRepository;
import com.example.currencyconverter.data.repository.JobExecutionHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRateService {

    private final FreeCurrencyApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final JobExecutionHistoryRepository jobHistoryRepository;
    private final CurrencyExchangeRateRepository rateRepository;
    private final CurrencyRepository currencyRepository;

    /**
     * Check if we need to refresh rates from the API
     * @return true if any currency needs a refresh, false if all are up to date
     */
    public boolean needsRateRefresh() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        
        // Get all currency codes from the database
        List<String> currencyCodes = currencyRepository.findAll().stream()
                .map(Currency::getCode)
                .toList();

        log.debug("Checking rate refresh status for {} currencies", currencyCodes.size());

        // Check each currency - if any one needs refresh, return true
        for (String baseCurrency : currencyCodes) {
            if (!rateRepository.hasRecentRates(baseCurrency, twentyFourHoursAgo)) {
                log.info("Currency {} needs rate refresh, last update before {}", 
                    baseCurrency, twentyFourHoursAgo);
                return true;
            }
            log.debug("Currency {} rates are up to date", baseCurrency);
        }
        
        log.info("All {} currency rates are up to date", currencyCodes.size());
        return false;
    }

    @Transactional
    public void fetchAndStoreCurrencyRates() {
        long startTime = System.currentTimeMillis();
        MDC.put("jobName", "CURRENCY_RATE_FETCH");
        MDC.put("jobId", String.valueOf(System.nanoTime()));
        log.info("Starting currency rates fetch job");

        // Get all currency codes from the database
        List<String> currencyCodes = currencyRepository.findAll().stream()
                .map(Currency::getCode)
                .toList();
        log.debug("Found {} currencies in database", currencyCodes.size());

        // Skip if rates are fresh
        if (!needsRateRefresh()) {
            log.info("Skipping API call as rates are fresh");
            MDC.clear();
            return;
        }

        log.info("Starting currency rates fetch job for {} currencies", currencyCodes.size());
        JobExecutionHistory jobHistory = new JobExecutionHistory();
        jobHistory.setJobName("CURRENCY_RATE_FETCH");
        jobHistory.setStartTime(LocalDateTime.now());
        jobHistory.setStatus(JobStatus.STARTED);
        jobHistoryRepository.save(jobHistory);
        MDC.put("jobHistoryId", String.valueOf(jobHistory.getId()));

        List<CurrencyExchangeRate> allRates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int totalProcessed = 0;
        int skippedCurrencies = 0;

        try {
            String currencies = String.join(",", currencyCodes);
            
            for (String baseCurrency : currencyCodes) {
                // Skip currencies that have fresh rates
                if (rateRepository.hasRecentRates(baseCurrency, LocalDateTime.now().minusHours(24))) {
                    log.debug("Skipping currency {} as rates are fresh (last update within 24h)", baseCurrency);
                    skippedCurrencies++;
                    continue;
                }
                MDC.put("currentCurrency", baseCurrency);
                MDC.put("processedCount", String.valueOf(totalProcessed));

                log.info("Fetching rates for base currency: {} (attempt {})", baseCurrency, currencyCodes.indexOf(baseCurrency) + 1);
                
                String url = UriComponentsBuilder.fromHttpUrl(apiConfig.getBaseUrl() + "/latest")
                        .queryParam("apikey", apiConfig.getApiKey())
                        .queryParam("base_currency", baseCurrency)
                        .queryParam("currencies", currencies)
                        .toUriString();

                ResponseEntity<CurrencyApiResponse> response = restTemplate.getForEntity(url, CurrencyApiResponse.class);
                CurrencyApiResponse apiResponse = response.getBody();

                if (apiResponse != null && apiResponse.getRates() != null) {
                    // First, mark existing rates as OLD for this base currency
                    rateRepository.updateStatusForBaseCurrency(baseCurrency, RateStatus.OLD);
                    
                    for (Map.Entry<String, Double> entry : apiResponse.getRates().entrySet()) {
                        // Skip if base and target are the same
                        if (!baseCurrency.equals(entry.getKey())) {
                            CurrencyExchangeRate rate = new CurrencyExchangeRate();
                            rate.setBaseCurrency(baseCurrency);
                            rate.setTargetCurrency(entry.getKey());
                            rate.setExchangeRate(BigDecimal.valueOf(entry.getValue()));
                            rate.setRateTimestamp(now);
                            rate.setJobExecution(jobHistory);
                            rate.setStatus(RateStatus.LATEST);
                            allRates.add(rate);
                            totalProcessed++;
                        }
                    }
                    log.debug("Fetched {} rates for base currency {}", apiResponse.getRates().size(), baseCurrency);
                }
                
                // Add a small delay to avoid hitting API rate limits
                Thread.sleep(100);
            }

            if (!allRates.isEmpty()) {
                log.debug("Saving {} exchange rates to database", allRates.size());
                rateRepository.saveAll(allRates);
            } else {
                log.warn("No new rates to save to database");
            }
            
            jobHistory.setStatus(JobStatus.COMPLETED);
            jobHistory.setRecordsProcessed(totalProcessed);
            jobHistory.setEndTime(LocalDateTime.now());
            
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("jobDurationMs", String.valueOf(duration));
            MDC.put("ratesProcessed", String.valueOf(totalProcessed));
            MDC.put("skippedCurrencies", String.valueOf(skippedCurrencies));
            
            log.info("Currency rates fetch job completed in {}ms. Processed {} rates, skipped {} currencies", 
                    duration, totalProcessed, skippedCurrencies);

        } catch (Exception e) {
            log.error("Failed to fetch currency rates: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            MDC.put("errorType", e.getClass().getSimpleName());
            MDC.put("errorMessage", e.getMessage());
            MDC.put("errorStackTrace", e.getStackTrace()[0].toString());
            jobHistory.setStatus(JobStatus.FAILED);
            jobHistory.setEndTime(LocalDateTime.now());
            jobHistory.setErrorMessage(e.getMessage());
        } finally {
            jobHistory.setEndTime(LocalDateTime.now());
            jobHistoryRepository.save(jobHistory);
            MDC.clear();
        }
    }
}
