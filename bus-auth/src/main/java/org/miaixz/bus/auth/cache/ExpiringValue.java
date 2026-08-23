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
package org.miaixz.bus.auth.cache;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;

/**
 * Associates an immutable authentication state value with its absolute expiration time.
 * <p>
 * The wrapper does not read system time or reset a caller's timeout. Consumers compare {@code expiresAt} with the Clock
 * supplied by RuntimeServices. {@link AuthCache} derives the backend TTL from this absolute deadline without resetting
 * it. Values containing mutable secret arrays are prohibited from this storage boundary.
 * </p>
 *
 * @param value     immutable authentication state value
 * @param expiresAt absolute expiration instant
 * @param <T>       authentication state value type
 * @author Kimi Liu
 */
public record ExpiringValue<T>(T value, Instant expiresAt) implements Serializable {

    /**
     * Stable serialization version for expiring cache values.
     */
    @Serial
    private static final long serialVersionUID = 2852230011506L;

    /**
     * Creates an expiring immutable state value.
     *
     * @param value     non-null authentication state value
     * @param expiresAt absolute expiration instant
     * @throws IllegalArgumentException if either component is {@code null}
     */
    public ExpiringValue {
        Assert.notNull(value, "Expiring authentication value must not be null");
        Assert.notNull(expiresAt, "Authentication value expiration must not be null");
    }

}
