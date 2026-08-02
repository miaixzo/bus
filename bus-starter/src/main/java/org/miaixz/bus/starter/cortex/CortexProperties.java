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

import java.time.Duration;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.cache.Options;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.guard.token.TokenGuardConfig;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable Cortex starter configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(GeniusBuilder.CORTEX)
public final class CortexProperties {

    /**
     * Whether the cortex integration is enabled.
     */
    private final boolean enabled;
    /**
     * Cortex server address used by configuration and watch clients.
     */
    private final String serverAddr;
    /**
     * Logical namespace that isolates Cortex data and watches.
     */
    private final String namespace;
    /**
     * Whether this application registers itself with Cortex during startup.
     */
    private final boolean autoRegister;
    /**
     * Whether remote Cortex settings are loaded into the Spring environment.
     */
    private final boolean settingEnabled;
    /**
     * Whether embedded Cortex server integration is enabled.
     */
    private final boolean serverEnabled;
    /**
     * Remote configuration group queried by the settings client.
     */
    private final String settingGroup;
    /**
     * Remote configuration data identifier queried by the settings client.
     */
    private final String settingDataId;
    /**
     * Maximum number of remote setting versions retained locally.
     */
    private final int maxSettingVersions;
    /**
     * Maximum active watches permitted within one namespace.
     */
    private final int maxWatchesPerNamespace;
    /**
     * Inactivity duration after which a watch is expired.
     */
    private final Duration watchExpire;
    /**
     * Time-to-live applied to locally cached Cortex data.
     */
    private final Duration cacheExpire;
    /**
     * Cache backend settings used by the Cortex integration.
     */
    private final Options cache;
    /**
     * Watch delivery and lifecycle settings.
     */
    private final Watch watch;
    /**
     * External configuration bridge settings.
     */
    private final Bridge bridge;
    /**
     * Request authorization settings for Cortex operations.
     */
    private final Guard guard;
    /**
     * Audit event collection and retention settings.
     */
    private final Audit audit;
    /**
     * Version-history limits and retention settings.
     */
    private final Version version;

    /**
     * Creates Cortex properties after validating limits for caching, watches, bridging, security, and history.
     *
     * @param enabled                whether Cortex integration is enabled
     * @param serverAddr             remote server address
     * @param namespace              client namespace
     * @param autoRegister           whether local APIs are registered automatically
     * @param settingEnabled         whether setting integration is enabled
     * @param serverEnabled          whether this application hosts Cortex server components
     * @param settingGroup           default setting group
     * @param settingDataId          default setting identifier
     * @param maxSettingVersions     retained setting revision count
     * @param maxWatchesPerNamespace watch registration limit per namespace
     * @param watchExpire            watch registration expiry
     * @param cacheExpire            default Cortex cache expiry
     * @param cache                  optional cache backend options
     * @param watch                  watch options
     * @param bridge                 bridge options
     * @param guard                  guard options
     * @param audit                  audit options
     * @param version                version-registry options
     */
    public CortexProperties(@DefaultValue(Normal.FALSE) boolean enabled, @DefaultValue(Normal.EMPTY) String serverAddr,
            @DefaultValue(Normal.DEFAULT) String namespace, @DefaultValue("true") boolean autoRegister,
            @DefaultValue("true") boolean settingEnabled, @DefaultValue("false") boolean serverEnabled,
            @DefaultValue("DEFAULT") String settingGroup, @DefaultValue(Normal.EMPTY) String settingDataId,
            @DefaultValue("10") int maxSettingVersions, @DefaultValue("1000") int maxWatchesPerNamespace,
            @DefaultValue("24h") Duration watchExpire, @DefaultValue("1h") Duration cacheExpire, Options cache,
            @DefaultValue Watch watch, @DefaultValue Bridge bridge, @DefaultValue Guard guard,
            @DefaultValue Audit audit, @DefaultValue Version version) {
        if (maxSettingVersions <= 0 || maxWatchesPerNamespace <= 0) {
            throw new IllegalArgumentException("Cortex version and watch limits must be greater than zero");
        }
        if (watchExpire == null || watchExpire.isZero() || watchExpire.isNegative() || cacheExpire == null
                || cacheExpire.isZero() || cacheExpire.isNegative()) {
            throw new IllegalArgumentException("Cortex expiry values must be greater than zero");
        }
        this.enabled = enabled;
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.autoRegister = autoRegister;
        this.settingEnabled = settingEnabled;
        this.serverEnabled = serverEnabled;
        this.settingGroup = settingGroup;
        this.settingDataId = settingDataId;
        this.maxSettingVersions = maxSettingVersions;
        this.maxWatchesPerNamespace = maxWatchesPerNamespace;
        this.watchExpire = watchExpire;
        this.cacheExpire = cacheExpire;
        this.cache = cache;
        this.watch = watch == null ? new Watch(false) : watch;
        this.bridge = bridge == null ? new Bridge("pull-only", Normal.EMPTY, 3, Normal.EMPTY) : bridge;
        this.guard = guard == null ? new Guard(true, new Token()) : guard;
        this.audit = audit == null ? new Audit(false) : audit;
        this.version = version == null ? new Version(false) : version;
    }

