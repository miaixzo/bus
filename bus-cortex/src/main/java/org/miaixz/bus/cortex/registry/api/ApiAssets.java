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

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
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
 */
@SuperBuilder
public class ApiAssets extends Assets {

    @Serial
    private static final long serialVersionUID = 2558885501596L;

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
        setMetadata(metadata);
    }

    /**
     * Stores raw metadata through the standard bean path and invalidates the parsed API metadata cache.
     *
     * @param metadata raw metadata JSON, or {@code null} to clear it
     */
    @Override
    public void setMetadata(String metadata) {
        super.setMetadata(metadata);
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
        Meta value = meta == null ? new Meta() : meta;
        setMetadata(value.merge(getMetadata()));
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
        meta(meta);
        return this;
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
     * @param keying route-key strategy
     * @return default key or {@code null}
     */
    private String defaultKey(Keying<Keying.RegistrySpec> keying) {
        return keying.key(
                Keying.RegistrySpec.route(getTenant_id(), null, getApp_id(), getMethod(), getVersion(), getVerb()));
    }

    /**
     * API-specific metadata payload stored directly in the raw asset metadata JSON payload.
     *
     * @author Kimi Liu
     */
    @Getter
    @Setter
    public static class Meta {

        /**
         * Metadata key storing the generated registry key.
         */
        private static final String KEY = "key";

        /**
         * Metadata members replaced on API writes, including removed system keys that must not survive a save.
         */
        private static final Set<String> FIELDS = Set.of(KEY, "beat", "lease");

        /**
         * Stable public key used by admin and bridge paths, for example {@code dp.license.get:1.0:1}.
         */
        private String key;

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
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(KEY, key);
            return MetadataCodec.merge(metadata, FIELDS, values);
        }

    }

}
