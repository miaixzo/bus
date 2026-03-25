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
package org.miaixz.bus.starter.cortex;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for Bus Cortex starter wiring.
 * <p>
 * The default client entrypoint keeps setup intentionally simple: a regular application only needs
 * {@code bus.cortex.server-addr}. The remaining properties are optional tuning switches for starter-side cache,
 * watches, config loading and self-registration behavior.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(GeniusBuilder.CORTEX)
public class CortexProperties {

    /**
     * Enables the Cortex starter.
     */
    private boolean enabled = true;

    /**
     * Cortex server address.
     */
    private String serverAddr = "";

    /**
     * Namespace used by the client.
     */
    private String namespace = Builder.DEFAULT_NAMESPACE;

    /**
     * Enables automatic self-registration for local API services.
     */
    private boolean autoRegister = true;

    /**
     * Enables startup-time config loading and annotation-based config injection.
     */
    private boolean configEnabled = true;

    /**
     * Enables optional server-side Cortex controllers inside the current application.
     */
    private boolean serverEnabled = false;

    /**
     * Remote config group used by startup property loading.
     */
    private String configGroup = "DEFAULT_GROUP";

    /**
     * Remote config data identifier used by startup property loading.
     */
    private String configDataId = Normal.EMPTY;

    /**
     * Maximum number of historical config versions retained in the local cache.
     */
    private int maxConfigVersions = 10;

    /**
     * Maximum number of watch registrations allowed in a single namespace.
     */
    private int maxWatchesPerNamespace = 1000;

    /**
     * Watch expiration time in milliseconds.
     */
    private long watchExpireMs = 86400000L;

    /**
     * Default expiration time in milliseconds for the local starter cache.
     */
    private long cacheExpireMs = 180000L;

    /**
     * Returns the required server address.
     *
     * @return trimmed server address
     * @throws IllegalStateException if {@code bus.cortex.server-addr} is blank
     */
    public String requireServerAddr() {
        if (StringKit.isBlank(serverAddr)) {
            throw new IllegalStateException("bus.cortex.server-addr is required");
        }
        return serverAddr.trim();
    }

    /**
     * Returns the effective namespace.
     *
     * @return configured namespace or the default namespace when blank
     */
    public String requireNamespace() {
        return StringKit.isBlank(namespace) ? Builder.DEFAULT_NAMESPACE : namespace.trim();
    }

    /**
     * Compatibility alias for legacy callers. Prefer {@link #requireNamespace()}.
     *
     * @return configured namespace or the default namespace when blank
     */
    public String requireScope() {
        return requireNamespace();
    }

}
