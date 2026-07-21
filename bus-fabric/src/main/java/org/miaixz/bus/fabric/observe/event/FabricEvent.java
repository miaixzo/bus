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
 * @param marker event marker
 * @param time   event time
 * @param tags   event tags
 * @param cause  event failure cause
 * @author Kimi Liu
 * @since Java 21+
 */
public record FabricEvent(ObservationMarker marker, Instant time, Tags tags, Throwable cause) {

    /**
     * Creates a fabric event.
     *
     * @param marker event marker
     * @param time   event time
     * @param tags   event tags
     * @param cause  event failure cause
     */
    public FabricEvent {
        marker = Assert.notNull(marker, () -> new ValidateException("Observe marker must not be null"));
        time = Assert.notNull(time, () -> new ValidateException("Event time must not be null"));
        tags = Assert.notNull(tags, () -> new ValidateException("Event tags must not be null"));
    }

    /**
     * Creates an event builder.
     *
     * @param marker event marker
     * @return event builder
     */
    public static Builder builder(final ObservationMarker marker) {
        return builder(marker, Clock.system());
    }

    /**
     * Creates an event builder using an explicit runtime clock.
     *
     * @param marker event marker
     * @param clock  runtime clock
     * @return event builder
     */
    public static Builder builder(final ObservationMarker marker, final Clock clock) {
        return new Builder(Assert.notNull(marker, () -> new ValidateException("Observe marker must not be null")),
                Assert.notNull(clock, () -> new ValidateException("Event clock must not be null")));
    }

    /**
     * Returns the marker module from its stable code prefix.
     *
     * @param marker marker
     * @return module
     */
    private static String module(final ObservationMarker marker) {
        final String code = marker.code();
        final int dot = code.indexOf(Symbol.C_DOT);
        return dot < 0 ? code : code.substring(0, dot);
    }

    /**
     * Returns the marker phase from its stable code suffix.
     *
     * @param marker marker
     * @return phase
     */
    private static String phase(final ObservationMarker marker) {
        final String code = marker.code();
        final int dot = code.indexOf(Symbol.C_DOT);
        return dot < 0 || dot == code.length() - 1 ? code : code.substring(dot + 1);
    }

    /**
     * Returns the normalized marker result.
     *
     * @param marker marker
     * @return result
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
         * Event marker.
         */
        private final ObservationMarker marker;

        /**
         * Runtime clock.
         */
        private final Clock clock;

        /**
         * Tag values.
         */
        private final Map<String, String> tags;

        /**
         * Failure cause.
         */
        private Throwable cause;

        /**
         * Creates a builder.
         *
         * @param marker event marker
         * @param clock  runtime clock
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
         * @param key   key
         * @param value value
         * @return this builder
         */
        public Builder tag(final String key, final String value) {
            final String checkedKey = Tags.normalize(key, "Tag key");
            tags.put(checkedKey, Tags.sanitize(checkedKey, value));
            return this;
        }

        /**
         * Sets the failure cause.
         *
         * @param cause cause
         * @return this builder
         */
        public Builder cause(final Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Builds an event.
         *
         * @return event
         */
        public FabricEvent build() {
            if (cause != null) {
                tags.putIfAbsent(org.miaixz.bus.fabric.Builder.TAG_EXCEPTION, cause.getClass().getName());
            }
            return new FabricEvent(marker, clock.now(), Tags.of(tags), cause);
        }

    }

}
