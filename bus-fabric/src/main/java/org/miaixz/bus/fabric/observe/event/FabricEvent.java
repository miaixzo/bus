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
package org.miaixz.bus.fabric.observe.event;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.tags.Tags;

/**
 * Immutable fabric observation event.
 *
 * @param marker stable observation marker
 * @param time   wall-clock event timestamp
 * @param tags   immutable sanitized event tags
 * @param cause  optional failure cause
 * @author Kimi Liu
 * @since Java 21+
 */
public record FabricEvent(ObservationMarker marker, Instant time, Tags tags, Throwable cause) {

    /**
     * Creates a fabric event.
     *
     * @param marker non-null observation marker
     * @param time   non-null wall-clock timestamp
     * @param tags   non-null immutable sanitized tags
     * @param cause  optional failure cause
     * @throws ValidateException if marker, time, or tags is {@code null}
     */
    public FabricEvent {
        marker = Assert.notNull(marker, () -> new ValidateException("Observe marker must not be null"));
        time = Assert.notNull(time, () -> new ValidateException("Event time must not be null"));
        tags = Assert.notNull(tags, () -> new ValidateException("Event tags must not be null"));
    }

    /**
     * Creates an event builder.
     *
     * @param marker observation marker used to derive default classification tags
     * @return builder using the system clock and a generated operation identifier
     * @throws ValidateException if {@code marker} is {@code null}
     */
    public static Builder builder(final ObservationMarker marker) {
        return builder(marker, Clock.system());
    }

    /**
     * Creates an event builder using an explicit runtime clock.
     *
     * @param marker observation marker used to derive default classification tags
     * @param clock  runtime time source sampled when the event is built
     * @return builder initialized with marker-derived tags and a generated operation identifier
     * @throws ValidateException if the marker or clock is {@code null}
     */
    public static Builder builder(final ObservationMarker marker, final Clock clock) {
        return new Builder(Assert.notNull(marker, () -> new ValidateException("Observe marker must not be null")),
                Assert.notNull(clock, () -> new ValidateException("Event clock must not be null")));
    }

    /**
     * Returns the marker module from its stable code prefix.
     *
     * @param marker observation marker whose stable code is inspected
     * @return code prefix before the first dot, or the complete code when no dot exists
     */
    private static String module(final ObservationMarker marker) {
        final String code = marker.code();
        final int dot = code.indexOf(Symbol.C_DOT);
        return dot < 0 ? code : code.substring(0, dot);
    }

    /**
     * Returns the marker phase from its stable code suffix.
     *
     * @param marker observation marker whose stable code is inspected
     * @return code suffix after the first dot, or the complete code when no non-empty suffix exists
     */
    private static String phase(final ObservationMarker marker) {
        final String code = marker.code();
        final int dot = code.indexOf(Symbol.C_DOT);
        return dot < 0 || dot == code.length() - 1 ? code : code.substring(dot + 1);
    }

    /**
     * Returns the normalized marker result.
     *
     * @param marker observation marker whose failure and terminal flags are classified
     * @return {@code failure}, {@code success}, or {@code active}
     */
    private static String result(final ObservationMarker marker) {
        if (marker.failure()) {
            return org.miaixz.bus.fabric.Builder.METER_EVENT_OBSERVER_FAILURE;
        }
        return marker.terminal() ? "success" : "active";
    }

    /**
     * Builder for fabric events.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Marker from which default module, protocol, phase, and result tags are derived.
         */
        private final ObservationMarker marker;

        /**
         * Borrowed time source sampled only when {@link #build()} is called.
         */
        private final Clock clock;

        /**
         * Mutable sanitized tag values retained in insertion order until build time.
         */
        private final Map<String, String> tags;

        /**
         * Optional failure attached to the event.
         */
        private Throwable cause;

        /**
         * Creates a builder.
         *
         * @param marker validated observation marker
         * @param clock  validated runtime time source
         */
        private Builder(final ObservationMarker marker, final Clock clock) {
            this.marker = marker;
            this.clock = clock;
            this.tags = new LinkedHashMap<>();
            tag(org.miaixz.bus.fabric.Builder.TAG_MODULE, module(marker));
            tag(org.miaixz.bus.fabric.Builder.TAG_PROTOCOL, module(marker));
            tag(org.miaixz.bus.fabric.Builder.TAG_PHASE, phase(marker));
            tag(org.miaixz.bus.fabric.Builder.TAG_RESULT, result(marker));
            tag(org.miaixz.bus.fabric.Builder.TAG_OPERATION_ID, ID.objectId());
        }

        /**
         * Adds a tag.
         *
         * @param key   non-blank, single-line tag key
         * @param value non-blank, single-line tag content to sanitize
         * @return this builder
         * @throws ValidateException if the key or value is invalid
         */
        public Builder tag(final String key, final String value) {
            final String checkedKey = Tags.normalize(key, "Tag key");
            tags.put(checkedKey, Tags.sanitize(checkedKey, value));
            return this;
        }

        /**
         * Sets the failure cause.
         *
         * @param cause failure to attach, or {@code null} to clear a previously configured cause
         * @return this builder
         */
        public Builder cause(final Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Builds an event.
         *
         * @return immutable event timestamped at build time with a sanitized tag snapshot
         */
        public FabricEvent build() {
            if (cause != null) {
                tags.putIfAbsent(org.miaixz.bus.fabric.Builder.TAG_EXCEPTION, cause.getClass().getName());
            }
            return new FabricEvent(marker, clock.now(), Tags.of(tags), cause);
        }

    }

}
