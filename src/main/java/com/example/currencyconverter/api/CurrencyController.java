package com.example.currencyconverter.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for currency conversion operations.
 *
 * This controller provides endpoints for:
 * 1. Converting between specific currency pairs
 * 2. Converting from one currency to all available currencies
 * 3. Retrieving list of supported currencies
 *
 * API Endpoints:
 * - POST /convert - Convert between specific currency pairs
 * - POST /convert/all - Convert to all available currencies
 * - GET /currencies - Get list of supported currencies
 *
 * Features:
 * - Input validation
 * - Error handling
 * - Request tracing (via MDC)
 * - JSON response formatting
 *
 * @see com.example.currencyconverter.service.CurrencyConverterifc
 * @see com.example.currencyconverter.result.CurrencyConversionRequest
 * @see com.example.currencyconverter.result.CurrencyConversionResult
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.example.currencyconverter.api.result.CurrencyConversionRequest;
import com.example.currencyconverter.api.result.CurrencyConversionResult;
import com.example.currencyconverter.api.service.CurrencyConverterifc;
import com.example.currencyconverter.data.CurrencyConversionMap;
import com.example.currencyconverter.data.model.Currency;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Tag(name = "Currency Operations", description = "API endpoints for currency conversion and management. Provides functionality for converting between currencies and retrieving supported currency information.")
@RestController
@RequestMapping("/api/v1")
public class CurrencyController {

    private final CurrencyConverterifc currencyConverter;
    private final CurrencyConversionMap conversionMap;

    @Autowired
    public CurrencyController(CurrencyConverterifc currencyConverter, CurrencyConversionMap conversionMap) {
        this.currencyConverter = currencyConverter;
        this.conversionMap = conversionMap;
    }

    @Operation(summary = "Convert currency", 
    description = "Convert an amount from one currency to another using real-time exchange rates",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Currency conversion request containing source amount, from currency, and to currency",
        required = true,
        content = @Content(schema = @Schema(implementation = CurrencyConversionRequest.class))
    ))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful conversion",
            content = @Content(schema = @Schema(implementation = CurrencyConversionResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Currency not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResult> convertCurrency(
        @Parameter(description = "Currency conversion request details", required = true)
        @RequestBody CurrencyConversionRequest request) {
        log.debug("Received conversion request: {}", request);
        if (request == null || request.getAmount() == null || request.getFromCurrency() == null || request.getToCurrency() == null) {
            log.error("Invalid conversion request - missing required fields");
            return ResponseEntity.badRequest().build();
        }
        log.info("Converting {} {} to {}", request.getAmount(), request.getFromCurrency(), request.getToCurrency());
        CurrencyConversionResult result = currencyConverter.convertCurrency(request);
        if (result == null) {
            log.error("Conversion failed for {} {} to {}", request.getAmount(), request.getFromCurrency(), request.getToCurrency());
            return ResponseEntity.internalServerError().build();
        } else {
            log.debug("Successfully converted {} {} to {}", request.getAmount(), request.getFromCurrency(), request.getToCurrency());
            return ResponseEntity.ok(result);
        }
    }
    
    @Operation(summary = "Convert to all currencies", 
    description = "Convert an amount from one currency to all supported currencies using real-time exchange rates",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Currency conversion request containing source amount and from currency",
        required = true,
        content = @Content(schema = @Schema(implementation = CurrencyConversionRequest.class))
    ))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful conversion",
            content = @Content(schema = @Schema(implementation = CurrencyConversionResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Currency not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/convert/all")
    public ResponseEntity<CurrencyConversionResult> convertToAllCurrencies(
        @Parameter(description = "Currency conversion request details", required = true)
        @RequestBody CurrencyConversionRequest request) {
        log.debug("Received convert-to-all request: {}", request);
        if (request == null || request.getAmount() == null || request.getFromCurrency() == null) {
            log.error("Invalid convert-to-all request - missing required fields");
            return ResponseEntity.badRequest().build();
        }
        log.info("Converting {} {} to all supported currencies", request.getAmount(), request.getFromCurrency());
        CurrencyConversionResult result = currencyConverter.convertCurrencyToAll(request);
        if (result == null) {
            log.error("Conversion to all currencies failed for {} {}", request.getAmount(), request.getFromCurrency());
            return ResponseEntity.internalServerError().build();
        } else {
            log.debug("Successfully converted {} {} to {} currencies", request.getAmount(), request.getFromCurrency(), 
                result.getCurrencyValues().size());
            return ResponseEntity.ok(result);
        }
    }

    @Operation(summary = "Get supported currencies", description = "Retrieve a list of all supported currencies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of supported currencies retrieved successfully",
            content = @Content(schema = @Schema(implementation = Currency.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/currencies")
    public ResponseEntity<List<Currency>> getSupportedCurrencies() {
        log.debug("Fetching supported currencies from cache");
        List<Currency> currencies = new ArrayList<>(conversionMap.getAllCurrencies().values());
        log.info("Found {} supported currencies", currencies.size());
        return ResponseEntity.ok(currencies);
    }
}
