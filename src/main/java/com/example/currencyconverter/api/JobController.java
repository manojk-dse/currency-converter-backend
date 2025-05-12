package com.example.currencyconverter.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.currencyconverter.job.CurrencyRateUpdateJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing background job operations.
 *
 * This controller provides endpoints to manually trigger and manage background jobs,
 * particularly focused on currency rate updates. It includes:
 * 
 * Features:
 * - Manual triggering of currency rate updates
 * - Job execution tracking with unique IDs
 * - Performance monitoring (execution duration)
 * - Comprehensive error handling and logging
 * 
 * Endpoints:
 * - POST /api/jobs/currency-rates/update: Manually trigger currency rate update
 *
 * Security Note:
 * These endpoints should be protected and accessible only to authorized administrators
 * as they can impact system performance and data consistency.
 *
 * @see CurrencyRateUpdateJob
 */
@Tag(name = "Job Operations", description = "API endpoints for managing background jobs and system maintenance tasks. These endpoints are intended for administrative use only.")
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final CurrencyRateUpdateJob currencyRateUpdateJob;

    /**
     * Endpoint to manually trigger a currency rate update job.
     * This endpoint:
     * 1. Generates a unique job ID for tracking
     * 2. Executes the currency rate update
     * 3. Measures and logs execution time
     * 4. Provides detailed error reporting
     *
     * @return ResponseEntity containing the job execution status
     *         200 OK - If the job completes successfully
     *         500 Internal Server Error - If the job fails
     */
    @Operation(summary = "Update currency rates", 
              description = "Manually trigger an update of currency exchange rates from the external API",
              tags = {"Job Operations"},
              security = {@SecurityRequirement(name = "bearer-key")})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Currency rates updated successfully"),
        @ApiResponse(responseCode = "500", description = "Update failed due to API error or other issues")
    })
    @PostMapping("/currency-rates/update")
    public ResponseEntity<String> triggerCurrencyRateUpdate() {
        String jobId = String.valueOf(System.nanoTime());
        log.info("Manual currency rate update job triggered - jobId: {}", jobId);
        
        try {
            long startTime = System.currentTimeMillis();
            currencyRateUpdateJob.updateCurrencyRates();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Currency rate update job completed successfully - jobId: {}, duration: {}ms", jobId, duration);
            return ResponseEntity.ok("Currency rate update job completed successfully");
        } catch (Exception e) {
            log.error("Currency rate update job failed - jobId: {}, error: {} - {}", 
                    jobId, e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Currency rate update failed: " + e.getMessage());
        }
    }
}
