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
package org.miaixz.bus.auth.vendor;

import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Context;

/**
 * Explicit construction dependencies for a third-party authentication client.
 *
 * @param registration immutable vendor client registration
 * @param cache        vendor response and compatibility cache
 * @param fabric       caller-owned Fabric context
 * @param clock        explicit Fabric clock used for security decisions
 * @param stateStore   tenant-aware atomic state store
 * @param secrets      caller-owned secret resolver for vendor client credentials
 * @author Kimi Liu
 */
public record VendorConfiguration(VendorRegistration registration, CacheX cache, Context fabric, Clock clock,
        StateStore stateStore, SecretResolver secrets) {

    /**
     * Validates every explicit construction dependency.
     *
     * @throws ValidateException if any dependency is null
     */
    public VendorConfiguration {
        if (registration == null || cache == null || fabric == null || clock == null || stateStore == null
                || secrets == null) {
            throw new ValidateException("Vendor configuration dependencies must not be null");
        }
    }

}
