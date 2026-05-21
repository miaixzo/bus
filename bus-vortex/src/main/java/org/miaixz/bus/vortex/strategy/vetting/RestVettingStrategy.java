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

import java.util.concurrent.TimeUnit;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.strategy.VettingStrategy;

import reactor.core.publisher.Mono;

/**
 * REST/API vetting strategy.
 * <p>
 * REST keeps parameter completeness and timestamp rules here. Route asset checks and route signature validation belong
 * to {@code RestQualifierStrategy}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class RestVettingStrategy extends VettingStrategy {

    /**
     * Creates a REST vetting strategy.
     */
    public RestVettingStrategy() {
        super();
    }

    /**
     * Runs REST-specific parameter and timestamp validation.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return validation completion signal
     */
    @Override
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return validateParameters(context).then(Mono.defer(() -> validateTimestamp(context)));
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
            try {
                Formats.valueOf(value(context, Args.FORMAT).toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidateException(ErrorCode._100105);
            }
            requireParameter(context, Args.TIMESTAMP, ErrorCode._100110);
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

}
