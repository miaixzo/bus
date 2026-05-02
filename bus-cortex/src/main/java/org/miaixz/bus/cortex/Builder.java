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
package org.miaixz.bus.cortex;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.health.TcpProber;
import org.miaixz.bus.cortex.registry.HealthProbeScheduler;

/**
 * Shared constants for bus-cortex.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Builder {

    /**
     * Creates a new Builder.
     */
    private Builder() {

    }

    /**
     * Default namespace used when no explicit namespace is specified.
     */
    public static final String DEFAULT_NAMESPACE = Normal.DEFAULT;
    /**
     * Default health probe interval in milliseconds (30 s). Recommended value for {@link HealthProbeScheduler}.
     */
    public static final long DEFAULT_HEALTH_INTERVAL_MS = 30000L;
    /**
     * Default health probe timeout in milliseconds (5 s). Used by {@link TcpProber} and recommended for
     * {@link HealthProbeScheduler}.
     */
    public static final long DEFAULT_HEALTH_TIMEOUT_MS = 5000L;
    /**
     * Default security-token expiry in seconds (24 h).
     */
    public static final long DEFAULT_TOKEN_EXPIRE_SECONDS = 86400L;

    /**
     * CacheX key prefix for registry entries ({@code reg:}).
     */
    public static final String REG_PREFIX = "reg" + Symbol.COLON;
    /**
     * CacheX key prefix for setting entries ({@code cfg:}).
     */
    public static final String CFG_PREFIX = "cfg" + Symbol.COLON;
    /**
     * CacheX key prefix for setting-domain entries. Uses the historical {@code cfg:} prefix for key compatibility.
     */
    public static final String SETTING_PREFIX = CFG_PREFIX;
    /**
     * CacheX key prefix for security and token entries ({@code sec:}).
     */
    public static final String SECURITY_PREFIX = "sec" + Symbol.COLON;
    /**
     * CacheX key prefix for sequence counters ({@code seq:}).
     */
    public static final String SEQUENCE_PREFIX = "seq" + Symbol.COLON;
    /**
     * CacheX key prefix for audit log entries ({@code audit:}).
     */
    public static final String AUDIT_PREFIX = "audit" + Symbol.COLON;

}
