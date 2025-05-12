package com.example.currencyconverter.data.model;

/**
 * Entity representing a currency exchange rate at a specific point in time.
 * 
 * This entity stores:
 * - The source currency (fromCurrency)
 * - The target currency (toCurrency)
 * - The exchange rate value
 * - The timestamp when the rate was recorded
 * - The status of the rate (ACTIVE/INACTIVE)
 *
 * The table is optimized for:
 * - Quick lookups of latest rates for currency pairs
 * - Historical rate tracking
 * - Rate validity management
 *
 * @see Currency
 * @see RateStatus
 */

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "currency_exchange_rates", 
       indexes = {
           @Index(name = "idx_base_target_currency", columnList = "base_currency,target_currency"),
           @Index(name = "idx_rate_timestamp", columnList = "rate_timestamp"),
           @Index(name = "idx_status_base_target", columnList = "status,base_currency,target_currency")
       })
public class CurrencyExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_execution_id", nullable = false)
    private JobExecutionHistory jobExecution;
    
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;
    
    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;
    
    @Column(name = "exchange_rate", nullable = false, precision = 20, scale = 6)
    private BigDecimal exchangeRate;
    
    @Column(name = "rate_timestamp", nullable = false)
    private LocalDateTime rateTimestamp;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private RateStatus status = RateStatus.LATEST;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
