package com.example.currencyconverter.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.currencyconverter.data.model.CurrencyExchangeRate;
import com.example.currencyconverter.data.model.RateStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyExchangeRateRepository extends JpaRepository<CurrencyExchangeRate, Long> {

    @Query("SELECT c FROM CurrencyExchangeRate c " +
           "WHERE c.baseCurrency = :baseCurrency " +
           "AND c.targetCurrency = :targetCurrency " +
           "AND c.rateTimestamp = (SELECT MAX(c2.rateTimestamp) " +
           "                       FROM CurrencyExchangeRate c2 " +
           "                       WHERE c2.baseCurrency = :baseCurrency " +
           "                       AND c2.targetCurrency = :targetCurrency)")
    Optional<CurrencyExchangeRate> findLatestExchangeRate(@Param("baseCurrency") String baseCurrency, 
                                                         @Param("targetCurrency") String targetCurrency);

    @Modifying
    @Transactional
    @Query("UPDATE CurrencyExchangeRate r SET r.status = :status WHERE r.baseCurrency = :baseCurrency AND r.status = 'LATEST'")
    void updateStatusForBaseCurrency(@Param("baseCurrency") String baseCurrency, @Param("status") RateStatus status);
    
    /**
     * Find all currency exchange rates with the given status.
     * @param status the status to filter by (e.g., LATEST)
     * @return list of currency exchange rates
     */
    List<CurrencyExchangeRate> findAllByStatus(RateStatus status);
    
    /**
     * Check if any rates for the given base currency have been updated within the last 24 hours
     * @param baseCurrency the base currency to check
     * @param timestamp the timestamp to compare against
     * @return true if rates exist and are fresh, false otherwise
     */
    @Query("SELECT COUNT(c) > 0 FROM CurrencyExchangeRate c " +
           "WHERE c.baseCurrency = :baseCurrency " +
           "AND c.status = 'LATEST' " +
           "AND c.rateTimestamp >= :timestamp")
    boolean hasRecentRates(@Param("baseCurrency") String baseCurrency, 
                          @Param("timestamp") LocalDateTime timestamp);
}
