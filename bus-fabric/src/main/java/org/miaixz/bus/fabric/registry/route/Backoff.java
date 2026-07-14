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
package org.miaixz.bus.fabric.registry.route;

import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Route retry backoff memory with an absolute retry boundary.
 *
 * @param route        failed route
 * @param failedAt     failure time
 * @param failures     consecutive failure count
 * @param backoffUntil retry boundary
 * @author Kimi Liu
 * @since Java 21+
 */
public record Backoff(Route route, Instant failedAt, int failures, Instant backoffUntil) {

    /**
     * Creates route retry backoff memory.
     */
    public Backoff {
        route = Assert.notNull(route, () -> new ValidateException("Route must not be null"));
        failedAt = Assert.notNull(failedAt, () -> new ValidateException("Route backoff times must not be null"));
        backoffUntil = Assert
                .notNull(backoffUntil, () -> new ValidateException("Route backoff times must not be null"));
        Assert.isTrue(
                failures > Normal._0,
                () -> new ValidateException("Route backoff failure count must be positive"));
    }

    /**
     * Returns whether the route is still under backoff.
     *
     * @param now current time
     * @return true when postponed
     */
    public boolean postponed(final Instant now) {
        final Instant current = Assert.notNull(now, () -> new ValidateException("Current time must not be null"));
        return current.isBefore(backoffUntil);
    }

    /**
     * Returns the redacted route identifier.
     *
     * @return route identifier
     */
    public String routeId() {
        return route.id();
    }

    @Override
    public String toString() {
        return "Backoff[routeId=" + routeId() + ", failedAt=" + failedAt + ", failures=" + failures + ", backoffUntil="
                + backoffUntil + "]";
    }

}
