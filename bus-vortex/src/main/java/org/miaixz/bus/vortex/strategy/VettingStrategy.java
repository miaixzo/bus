/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.strategy;

import java.util.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * The strategy responsible for "vetting" incoming requests by performing critical validations.
 * <p>
 * As one of the first strategies in the chain (with high precedence), its primary roles are:
 * <ol>
 * <li>Validating the presence and format of core gateway parameters (e.g., method, version, timestamp, sign).</li>
 * <li>Validating the request timestamp to prevent replay attacks.</li>
 * <li>Validating the request signature (e.g., HMAC) to ensure data integrity and authenticity.</li>
 * <li>Enriching the request {@link Context} with derived information like client IP and domain.</li>
 * </ol>
 * This strategy ensures that a request is well-formed, timely, and authentic before it is passed to subsequent
 * strategies like authorization or rate limiting.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class VettingStrategy extends AbstractStrategy {

    /**
     * Applies the request parsing and validation logic.
     * <p>
     * It differentiates between a standard, fully-parameterized gateway request and a simple URL-based request.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            if (Args.isUrlRequest(exchange.getRequest())) {
                validateAndEnrichUrlRequest(exchange, context);
            } else {
                validateAndEnrich(exchange, context);
            }
            return chain.apply(exchange);
        });
    }

    /**
     * Orchestrates the entire validation and enrichment process for a standard gateway request.
     *
     * @param exchange The current server exchange.
     * @param context  The request context to be validated and enriched.
     */
    protected void validateAndEnrich(ServerWebExchange exchange, Context context) {
        this.validateParameters(context);
        this.validateTimestamp(context);
        this.validateSignature(context);
        this.enrich(exchange, context);
    }

    /**
     * Validates and enriches a simple URL-based request that does not follow the standard gateway parameter protocol.
     * <p>
     * This method hardcodes the 'method' (from the path) and 'version' and only performs timestamp validation and
     * context enrichment.
     *
     * @param exchange The current server exchange.
     * @param context  The request context to be enriched.
     */
    protected void validateAndEnrichUrlRequest(ServerWebExchange exchange, Context context) {
        context.getParameters().put(Args.METHOD, exchange.getRequest().getPath().value());
        if (ObjectKit.isEmpty(context.getParameters().get(Args.VERSION))) {
            context.getParameters().put(Args.VERSION, Args.DEFAULT_VERSION);
        }
        this.validateTimestamp(context);
        this.enrich(exchange, context);
    }

    /**
     * Validates the presence and basic integrity of standard gateway parameters.
     *
     * @param context The request context.
     */
    protected void validateParameters(Context context) {
        Map<String, Object> params = context.getParameters();
        this.checkForUndefinedValues(params);
        this.requireParameter(params, Args.METHOD, ErrorCode._100110);
        this.requireParameter(params, Args.VERSION, ErrorCode._100109);
        this.requireParameter(params, Args.FORMAT, ErrorCode._100113);
        this.requireParameter(params, Args.SIGN, ErrorCode._100114);
        this.requireParameter(params, Args.TIMESTAMP, ErrorCode._100115);
    }

    /**
     * Checks for "undefined" string values in parameters, which often indicate client-side issues.
     *
     * @param params The request parameters.
     */
    private void checkForUndefinedValues(Map<String, Object> params) {
        boolean hasUndefinedValue = params.entrySet().stream().anyMatch(
                entry -> Normal.UNDEFINED.equalsIgnoreCase(entry.getKey())
                        || Normal.UNDEFINED.equalsIgnoreCase(String.valueOf(entry.getValue())));

        if (hasUndefinedValue) {
            throw new ValidateException(ErrorCode._100101);
        }
    }

    /**
     * Helper method to require a non-blank parameter.
     *
     * @param params The request parameters.
     * @param key    The key of the required parameter.
     * @param errors The error code to throw if the parameter is missing.
     */
    private void requireParameter(Map<String, Object> params, String key, Errors errors) {
        if (StringKit.isBlank(Optional.ofNullable(params.get(key)).map(Object::toString).orElse(null))) {
            throw new ValidateException(errors);
        }
    }

    /**
     * Validates the request timestamp to ensure it's within an acceptable time window (e.g., 10 minutes) to prevent
     * replay attacks.
     *
     * @param context The request context.
     */
    protected void validateTimestamp(Context context) {
        String timestampStr = String.valueOf(context.getParameters().get(Args.TIMESTAMP));
        try {
            long clientTimestampMs = Long.parseLong(timestampStr);
            long currentTimestampMs = DateKit.current();
            long absoluteDifferenceMs = Math.abs(currentTimestampMs - clientTimestampMs);

            if (absoluteDifferenceMs > TimeUnit.MINUTES.toMillis(10)) {
                this.logTimestampMismatch(clientTimestampMs, currentTimestampMs, absoluteDifferenceMs);
                throw new ValidateException(ErrorCode._100107);
            }
        } catch (NumberFormatException e) {
            Logger.error("Validation failed: Timestamp format is not a valid millisecond long: {}", timestampStr);
            throw new ValidateException(ErrorCode._100107);
        }
    }

    /**
     * Logs a formatted, aligned message for timestamp mismatches to aid in debugging.
     *
     * @param clientTime The timestamp from the client.
     * @param serverTime The current server timestamp.
     * @param difference The difference in milliseconds.
     */
    private void logTimestampMismatch(long clientTime, long serverTime, long difference) {
        Logger.info("*************************************************");
        Logger.info("*" + String.format("%-" + 47 + "s", "  Client Time (ms): " + clientTime) + "*");
        Logger.info("*" + String.format("%-" + 47 + "s", "  Server Time (ms): " + serverTime) + "*");
        Logger.info("*" + String.format("%-" + 47 + "s", "  Difference (ms) : " + difference) + "*");
        Logger.info("*************************************************");
    }

    /**
     * Validates the request signature to ensure authenticity and integrity.
     *
     * @param context The request context.
     */
    protected void validateSignature(Context context) {
        Map<String, Object> params = context.getParameters();
        String key = ObjectKit.isNotEmpty(params.get(Args.APIKEY)) ? String.valueOf(params.get(Args.APIKEY))
                : String.valueOf(params.get(Args.METHOD));

        if (!this.validateSign(key + params.get(Args.TIMESTAMP), context.getHttpMethod().name(), params)) {
            throw new SignatureException(ErrorCode._100106);
        }
    }

    /**
     * Verifies if the signature of an API request is valid by recalculating it and comparing it to the one provided by
     * the client.
     *
     * @param key        The secret key used for signing (e.g., API key or method name).
     * @param httpMethod The HTTP method of the request (e.g., "POST").
     * @param params     The map of request parameters.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    protected boolean validateSign(String key, String httpMethod, Map<String, Object> params) {
        if (params == null || !params.containsKey(Args.SIGN)) {
            return false;
        }

        String clientSign = String.valueOf(params.get(Args.SIGN));
        Logger.info("==>Client Sign: {}", clientSign);

        Map<String, Object> paramsForSign = new TreeMap<>(params);
        paramsForSign.remove(Args.SIGN);

        String serverSign = this.generateSignature(key, httpMethod, paramsForSign);
        Logger.info("==>Server Sign: {}", serverSign);

        return Objects.equals(clientSign, serverSign);
    }

    /**
     * Generates an API signature using the HMAC-SHA256 algorithm.
     * <p>
     * The signature is created by: 1. Filtering out empty parameters and the 'sign' parameter itself. 2. Sorting the
     * remaining parameters by key. 3. URL-encoding and concatenating keys and values. 4. Prepending the HTTP method and
     * a newline. 5. Signing the resulting string with the provided key using HMAC-SHA256. 6. Base64-encoding the binary
     * signature.
     *
     * @param key        The secret key.
     * @param httpMethod The HTTP method.
     * @param params     The parameters to sign.
     * @return A Base64-encoded signature string.
     */
    protected String generateSignature(String key, String httpMethod, Map<String, Object> params) {
        if (StringKit.isEmpty(key) || httpMethod == null || params == null) {
            throw new IllegalArgumentException("Key, http method, and params cannot be null or empty.");
        }

        // 1. Filter, encode, and sort parameters
        String sortedAndEncodedParams = params.entrySet().stream()
                .filter(entry -> StringKit.isNotEmpty(String.valueOf(entry.getValue())))
                .sorted(Map.Entry.comparingByKey())
                .map(
                        entry -> UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8)
                                + UrlEncoder.encodeAll(String.valueOf(entry.getValue()), Charset.UTF_8))
                .collect(Collectors.joining());

        // 2. Construct the string to be signed
        String stringToSign = httpMethod + "\n" + sortedAndEncodedParams;

        // 3. Sign and encode
        HMac hmac = Builder.hmac(Algorithm.HMACSHA256, key.getBytes(Charset.UTF_8));
        byte[] signBytes = hmac.digest(stringToSign.getBytes(Charset.UTF_8));
        return Base64.encode(signBytes);
    }

    /**
     * Enriches the context with derived information (IP, domain, etc.) and adds it to the parameter map for downstream
     * use.
     *
     * @param exchange The current server exchange.
     * @param context  The request context.
     */
    protected void enrich(ServerWebExchange exchange, Context context) {
        Map<String, Object> params = context.getParameters();
        params.put("x_request_id", exchange.getRequest().getId());

        String clientIp = this.determineClientIp(exchange.getRequest());
        context.setX_request_ipv4(clientIp);
        params.put("x_request_ipv4", clientIp);

        String domain = this.determineRequestDomain(exchange.getRequest());
        context.setX_request_domain(domain);
        params.put("x_request_domain", domain);
    }

    /**
     * Determines the client IP address from request headers, checking common proxy headers first.
     *
     * @param request The server HTTP request.
     * @return The determined client IP address or "unknown".
     */
    private String determineClientIp(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst("x_request_ip")).orElseGet(
                () -> Optional.ofNullable(request.getHeaders().getFirst("X-Forwarded-For")).orElseGet(
                        () -> Optional.ofNullable(request.getRemoteAddress())
                                .map(address -> address.getAddress().getHostAddress()).orElse("unknown")));
    }

    /**
     * _ * Determines the request domain (host:port) from headers.
     *
     * @param request The server HTTP request.
     * @return The determined domain or "unknown:0" as a fallback.
     */
    private String determineRequestDomain(ServerHttpRequest request) {
        return getAuthority(request).orElseGet(() -> {
            Logger.warn(
                    "==> Filter: Unable to determine the request domain (host:port). Using default value: {}",
                    Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO);
            return Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO;
        });
    }

}
