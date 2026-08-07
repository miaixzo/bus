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
package org.miaixz.bus.fabric.network;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Applies Fabric duration semantics to mutable core I/O timeout policies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class NetworkTimeout {

    /** Prevents utility-class construction. */
    private NetworkTimeout() {
        // No initialization required.
    }

    /**
     * Applies one operation timeout. Zero explicitly disables a previously configured timeout.
     *
     * @param policy   mutable I/O timeout policy; {@code null} is ignored for no-timeout transports
     * @param duration non-negative Fabric duration; {@code null} is ignored
     */
    public static void apply(final Timeout policy, final Duration duration) {
        if (policy == null || duration == null) {
            return;
        }
        if (duration.isNegative()) {
            throw new ValidateException("Network timeout must not be negative");
        }
        if (duration.isZero()) {
            policy.clearTimeout();
            return;
        }
        policy.timeout(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Copies the operation timeout duration from one mutable policy to another.
     *
     * @param target target policy controlling the physical operation; {@code null} is ignored
     * @param source source policy configured by the protocol layer; {@code null} is ignored
     */
    public static void apply(final Timeout target, final Timeout source) {
        if (target == null || source == null) {
            return;
        }
        final long nanos = source.timeoutNanos();
        if (nanos == 0L) {
            target.clearTimeout();
        } else {
            target.timeout(nanos, TimeUnit.NANOSECONDS);
        }
    }

}
