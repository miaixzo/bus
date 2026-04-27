/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.strategy;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
 * @since Java 21+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class VettingStrategy extends AbstractStrategy {

    /**
     * Creates a vetting strategy.
     */
    public VettingStrategy() {
    }

    /**
     * Applies the request parsing and validation logic in a fully reactive, non-blocking manner.
     * <p>
     * It differentiates between a standard, fully-parameterized gateway request and a simple URL-based request. The
     * validation steps are composed into a single {@link Mono} that completes successfully only if all checks pass. If
     * any validation fails, the chain is terminated with an error signal.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            Mono<Void> validationMono;
            if (Args.isCstRequest(exchange.getRequest().getPath().value())) {
                validationMono = validateAndEnrichUrlRequest(exchange, context);
            } else {
                validationMono = validateAndEnrich(exchange, context);
            }

            return validationMono.then(chain.apply(exchange));
        });
    }

    /**
     * Orchestrates the entire validation and enrichment process for a standard gateway request. Each step returns a
     * {@link Mono}, allowing them to be chained together reactively.
     *
     * @param exchange The current server exchange.
     * @param context  The request context to be validated and enriched.
     * @return A {@link Mono} that completes on success or signals an error on validation failure.
     */
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return validateParameters(context).then(Mono.defer(() -> validateTimestamp(context)))
                .then(Mono.defer(() -> validateSignature(context)))
                .then(Mono.fromRunnable(() -> enrich(exchange, context)));
    }

    /**
     * Validates and enriches a simple URL-based request that does not follow the standard gateway parameter protocol.
     * <p>
     * This method hardcodes the 'method' (from the path) and 'version' and only performs context enrichment.
     *
     * @param exchange The current server exchange.
     * @param context  The request context to be enriched.
     * @return A {@link Mono} that completes after enrichment.
     */
    protected Mono<Void> validateAndEnrichUrlRequest(ServerWebExchange exchange, Context context) {
        return Mono.fromRunnable(() -> {
            context.getParameters().put(Args.METHOD, exchange.getRequest().getPath().value());
            if (ObjectKit.isEmpty(context.getParameters().get(Args.VERSION))) {
                context.getParameters().put(Args.VERSION, Args.DEFAULT_VERSION);
            }
        }).then(Mono.fromRunnable(() -> enrich(exchange, context)));
    }

    /**
     * Validates the presence and basic integrity of standard gateway parameters. This method is wrapped in
     * {@link Mono#fromRunnable} to ensure the synchronous, exception-throwing logic is captured reactively.
     *
     * @param context The request context.
     * @return A {@link Mono} that completes on success or signals a {@link ValidateException}.
     */
    protected Mono<Void> validateParameters(Context context) {
        return Mono.fromRunnable(() -> {
            Map<String, Object> params = context.getParameters();
            checkForUndefinedValues(params);
            requireParameter(params, Args.METHOD, ErrorCode._100102);
            requireParameter(params, Args.VERSION, ErrorCode._100106);
            requireParameter(params, Args.FORMAT, ErrorCode._100104);
            requireParameter(params, Args.SIGN, ErrorCode._100108);
            requireParameter(params, Args.TIMESTAMP, ErrorCode._100110);
        });
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
     * <p>
     * Uses {@link Mono#fromCallable} to wrap the potentially blocking parsing logic and
     * {@link reactor.core.publisher.Mono#onErrorMap} to translate {@link NumberFormatException} into a domain-specific
     * {@link ValidateException}.
     *
     * @param context The request context.
     * @return A {@link Mono} that completes on success or signals a {@link ValidateException}.
     */
    protected Mono<Void> validateTimestamp(Context context) {
        return Mono.fromCallable(() -> {
            String timestampStr = String.valueOf(context.getParameters().get(Args.TIMESTAMP));
            long clientTimestampMs = Long.parseLong(timestampStr);
            long currentTimestampMs = DateKit.current();
            long absoluteDifferenceMs = Math.abs(currentTimestampMs - clientTimestampMs);

            if (absoluteDifferenceMs > TimeUnit.MINUTES.toMillis(Holder.getTimestampToleranceMinutes())) {
                logTimestampMismatch(clientTimestampMs, currentTimestampMs, absoluteDifferenceMs);
                throw new ValidateException(ErrorCode._100111);
            }
            return (Void) null;
        }).onErrorMap(NumberFormatException.class, ex -> new ValidateException(ErrorCode._100111));
    }

    /**
     * Logs a formatted, aligned message for timestamp mismatches to aid in debugging.
     *
     * @param clientTime The timestamp from the client.
     * @param serverTime The current server timestamp.
     * @param difference The difference in milliseconds.
     */
    private void logTimestampMismatch(long clientTime, long serverTime, long difference) {
        Logger.info(true, "Vetting", "*************************************************");
        Logger.info(true, "Vetting", "*" + String.format("%-" + 47 + "s", "  Client Time (ms): " + clientTime) + "*");
        Logger.info(true, "Vetting", "*" + String.format("%-" + 47 + "s", "  Server Time (ms): " + serverTime) + "*");
        Logger.info(true, "Vetting", "*" + String.format("%-" + 47 + "s", "  Difference  (ms): " + difference) + "*");
        Logger.info(true, "Vetting", "*************************************************");
    }

    /**
     * Validates the request signature to ensure authenticity and integrity.
     * <p>
     * The signature generation is a blocking operation. This method wraps it in {@link Mono#fromCallable} and
     * subscribes on {@code Schedulers.boundedElastic()} to offload the work from the event loop, preventing the server
     * from blocking.
     *
     * @param context The request context.
     * @return A {@link Mono} that completes on success or signals a {@link SignatureException}.
     */
    protected Mono<Void> validateSignature(Context context) {
        return Mono.fromCallable(() -> {
            Map<String, Object> params = context.getParameters();
            String key = StringKit.isNotEmpty(getApiKey(context)) ? getApiKey(context)
                    : String.valueOf(params.get(Args.METHOD));
            if (!validateSign(key + params.get(Args.TIMESTAMP), context.getHttpMethod().name(), params)) {
                throw new SignatureException(ErrorCode._100109);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
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
        Logger.info(true, "Vetting", "Client Sign: {}", clientSign);

        Map<String, Object> paramsForSign = new TreeMap<>(params);
        paramsForSign.remove(Args.SIGN);

        String serverSign = this.generateSignature(key, httpMethod, paramsForSign);
        Logger.info(true, "Vetting", "Server Sign: {}", serverSign);

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

        String sortedAndEncodedParams = params.entrySet().stream().map(entry -> {
            Object value = entry.getValue();
            String normalizedValue = value instanceof Map || value instanceof Collection
                    || (value != null && value.getClass().isArray()) ? JsonKit.toJsonString(value)
                            : String.valueOf(value);
            return Map.entry(entry.getKey(), normalizedValue);
        }).filter(entry -> StringKit.isNotEmpty(entry.getValue())).sorted(Map.Entry.comparingByKey())
                .map(
                        entry -> UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8)
                                + UrlEncoder.encodeAll(entry.getValue(), Charset.UTF_8))
                .collect(Collectors.joining());

        String stringToSign = httpMethod + "\n" + sortedAndEncodedParams;

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
        String x_request_id = context.getX_request_id();
        if (StringKit.isEmpty(x_request_id)) {
            x_request_id = context.getX_request_id();
            context.setX_request_id(x_request_id);
        }
        context.getParameters().put("x_request_id", x_request_id);

        String x_request_ipv4 = context.getX_request_ip();
        if (StringKit.isEmpty(x_request_ipv4)) {
            x_request_ipv4 = this.getClientIp(exchange.getRequest());
            context.setX_request_ipv4(x_request_ipv4);
        }
        context.getParameters().put("x_request_ipv4", x_request_ipv4);

        String x_request_domain = context.getX_request_domain();
        if (StringKit.isEmpty(x_request_domain)) {
            x_request_domain = this.determineRequestDomain(exchange.getRequest());
            context.setX_request_domain(x_request_domain);
        }
        context.getParameters().put("x_request_domain", x_request_domain);
    }

    /**
     * Determines the request domain (host:port) from headers.
     *
     * @param request The server HTTP request.
     * @return The determined domain or "unknown:0" as a fallback.
     */
    private String determineRequestDomain(ServerHttpRequest request) {
        return getAuthority(request).orElseGet(() -> {
            Logger.warn(
                    true,
                    "Vetting",
                    "Unable to determine request domain (host:port). Using default value: {}",
                    (Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO));
            return Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO;
        });
    }

}