    /**
     * Validates the server addr.
     *
     * @return required trimmed server address
     */
    public String requireServerAddr() {
        if (StringKit.isBlank(serverAddr)) {
            throw new IllegalStateException("bus.cortex.server-addr is required");
        }
        return serverAddr.trim();
    }

    /**
     * Validates the namespace.
     *
     * @return configured nonblank namespace
     */
    public String requireNamespace() {
        if (StringKit.isBlank(namespace)) {
            throw new IllegalStateException("bus.cortex.namespace is required");
        }
        return namespace.trim();
    }

    /**
     * Validates the max setting versions.
     *
     * @return retained setting revision count
     */
    public int requireMaxSettingVersions() {
        return maxSettingVersions;
    }

    /**
     * Validates the max watches per namespace.
     *
     * @return watch registration limit
     */
    public int requireMaxWatchesPerNamespace() {
        return maxWatchesPerNamespace;
    }

    /**
     * Validates the watch expire ms.
     *
     * @return watch expiry in milliseconds
     */
    public long requireWatchExpireMs() {
        return watchExpire.toMillis();
    }

    /**
     * Validates the cache expire ms.
     *
     * @return cache expiry in milliseconds
     */
    public long requireCacheExpireMs() {
        return cacheExpire.toMillis();
    }

    /**
     * Watch integration options.
     *
     * @param loggingEnabled logging enabled
     */
    public record Watch(boolean loggingEnabled) {

        /**
         * Creates watch defaults.
         */
        public Watch() {
            this(false);
        }
    }

    /**
     * Remote bridge options.
     *
     * @param mode       configured operating mode
     * @param url        service endpoint URL
     * @param maxRetries max retries
     * @param source     source schema settings
     */
    public record Bridge(String mode, String url, int maxRetries, String source) {

        /**
         * Creates and validates bridge options.
         */
        public Bridge {
            mode = StringKit.isBlank(mode) ? "pull-only" : mode.trim();
            url = url == null ? Normal.EMPTY : url.trim();
            source = source == null ? Normal.EMPTY : source.trim();
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("bus.cortex.bridge.max-retries must be greater than zero");
            }
            if ("push+pull".equalsIgnoreCase(mode) && StringKit.isBlank(url)) {
                throw new IllegalArgumentException("bus.cortex.bridge.url is required for push+pull mode");
            }
        }

        /**
         * Indicates whether bidirectional push and pull synchronization is enabled for this bridge.
         *
         * @return whether push and pull synchronization is enabled
         */
        public boolean isPushPullEnabled() {
            return "push+pull".equalsIgnoreCase(mode);
        }

        /**
         * Validates the url.
         *
         * @return required bridge URL
         */
        public String requireUrl() {
            return url;
        }

        /**
         * Validates the max retries.
         *
         * @return validated retry count
         */
        public int requireMaxRetries() {
            return maxRetries;
        }

        /**
         * Resolves the source.
         *
         * @return normalized source marker
         */
        public String resolveSource() {
            return source;
        }
    }

    /**
     * Guard integration options.
     *
     * @param enabled whether the feature is enabled
     * @param token   guard token
     */
    public record Guard(boolean enabled, Token token) {

        /**
         * Creates guard defaults.
         */
        public Guard() {
            this(true, new Token());
        }

        /**
         * Returns the token.
         *
         * @return token guard configuration
         */
        public Token getToken() {
            return token;
        }
    }

    /**
     * Token guard binding type retained for the Cortex guard contract.
     */
    public static final class Token extends TokenGuardConfig {

        /**
         * Creates token guard defaults.
         */
        public Token() {
            super();
        }
    }

    /**
     * Audit integration options.
     *
     * @param enabled whether the feature is enabled
     */
    public record Audit(boolean enabled) {

        /**
         * Creates audit defaults.
         */
        public Audit() {
            this(false);
        }
    }

    /**
     * Version registry options.
     *
     * @param enabled whether the feature is enabled
     */
    public record Version(boolean enabled) {

        /**
         * Creates version defaults.
         */
        public Version() {
            this(false);
        }
    }

    /**
     * @return safe diagnostic text
     */
    @Override
    public String toString() {
        return "CortexProperties[enabled=" + enabled + ", namespace=" + namespace + ", serverEnabled=" + serverEnabled
                + ", maxSettingVersions=" + maxSettingVersions + ", maxWatchesPerNamespace=" + maxWatchesPerNamespace
                + ", watchExpire=" + watchExpire + ", cacheExpire=" + cacheExpire + ", guard=***]";
    }

}
