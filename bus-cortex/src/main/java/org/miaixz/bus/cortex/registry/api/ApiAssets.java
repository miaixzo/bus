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
package org.miaixz.bus.cortex.registry.api;

import jakarta.persistence.Transient;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

import lombok.Getter;
import lombok.Setter;

/**
 * API service assets persisted in registry.
 * <p>
 * API-specific runtime hints are stored in the base asset metadata JSON payload instead of dedicated model fields.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@SuperBuilder
public class ApiAssets extends Assets {

    /**
     * Default service lease duration used when API metadata does not define one.
     */
    private static final int DEFAULT_LEASE_SECONDS = 30;

    /**
     * API-specific metadata view.
     */
    @Transient
    private Meta meta;

    /**
     * Creates an API service entry with type preset to {@link Type#API}.
     */
    public ApiAssets() {
        setType(Type.API.key());
    }

    /**
     * Replaces the raw metadata payload and clears the parsed API metadata view.
     *
     * @param metadata raw metadata JSON
     */
    @Override
    public void metadata(String metadata) {
        super.metadata(metadata);
        this.meta = null;
    }

    /**
     * Returns API-specific metadata parsed from the raw asset metadata JSON payload.
     *
     * @return API metadata
     */
    public Meta meta() {
        if (meta == null) {
            meta = Meta.from(getMetadata());
        }
        return meta;
    }

    /**
     * Replaces API-specific metadata and writes it back to the raw asset metadata JSON payload.
     *
     * @param meta API metadata
     */
    public void meta(Meta meta) {
        this.meta = meta == null ? new Meta() : meta;
        super.metadata(this.meta.merge());
    }

    /**
     * Applies API-registry metadata defaults.
     *
     * @return this asset
     */
    public ApiAssets normalizeMeta() {
        Meta meta = meta();
        meta.setRouteKey(defaultRouteKey());
        if (meta.getLeaseSeconds() == null) {
            meta.setLeaseSeconds(DEFAULT_LEASE_SECONDS);
        }
        if (meta.getHeartbeatIntervalMs() == null) {
            meta.setHeartbeatIntervalMs(Builder.DEFAULT_HEALTH_INTERVAL_MS);
        }
        meta(meta);
        return this;
    }

    /**
     * Returns heartbeat interval from API metadata.
     *
     * @return heartbeat interval in milliseconds
     */
    public Long heartbeatIntervalMs() {
        return meta().getHeartbeatIntervalMs();
    }

    /**
     * Stores heartbeat interval into API metadata.
     *
     * @param heartbeatIntervalMs heartbeat interval in milliseconds
     */
    public void heartbeatIntervalMs(Long heartbeatIntervalMs) {
        Meta meta = meta();
        meta.setHeartbeatIntervalMs(heartbeatIntervalMs);
        meta(meta);
    }

    /**
     * Returns service lease duration from API metadata.
     *
     * @return lease duration in seconds
     */
    public Integer leaseSeconds() {
        return meta().getLeaseSeconds();
    }

    /**
     * Stores service lease duration into API metadata.
     *
     * @param leaseSeconds lease duration in seconds
     */
    public void leaseSeconds(Integer leaseSeconds) {
        Meta meta = meta();
        meta.setLeaseSeconds(leaseSeconds);
        meta(meta);
    }

    /**
     * Returns route key from API metadata.
     *
     * @return route key
     */
    public String routeKey() {
        return meta().getRouteKey();
    }

    /**
     * Stores route key into API metadata.
     *
     * @param routeKey route key
     */
    public void routeKey(String routeKey) {
        Meta meta = meta();
        meta.setRouteKey(routeKey);
        meta(meta);
    }

    /**
     * Builds the default route key from method and version.
     *
     * @return default route key or {@code null}
     */
    private String defaultRouteKey() {
        if (getMethod() == null || getVersion() == null) {
            return null;
        }
        return getMethod() + ":" + getVersion();
    }

    /**
     * API-specific metadata payload stored directly in the raw asset metadata JSON payload.
     */
    @Getter
    @Setter
    public static class Meta {

        /**
         * Heartbeat interval in milliseconds expected for runtime instances of this service.
         */
        private Long heartbeatIntervalMs;
        /**
         * Lease timeout in seconds for runtime instances of this service.
         */
        private Integer leaseSeconds;
        /**
         * Stable route key used by admin and bridge paths.
         */
        private String routeKey;

        /**
         * Creates an empty API metadata view.
         */
        public Meta() {
        }

        /**
         * Parses API metadata from a raw metadata JSON payload.
         *
         * @param metadata raw metadata JSON
         * @return parsed API metadata
         */
        public static Meta from(String metadata) {
            if (StringKit.isBlank(metadata)) {
                return new Meta();
            }
            try {
                Meta meta = JsonKit.toPojo(metadata, Meta.class);
                return meta == null ? new Meta() : meta;
            } catch (Exception ignore) {
                return new Meta();
            }
        }

        /**
         * Converts this API metadata to JSON.
         *
         * @return metadata JSON
         */
        public String merge() {
            return JsonKit.toJsonString(this);
        }

    }

}
