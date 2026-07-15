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
package org.miaixz.bus.fabric.protocol.sse;

import java.time.Duration;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Immutable server-sent event value.
 *
 * @param id    event identifier or null when absent
 * @param event event type
 * @param data  event data
 * @param retry retry directive or null when absent
 * @author Kimi Liu
 * @since Java 21+
 */
public record SseEvent(String id, String event, String data, Duration retry) {

    /**
     * Creates a validated event.
     *
     * @param id    event identifier
     * @param event event type
     * @param data  event data
     * @param retry retry directive
     */
    public SseEvent {
        id = validateSingleLine(id, "SSE event id", true);
        event = normalizeEvent(event);
        data = normalizeData(data);
        retry = validateRetry(retry);
    }

    /**
     * Creates an event.
     *
     * @param id    event identifier
     * @param event event type
     * @param data  event data
     * @param retry retry directive
     * @return immutable event
     */
    public static SseEvent of(final String id, final String event, final String data, final Duration retry) {
        return new SseEvent(id, event, data, retry);
    }

    /**
     * Returns the event identifier.
     *
     * @return event identifier or null
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    @Override
    public String event() {
        return event;
    }

    /**
     * Returns event data.
     *
     * @return event data
     */
    @Override
    public String data() {
        return data;
    }

    /**
     * Returns retry directive.
     *
     * @return retry directive or null
     */
    @Override
    public Duration retry() {
        return retry;
    }

    /**
     * Normalizes an event type.
     *
     * @param value event type
     * @return normalized event type
     */
    private static String normalizeEvent(final String value) {
        if (value == null) {
            return Builder.SSE_DEFAULT_EVENT;
        }
        boolean nonBlank = false;
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current == Symbol.C_CR || current == Symbol.C_LF || current < Symbol.C_SPACE) {
                throw new ValidateException("SSE event type must be single-line text");
            }
            if (current > Symbol.C_SPACE) {
                nonBlank = true;
            }
        }
        return nonBlank ? value : Builder.SSE_DEFAULT_EVENT;
    }

    /**
     * Normalizes event data.
     *
     * @param value data
     * @return normalized data
     */
    private static String normalizeData(final String value) {
        if (value == null) {
            return Normal.EMPTY;
        }
        final int length = value.length();
        for (int i = 0; i < length; i++) {
            final char current = value.charAt(i);
            if (current == Symbol.C_CR || current < Symbol.C_SPACE && current != Symbol.C_LF) {
                throw new ValidateException("SSE event data must not contain control characters");
            }
        }
        return value;
    }

    /**
     * Validates a single-line text component.
     *
     * @param value    value
     * @param name     field name
     * @param nullable whether null is allowed
     * @return validated value
     */
    private static String validateSingleLine(final String value, final String name, final boolean nullable) {
        if (value == null) {
            if (nullable) {
                return null;
            }
            throw new ValidateException(name + " must not be null");
        }
        final int length = value.length();
        for (int i = 0; i < length; i++) {
            final char current = value.charAt(i);
            if (current == Symbol.C_CR || current == Symbol.C_LF || current < Symbol.C_SPACE) {
                throw new ValidateException(name + " must be single-line text");
            }
        }
        return value;
    }

    /**
     * Validates retry directives.
     *
     * @param value retry
     * @return retry
     */
    private static Duration validateRetry(final Duration value) {
        Assert.isFalse(
                value != null && value.isNegative(),
                () -> new ValidateException("SSE retry must be non-negative"));
        return value;
    }

}
