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
package org.miaixz.bus.vortex.strategy.vetting;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.strategy.VettingStrategy;
import org.miaixz.bus.core.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * REST/API vetting strategy.
 * <p>
 * REST keeps its parameter completeness, timestamp, and parameter-signature rules here. Common enrichment and
 * forwarding cleanup remain inherited from {@link VettingStrategy}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class RestVettingStrategy extends VettingStrategy {

    /**
     * Creates a REST vetting strategy.
     */
    public RestVettingStrategy() {
        super();
    }

    /**
     * Runs REST-specific validation before common enrichment and forwarding cleanup.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return validation completion signal
     */
    @Override
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return validateParameters(context).then(Mono.defer(() -> validateTimestamp(context)))
                .then(Mono.defer(() -> validateSignature(context)))
                .then(Mono.fromRunnable(() -> mergeAuthorizationAttributes(exchange, context)))
                .then(Mono.fromRunnable(() -> enrich(exchange, context)))
                .then(Mono.fromRunnable(() -> removeForwardingControlParameters(context)));
    }

    /**
     * Validates REST required gateway parameters.
     *
     * @param context request context
     * @return validation completion signal
     */
    @Override
    protected Mono<Void> validateParameters(Context context) {
        return super.validateParameters(context).then(Mono.fromRunnable(() -> {
            requireParameter(context, Args.METHOD, ErrorCode._100102);
            requireParameter(context, Args.VERSION, ErrorCode._100106);
            requireParameter(context, Args.FORMAT, ErrorCode._100104);
            requireParameter(context, Args.TIMESTAMP, ErrorCode._100110);
            if (context.getAssets() != null && Consts.ONE.equals(context.getAssets().getSign())) {
                requireParameter(context, Args.SIGN, ErrorCode._100108);
            }
        }));
    }

    /**
     * Requires a non-blank request parameter.
     *
     * @param context request context
     * @param key     required parameter name
     * @param errors  error code raised when the parameter is missing
     */
    private void requireParameter(Context context, String key, Errors errors) {
        if (StringKit.isBlank(value(context, key))) {
            throw new ValidateException(errors);
        }
    }

    /**
     * Validates the request timestamp against the configured tolerance window.
     *
     * @param context request context
     * @return validation completion signal
     */
    protected Mono<Void> validateTimestamp(Context context) {
        return Mono.fromCallable(() -> {
            String timestampStr = value(context, Args.TIMESTAMP);
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
     * Logs timestamp mismatches for replay-window troubleshooting.
     *
     * @param clientTime client timestamp in milliseconds
     * @param serverTime server timestamp in milliseconds
     * @param difference absolute difference in milliseconds
     */
    private void logTimestampMismatch(long clientTime, long serverTime, long difference) {
        Logger.warn(
                false,
                "Vortex",
                "Timestamp validation failed: strategy=rest-vetting, clientTimestampMs={}, serverTimestampMs={}, differenceMs={}, toleranceMinutes={}",
                clientTime,
                serverTime,
                difference,
                Holder.getTimestampToleranceMinutes());
    }

    /**
     * Validates the REST/API signature when the resolved asset enables signing.
     *
     * @param context request context
     * @return validation completion signal
     */
    protected Mono<Void> validateSignature(Context context) {
        if (context.getAssets() == null || !Consts.ONE.equals(context.getAssets().getSign())) {
            return Mono.empty();
        }
        return Mono.fromCallable(() -> {
            Map<String, Object> params = context.getParameters();
            String key = StringKit.isNotEmpty(getApiKey(context)) ? getApiKey(context) : value(context, Args.METHOD);
            if (!validateSign(key + value(context, Args.TIMESTAMP), context.getHttpMethod().value(), params)) {
                throw new SignatureException(ErrorCode._100109);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Recalculates and compares the REST/API request signature.
     *
     * @param key        secret key material
     * @param httpMethod current HTTP method name
     * @param params     request parameters
     * @return {@code true} when the signature matches
     */
    protected boolean validateSign(String key, String httpMethod, Map<String, Object> params) {
        String clientSign = value(params, Args.SIGN);
        if (params == null || clientSign == null) {
            return false;
        }

        Logger.debug(
                true,
                "Vortex",
                "Signature validation started: strategy=rest-vetting, httpMethod={}, parameterCount={}, clientSignatureLength={}",
                httpMethod,
                params.size(),
                clientSign.length());

        Map<String, Object> paramsForSign = copyWithoutIgnoreCase(params, Args.SIGN);

        String serverSign = this.generateSignature(key, httpMethod, paramsForSign);
        boolean matched = Objects.equals(clientSign, serverSign);
        Logger.info(
                false,
                "Vortex",
                "Signature validation completed: strategy=rest-vetting, httpMethod={}, signedParameterCount={}, matched={}, serverSignatureLength={}",
                httpMethod,
                paramsForSign.size(),
                matched,
                serverSign.length());

        return matched;
    }

    /**
     * Generates a REST/API signature using HMAC-SHA256 and Base64.
     *
     * @param key        secret key
     * @param httpMethod current HTTP method name
     * @param params     request parameters to sign
     * @return Base64-encoded signature
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

        String stringToSign = httpMethod + Symbol.LF + sortedAndEncodedParams;

        HMac hmac = Builder.hmac(Algorithm.HMACSHA256, key.getBytes(Charset.UTF_8));
        byte[] signBytes = hmac.digest(stringToSign.getBytes(Charset.UTF_8));
        return Base64.encode(signBytes);
    }

}
