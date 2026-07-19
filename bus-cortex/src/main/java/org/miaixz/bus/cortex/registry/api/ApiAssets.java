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

import java.util.Map;

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.builtin.RegistryGenerator;
import org.miaixz.bus.cortex.registry.MetadataCodec;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

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
    private static final int DEFAULT_LEASE = 30;

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
        super.metadata(this.meta.merge(getMetadata()));
    }

    /**
     * Applies API-registry metadata defaults.
     *
     * @return this asset
     */
    public ApiAssets normalizeMeta() {
        return normalizeMeta(RegistryGenerator.INSTANCE);
    }

    /**
     * Applies API-registry metadata defaults using the supplied keying strategy.
     *
     * @param keying route-key strategy
     * @return this asset
     */
    public ApiAssets normalizeMeta(Keying<Keying.RegistrySpec> keying) {
        Keying<Keying.RegistrySpec> effective = keying == null ? RegistryGenerator.INSTANCE : keying;
        Meta meta = meta();
        meta.setKey(defaultKey(effective));
        if (meta.getLease() == null) {
            meta.setLease(DEFAULT_LEASE);
        }
        if (meta.getBeat() == null) {
            meta.setBeat(Builder.DEFAULT_HEALTH_INTERVAL_MS);
        }
        meta(meta);
        return this;
    }

    /**
     * Returns beat interval from API metadata.
     *
     * @return beat interval in milliseconds
     */
    public Long beat() {
        return meta().getBeat();
    }

    /**
     * Stores beat interval into API metadata.
     *
     * @param beat beat interval in milliseconds
     */
    public void beat(Long beat) {
        Meta meta = meta();
        meta.setBeat(beat);
        meta(meta);
    }

    /**
     * Returns service lease duration from API metadata.
     *
     * @return lease duration in seconds
     */
    public Integer lease() {
        return meta().getLease();
    }

    /**
     * Stores service lease duration into API metadata.
     *
     * @param lease lease duration in seconds
     */
    public void lease(Integer lease) {
        Meta meta = meta();
        meta.setLease(lease);
        meta(meta);
    }

    /**
     * Returns registry key from API metadata.
     * <p>
     * The built-in format uses {@code method:version:verbCode}, for example {@code dp.license.get:1.0:1}.
     * </p>
     *
     * @return registry key
     */
    public String key() {
        return meta().getKey();
    }

    /**
     * Stores registry key into API metadata.
     * <p>
     * The built-in default key format uses the numeric registry verb code rather than the HTTP token string.
     * </p>
     *
     * @param key registry key
     */
    public void key(String key) {
        Meta meta = meta();
        meta.setKey(key);
        meta(meta);
    }

    /**
     * Builds the default public key from method, version, and numeric verb code.
     * <p>
     * This metadata field intentionally keeps the lightweight public alias semantics. Runtime lookup candidates are now
     * generated independently by the registry-side {@link Keying#keys(Object)} candidate chain.
     * </p>
     *
     * @return default key or {@code null}
     */
    private String defaultKey(Keying<Keying.RegistrySpec> keying) {
        return keying.key(Keying.RegistrySpec.route(null, null, null, getMethod(), getVersion(), getVerb()));
    }

    /**
     * API-specific metadata payload stored directly in the raw asset metadata JSON payload.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Meta {

        /**
         * Metadata key storing the generated registry key.
         */
        private static final String KEY = "key";

        /**
         * Metadata key storing the beat interval.
         */
        private static final String BEAT = "beat";

        /**
         * Metadata key storing the service lease duration.
         */
        private static final String LEASE = "lease";
        
        /**
         * Stable public key used by admin and bridge paths, for example {@code dp.license.get:1.0:1}.
         */
        private String key;

        /**
         * Beat interval in milliseconds expected for runtime instances of this service.
         */
        private Long beat;

        /**
         * Lease timeout in seconds for runtime instances of this service.
         */
        private Integer lease;

        /**
         * Creates an empty API metadata view.
         */
        public Meta() {
            // No initialization required.
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
                Logger.debug(
                        false,
                        "Cortex",
                        "Cortex operation skipped: provider={}, recoverable={}, exception={}",
                        "ApiAssets",
                        true,
                        ignore.getClass().getSimpleName());
                return new Meta();
            }
        }

        /**
         * Converts this API metadata to JSON.
         *
         * @return metadata JSON
         */
        public String merge() {
            return merge(null);
        }

        /**
         * Converts this API metadata to JSON while preserving extension fields already present in the raw metadata.
         *
         * @param metadata existing raw metadata JSON
         * @return merged metadata JSON
         */
        public String merge(String metadata) {
            Map<String, Object> root = MetadataCodec.root(metadata);
            if (key == null) {
                root.remove(KEY);
            } else {
                root.put(KEY, key);
            }

            if (beat == null) {
                root.remove(BEAT);
            } else {
                root.put(BEAT, beat);
            }

            if (lease == null) {
                root.remove(LEASE);
            } else {
                root.put(LEASE, lease);
            }

            return MetadataCodec.json(root);
        }

    }

}
