package com.example.currencyconverter.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * A servlet filter that adds distributed tracing capabilities to the application.
 * This filter ensures that each HTTP request can be traced through the system by:
 * 1. Generating or propagating a unique trace ID for each request
 * 2. Adding the trace ID to the Mapped Diagnostic Context (MDC) for logging
 * 3. Including the trace ID in the response headers
 *
 * Features:
 * - Generates UUID-based trace IDs if none provided
 * - Propagates existing trace IDs from request headers
 * - Automatically cleans up MDC context after request completion
 * - Ensures trace ID is available in logs across the request lifecycle
 *
 * Usage:
 * The filter is automatically applied to all endpoints due to @Component annotation.
 * Trace IDs can be:
 * - Provided in request header 'X-Trace-ID'
 * - Retrieved from response header 'X-Trace-ID'
 * - Found in log entries via the 'traceId' field
 *
 * @see OncePerRequestFilter
 * @see MDC
 */
@Component
public class RequestTracingFilter extends OncePerRequestFilter {

    /**
     * Key used to store the trace ID in the MDC context.
     * This key will be visible in log entries.
     */
    private static final String TRACE_ID = "traceId";

    /**
     * HTTP header name used to propagate the trace ID.
     * This header can be used to pass trace IDs between services.
     */
    private static final String X_TRACE_ID = "X-Trace-ID";

    /**
     * Core filter method that processes each HTTP request exactly once.
     * This method:
     * 1. Extracts or generates a trace ID
     * 2. Adds it to MDC context
     * 3. Adds it to response headers
     * 4. Cleans up after request completion
     *
     * @param request The HTTP request being processed
     * @param response The HTTP response being generated
     * @param filterChain The filter chain for request processing
     * @throws ServletException If a servlet error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String traceId = request.getHeader(X_TRACE_ID);
            if (traceId == null || traceId.isEmpty()) {
                traceId = generateTraceId();
            }
            MDC.put(TRACE_ID, traceId);
            response.addHeader(X_TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
