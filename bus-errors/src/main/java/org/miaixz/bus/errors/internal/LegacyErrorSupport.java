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
package org.miaixz.bus.errors.internal;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Keeps the historical gateway status and application-code precedence independent.
 *
 * @param status      legacy gateway status
 * @param code        legacy application code
 * @param description legacy description
 *
 * @author Kimi Liu
 */
public record LegacyErrorSupport(HttpStatus status, String code, String description) {

    /**
     * Applies the legacy status and body precedence to a bounded diagnostic chain.
     *
     * @param failure original gateway error
     * @return legacy response metadata
     */
    public static LegacyErrorSupport from(Throwable failure) {
        var causes = ThrowableNormalizer.causes(failure);
        var responseStatus = ThrowableNormalizer.find(causes, ResponseStatusException.class);
        var bufferLimit = ThrowableNormalizer.find(causes, DataBufferLimitException.class);
        var unchecked = ThrowableNormalizer.find(causes, UncheckedException.class);
        boolean timeout = ThrowableNormalizer.find(causes, TimeoutException.class) != null
                || ThrowableNormalizer.find(causes, SocketTimeoutException.class) != null;
        boolean unknownHost = ThrowableNormalizer.find(causes, UnknownHostException.class) != null;
        boolean webClient = ThrowableNormalizer.find(causes, WebClientException.class) != null;
        var status = status(
                causes,
                responseStatus,
                bufferLimit != null,
                unchecked != null,
                timeout,
                unknownHost || webClient);
        if (ThrowableNormalizer.find(causes, OutOfMemoryError.class) != null) {
            return registered(status, ErrorCode._100807);
        }
        if (bufferLimit != null) {
            return registered(status, ErrorCode._100531);
        }
        if (responseStatus != null) {
            return new LegacyErrorSupport(status, Integer.toString(responseStatus.getStatusCode().value()),
                    responseStatus.getReason());
        }
        if (unchecked != null && StringKit.isNotBlank(unchecked.getCode())) {
            return new LegacyErrorSupport(status, unchecked.getCode(), unchecked.getDesc());
        }
        if (timeout) {
            return registered(status, ErrorCode._100811);
        }
        if (ThrowableNormalizer.find(causes, WebClientResponseException.class) != null) {
            return registered(status, ErrorCode._100710);
        }
        if (unknownHost) {
            return registered(status, ErrorCode._100811);
        }
        return registered(status, webClient ? ErrorCode._100710 : ErrorCode._100807);
    }

    /**
     * Selects the historical transport status independently of the legacy response body.
     *
     * @param causes         bounded diagnostic cause chain
     * @param responseStatus first Spring status exception, if present
     * @param bufferLimit    whether downstream buffering exceeded its limit
     * @param unchecked      whether a Bus unchecked error was found
     * @param timeout        whether the failure was a gateway or socket timeout
     * @param downstream     whether the downstream client or DNS lookup failed
     * @return legacy HTTP status
     */
    private static HttpStatus status(
            List<Throwable> causes,
            ResponseStatusException responseStatus,
            boolean bufferLimit,
            boolean unchecked,
            boolean timeout,
            boolean downstream) {
        if (responseStatus != null && responseStatus.getStatusCode() instanceof HttpStatus status) {
            return status;
        }
        if (bufferLimit) {
            return HttpStatus.BAD_GATEWAY;
        }
        var validation = ThrowableNormalizer.find(causes, ValidateException.class);
        if (validation != null && ErrorCode._100530.getKey().equals(validation.getCode())) {
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }
        if (timeout) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (downstream) {
            return HttpStatus.BAD_GATEWAY;
        }
        return unchecked ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Pairs the selected transport status with a registered legacy error.
     *
     * @param status legacy transport status
     * @param error  registered application code and description
     * @return legacy response metadata
     */
    private static LegacyErrorSupport registered(HttpStatus status, Errors error) {
        return new LegacyErrorSupport(status, error.getKey(), error.getValue());
    }

}
