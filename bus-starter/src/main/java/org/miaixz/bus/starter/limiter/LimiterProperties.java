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
package org.miaixz.bus.starter.limiter;

import java.time.Duration;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable limiter starter properties, isolated from the mutable core context.
 *
 * @author Kimi Liu
 */
@Getter
@Validated
@ConfigurationProperties(GeniusBuilder.LIMITER)
public class LimiterProperties {

    /**
     * Whether the limiter integration is enabled.
     */
    private final boolean enabled;
    /**
     * Retention time for hotspot limiter decisions.
     */
    private final Duration hotspotCacheDuration;
    /**
     * Whether rejected limiter decisions are written to the log.
     */
    private final boolean logger;
    /**
     * Supplier implementation used to obtain limiter resources.
     */
    private final String supplierClass;
    /**
     * Extension settings passed to the selected limiter implementation.
     */
    private final String extension;

    /**
     * Creates validated limiter properties.
     *
     * @param enabled              whether limiter integration is enabled
     * @param hotspotCacheDuration hotspot result cache duration
     * @param logger               whether limiter logging is enabled
     * @param supplierClass        custom user-marker supplier class
     * @param extension            optional extension configuration
     */
    public LimiterProperties(@DefaultValue("false") boolean enabled, @DefaultValue("60s") Duration hotspotCacheDuration,
            @DefaultValue(Normal.TRUE) boolean logger, @DefaultValue(Normal.EMPTY) String supplierClass,
            String extension) {
        if (hotspotCacheDuration == null || hotspotCacheDuration.isZero() || hotspotCacheDuration.isNegative()
                || hotspotCacheDuration.toSeconds() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("bus.limiter.hotspot-cache-duration must be a positive whole duration");
        }
        this.enabled = enabled;
        this.hotspotCacheDuration = hotspotCacheDuration;
        this.logger = logger;
        this.supplierClass = supplierClass == null ? Normal.EMPTY : supplierClass.trim();
        this.extension = extension;
    }

}
