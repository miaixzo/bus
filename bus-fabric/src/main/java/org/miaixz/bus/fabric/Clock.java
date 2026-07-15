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
package org.miaixz.bus.fabric;

import java.time.Instant;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Fabric time source abstraction.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Clock {

    /**
     * Returns the system clock.
     *
     * @return system clock
     */
    static Clock system() {
        return Instances.get(Clock.class.getName() + ".system", SystemClock::new);
    }

    /**
     * Returns a fixed clock.
     *
     * @param instant fixed instant
     * @return fixed clock
     */
    static Clock fixed(final Instant instant) {
        if (instant == null) {
            throw new ValidateException("Instant must not be null");
        }
        return new FixedClock(instant);
    }

    /**
     * Returns the current instant.
     *
     * @return current instant
     */
    Instant now();

    /**
     * Returns epoch milliseconds.
     *
     * @return epoch milliseconds
     */
    long millis();

    /**
     * Returns monotonic nanoseconds.
     *
     * @return nanoseconds
     */
    long nanos();

    /**
     * System clock implementation.
     */
    final class SystemClock implements Clock {

        /**
         * Creates a system clock.
         */
        private SystemClock() {
            // No initialization required.
        }

        /**
         * Returns the current system instant.
         *
         * @return current system instant
         */
        @Override
        public Instant now() {
            return Instant.now();
        }

        /**
         * Returns the current system epoch milliseconds.
         *
         * @return current system epoch milliseconds
         */
        @Override
        public long millis() {
            return System.currentTimeMillis();
        }

        /**
         * Returns the current monotonic system nanoseconds.
         *
         * @return current monotonic nanoseconds
         */
        @Override
        public long nanos() {
            return System.nanoTime();
        }

    }

    /**
     * Fixed clock implementation.
     *
     * @param instant fixed instant
     */
    record FixedClock(Instant instant) implements Clock {

        /**
         * Creates a fixed clock.
         *
         * @param instant fixed instant
         */
        public FixedClock {
            if (instant == null) {
                throw new ValidateException("Instant must not be null");
            }
        }

        /**
         * Returns the fixed instant.
         *
         * @return fixed instant
         */
        @Override
        public Instant now() {
            return instant;
        }

        /**
         * Returns the fixed instant as epoch milliseconds.
         *
         * @return fixed epoch milliseconds
         */
        @Override
        public long millis() {
            return instant.toEpochMilli();
        }

        /**
         * Returns the fixed instant as epoch-derived nanoseconds.
         *
         * @return fixed epoch-derived nanoseconds
         */
        @Override
        public long nanos() {
            return instant.toEpochMilli() * 1_000_000L;
        }

    }

}
