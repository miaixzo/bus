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

import org.miaixz.bus.cache.Hybrid;
import org.miaixz.bus.cache.Options;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.guard.token.TokenGuardConfig;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for Bus Cortex starter wiring.
 * <p>
 * For client-side integrations that talk to a remote Cortex server, a minimal setup typically starts with
 * {@code bus.cortex.server-addr}. The remaining properties tune starter-provisioned cache behavior, watch delivery,
 * bridge wiring, and optional server-side components.
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
     * Creates a mutable Bus Cortex starter property holder.
     */
    public CortexProperties() {

    }

    /**
     * Enables the Cortex starter.
     */
    private boolean enabled = true;

    /**
     * Optional remote Cortex server address used by client-side integrations.
     */
    private String serverAddr = "";

    /**
     * Namespace used by the client.
     */
    private String namespace = Normal.DEFAULT;

    /**
     * Reserved starter flag for automatic self-registration of local API services.
     */
    private boolean autoRegister = true;

    /**
     * Enables setting-related starter wiring and production-store validation paths.
     */
    private boolean settingEnabled = true;

    /**
     * Marks the current application as a server-side Cortex deployment.
     */
    private boolean serverEnabled = false;

    /**
     * Reserved default remote setting group for external bootstrap integrations.
     */
    private String settingGroup = "DEFAULT";

    /**
     * Reserved default remote setting data identifier for external bootstrap integrations.
     */
    private String settingDataId = Normal.EMPTY;

    /**
     * Maximum number of historical {@code setting.item.revision} snapshots retained after publish and rollback
     * operations.
     */
    private int maxSettingVersions = 10;

    /**
     * Maximum number of watch registrations allowed in a single namespace.
     */
    private int maxWatchesPerNamespace = 1000;

    /**
     * Watch expiration time in milliseconds.
     */
    private long watchExpireMs = 86400000L;

    /**
     * Default expiration time in milliseconds for the starter-provisioned Cortex cache.
     */
    private long cacheExpireMs = Hybrid.DEFAULT_EXPIRE_MS;

    /**
     * Nested cache backend options for Cortex.
     * <p>
     * When present, these options are overlaid on top of the Cortex starter defaults so callers may override only a
     * subset of backend settings under {@code bus.cortex.cache.*}.
     * </p>
     */
    @NestedConfigurationProperty
    private Options cache;

    /**
     * Watch-specific starter properties.
     */
    private Watch watch = new Watch();

    /**
     * Bridge-specific starter properties.
     */
    private Bridge bridge = new Bridge();
    /**
     * Guard-specific starter properties.
     */
    private Guard guard = new Guard();
    /**
     * Audit-specific starter properties.
     */
    private Audit audit = new Audit();
    /**
     * Version-registry starter properties.
     */
    private Version version = new Version();

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
        return StringKit.isBlank(namespace) ? Normal.DEFAULT : namespace.trim();
    }

    /**
     * Compatibility alias for legacy callers. Prefer {@link #requireNamespace()}.
     *
     * @return configured namespace or the default namespace when blank
     */
    public String requireScope() {
        return requireNamespace();
    }

    /**
     * Returns the effective maximum retained {@code setting.item.revision} count.
     *
     * @return positive max version count
     */
    public int requireMaxSettingVersions() {
        return maxSettingVersions > 0 ? maxSettingVersions : 10;
    }

    /**
     * Returns the effective watch registration cap.
     *
     * @return positive watch cap
     */
    public int requireMaxWatchesPerNamespace() {
        return maxWatchesPerNamespace > 0 ? maxWatchesPerNamespace : 1000;
    }

    /**
     * Returns the effective watch expiration timeout.
     *
     * @return positive watch expiration in milliseconds
     */
    public long requireWatchExpireMs() {
        return watchExpireMs > 0L ? watchExpireMs : 86400000L;
    }

    /**
     * Returns the configured cache expiration timeout.
     *
     * @return positive cache expiration in milliseconds
     */
    public long requireCacheExpireMs() {
        return cacheExpireMs > 0L ? cacheExpireMs : Hybrid.DEFAULT_EXPIRE_MS;
    }

    /**
     * Nested watch properties.
     */
    @Getter
    @Setter
    public static class Watch {

        /**
         * Creates the nested watch-property holder.
         */
        public Watch() {
        }

        /**
         * Enables registration of the builtin logging watch listener.
         */
        private boolean loggingEnabled = false;

    }

    /**
     * Nested bridge properties.
     */
    @Getter
    @Setter
    public static class Bridge {

        /**
         * Creates the nested bridge-property holder.
         */
        public Bridge() {
        }

        /**
         * Bridge synchronization mode. Only {@code push+pull/pull-only} creates the remote bridge bean.
         */
        private String mode = "pull-only";

        /**
         * Remote Vortex bridge base URL.
         */
        private String url = Normal.EMPTY;

        /**
         * Maximum number of delivery retries for bridge pushes.
         */
        private int maxRetries = 3;

        /**
         * Optional event source marker attached to bridge payloads.
         */
        private String source = Normal.EMPTY;

        /**
         * Returns whether the remote bridge should be started.
         *
         * @return {@code true} when bridge mode is {@code push+pull}
         */
        public boolean isPushPullEnabled() {
            return "push+pull".equalsIgnoreCase(StringKit.trim(mode));
        }

        /**
         * Returns the required bridge URL.
         *
         * @return trimmed bridge URL
         * @throws IllegalStateException if the bridge URL is blank while push+pull mode is enabled
         */
        public String requireUrl() {
            if (StringKit.isBlank(url)) {
                throw new IllegalStateException("bus.cortex.bridge.url is required when bridge mode is push+pull");
            }
            return url.trim();
        }

        /**
         * Returns the effective retry count.
         *
         * @return positive retry count
         */
        public int requireMaxRetries() {
            return maxRetries > 0 ? maxRetries : 3;
        }

        /**
         * Returns the optional bridge source marker.
         *
         * @return trimmed source marker or empty string
         */
        public String resolveSource() {
            return StringKit.isBlank(source) ? Normal.EMPTY : source.trim();
        }

    }

    /**
     * Nested guard properties.
     */
    @Getter
    @Setter
    public static class Guard {

        /**
         * Creates the nested guard-property holder.
         */
        public Guard() {
        }

        /**
         * Enables default token guard configuration exposure.
         */
        private boolean enabled = true;

        /**
         * Default token guard configuration.
         */
        private Token token = new Token();
    }

    /**
     * Nested token-guard properties.
     */
    @Getter
    @Setter
    public static class Token extends TokenGuardConfig {

        /**
         * Creates the nested token-property holder.
         */
        public Token() {
        }

    }

    /**
     * Nested audit properties.
     */
    @Getter
    @Setter
    public static class Audit {

        /**
         * Creates the nested audit-property holder.
         */
        public Audit() {
        }

        /**
         * Enables the default audit logger bean.
         */
        private boolean enabled = false;
    }

    /**
     * Nested version properties.
     */
    @Getter
    @Setter
    public static class Version {

        /**
         * Creates the nested version-property holder.
         */
        public Version() {
        }

        /**
         * Enables the version registry bean.
         */
        private boolean enabled = false;
    }

}
