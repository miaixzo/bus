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
import org.springframework.boot.context.properties.bind.ConstructorBinding;
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
 */
@Getter
@Validated
@ConfigurationProperties(GeniusBuilder.CORTEX)
public class CortexProperties {

    /**
     * Whether the cortex integration is enabled.
     */
    private final boolean enabled;

    /**
     * Maximum active watches permitted within one tenantId.
     */
    private final int maxWatchesPerTenant;

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
     * Optional host access checks for Cortex operations; disabling them preserves business validation.
     */
    private final Guard guard;

    /**
     * Audit event collection and retention settings.
     */
    private final Audit audit;

    /**
     * Read-only formal release registry settings.
     */
    private final Release release;

    /**
     * Creates Cortex properties after validating cache expiry, watch limits and nested integration options.
     *
     * @param enabled             whether Cortex integration is enabled
     * @param maxWatchesPerTenant watch registration limit per tenantId
     * @param watchExpire         watch registration expiry
     * @param cacheExpire         default Cortex cache expiry
     * @param cache               optional cache backend options
     * @param watch               watch options
     * @param bridge              bridge options
     * @param guard               guard options
     * @param audit               audit options
     * @param release             release-registry options
     */
    public CortexProperties(@DefaultValue(Normal.FALSE) boolean enabled, @DefaultValue("1000") int maxWatchesPerTenant,
            @DefaultValue("24h") Duration watchExpire, @DefaultValue("1h") Duration cacheExpire, Options cache,
            @DefaultValue Watch watch, @DefaultValue Bridge bridge, @DefaultValue Guard guard,
            @DefaultValue Audit audit, @DefaultValue Release release) {
        if (maxWatchesPerTenant <= 0) {
            throw new IllegalArgumentException("Cortex watch limits must be greater than zero");
        }
        if (watchExpire == null || watchExpire.isZero() || watchExpire.isNegative() || cacheExpire == null
                || cacheExpire.isZero() || cacheExpire.isNegative()) {
            throw new IllegalArgumentException("Cortex expiry values must be greater than zero");
        }
        this.enabled = enabled;
        this.maxWatchesPerTenant = maxWatchesPerTenant;
        this.watchExpire = watchExpire;
        this.cacheExpire = cacheExpire;
        this.cache = cache;
        this.watch = watch == null ? new Watch(false) : watch;
        this.bridge = bridge == null ? new Bridge("pull-only", Normal.EMPTY, 3, Normal.EMPTY) : bridge;
        this.guard = guard == null ? new Guard(true, new Token()) : guard;
        this.audit = audit == null ? new Audit(false) : audit;
        this.release = release == null ? new Release(false) : release;
    }

    /**
     * Validates the max watches per tenantId.
     *
     * @return watch registration limit
     */
    public int requireMaxWatchesPerTenant() {
        return maxWatchesPerTenant;
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
     * @return safe diagnostic text
     */
    @Override
    public String toString() {
        return "CortexProperties[enabled=" + enabled + ", maxWatchesPerTenant=" + maxWatchesPerTenant + ", watchExpire="
                + watchExpire + ", cacheExpire=" + cacheExpire + ", guard=***]";
    }

    /**
     * Watch integration options.
     *
     * @param loggingEnabled logging enabled
     * @author Kimi Liu
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
     * @author Kimi Liu
     */
    public record Bridge(@DefaultValue("pull-only") String mode, @DefaultValue(Normal.EMPTY) String url,
            @DefaultValue("3") int maxRetries, @DefaultValue(Normal.EMPTY) String source) {

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
     * @author Kimi Liu
     */
    public record Guard(@DefaultValue("true") boolean enabled, @DefaultValue Token token) {

        /**
         * Binds the canonical guard options even when the convenience constructor is present.
         *
         * @param enabled whether optional access checks are enabled
         * @param token   credential pre-validation options, or null to use defaults
         */
        @ConstructorBinding
        public Guard {
            token = token == null ? new Token() : token;
        }

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
     *
     * @author Kimi Liu
     */
    public static class Token extends TokenGuardConfig {

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
     * @author Kimi Liu
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
     * Release registry options.
     *
     * @param enabled whether the feature is enabled
     * @author Kimi Liu
     */
    public record Release(boolean enabled) {

        /**
         * Binds the release registry switch through the canonical record constructor.
         *
         * @param enabled whether release registry integration is enabled
         */
        @ConstructorBinding
        public Release(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Creates release defaults.
         */
        public Release() {
            this(false);
        }

    }

}
