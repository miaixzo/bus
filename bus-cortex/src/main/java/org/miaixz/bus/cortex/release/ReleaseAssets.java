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
package org.miaixz.bus.cortex.release;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.MetadataCodec;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Versioned artifact definition.
 *
 * @author Kimi Liu
 */
public class ReleaseAssets extends Assets {

    /**
     * Version-specific metadata view.
     */
    @Transient
    private Meta meta;

    /**
     * Creates a ReleaseAssets with the default type set to VERSION.
     */
    public ReleaseAssets() {
        setType(Type.VERSION.key());
    }

    /**
     * Replaces the raw metadata payload and clears the parsed version metadata view.
     *
     * @param metadata raw metadata JSON
     */
    @Override
    public void metadata(String metadata) {
        setMetadata(metadata);
    }

    /**
     * Stores raw metadata through the standard bean path and invalidates the parsed version metadata cache.
     *
     * @param metadata raw metadata JSON, or {@code null} to clear it
     */
    @Override
    public void setMetadata(String metadata) {
        super.setMetadata(metadata);
        this.meta = null;
    }

    /**
     * Returns version-specific metadata parsed from the raw asset metadata JSON payload.
     *
     * @return version metadata
     */
    public Meta meta() {
        if (meta == null) {
            meta = Meta.from(getMetadata());
        }
        return meta;
    }

    /**
     * Replaces version-specific metadata and writes it back to the raw asset metadata JSON payload.
     *
     * @param meta version metadata
     */
    public void meta(Meta meta) {
        Meta value = meta == null ? new Meta() : meta;
        setMetadata(value.merge(getMetadata()));
    }

    /**
     * Writes the current version metadata view back to the raw asset metadata JSON payload.
     *
     * @return this asset
     */
    public ReleaseAssets normalizeMeta() {
        meta(meta());
        return this;
    }

    /**
     * Returns the semantic version string.
     *
     * @return semantic version string
     */
    public String semver() {
        return meta().getSemver();
    }

    /**
     * Stores the semantic version string into version metadata.
     *
     * @param semver semantic version string
     */
    public void semver(String semver) {
        Meta meta = meta();
        meta.setSemver(semver);
        meta(meta);
    }

    /**
     * Returns the human-readable changelog for this version.
     *
     * @return changelog text
     */
    public String changelog() {
        return meta().getChangelog();
    }

    /**
     * Stores the changelog text into version metadata.
     *
     * @param changelog changelog text
     */
    public void changelog(String changelog) {
        Meta meta = meta();
        meta.setChangelog(changelog);
        meta(meta);
    }

    /**
     * Returns artifact coordinates or a download URL for this version.
     *
     * @return artifact coordinates or URL
     */
    public String artifact() {
        return meta().getArtifact();
    }

    /**
     * Stores artifact coordinates or a download URL into version metadata.
     *
     * @param artifact artifact coordinates or URL
     */
    public void artifact(String artifact) {
        Meta meta = meta();
        meta.setArtifact(artifact);
        meta(meta);
    }

    /**
     * Returns the compatible API or schema range.
     *
     * @return compatible range expression
     */
    public String compatibleRange() {
        return meta().getCompatibleRange();
    }

    /**
     * Stores the compatible API or schema range into version metadata.
     *
     * @param compatibleRange compatible range expression
     */
    public void compatibleRange(String compatibleRange) {
        Meta meta = meta();
        meta.setCompatibleRange(compatibleRange);
        meta(meta);
    }

    /**
     * Returns the release timestamp in epoch milliseconds.
     *
     * @return release timestamp
     */
    public Long releasedAt() {
        return meta().getReleasedAt();
    }

    /**
     * Stores the release timestamp into version metadata.
     *
     * @param releasedAt release timestamp in epoch milliseconds
     */
    public void releasedAt(Long releasedAt) {
        Meta meta = meta();
        meta.setReleasedAt(releasedAt);
        meta(meta);
    }

    /**
     * Returns the support end timestamp in epoch milliseconds.
     *
     * @return support end timestamp
     */
    public Long supportUntil() {
        return meta().getSupportUntil();
    }

    /**
     * Stores the support end timestamp into version metadata.
     *
     * @param supportUntil support end timestamp in epoch milliseconds
     */
    public void supportUntil(Long supportUntil) {
        Meta meta = meta();
        meta.setSupportUntil(supportUntil);
        meta(meta);
    }

    /**
     * Version-specific metadata payload stored directly in the raw asset metadata JSON payload.
     *
     * @author Kimi Liu
     */
    @Getter
    @Setter
    public static class Meta {

        /**
         * Fixed metadata members owned by the version subtype, independent of incoming payloads.
         */
        private static final Set<String> FIELDS = Set
                .of("semver", "changelog", "artifact", "compatibleRange", "releasedAt", "supportUntil");

        /**
         * Semantic version string.
         */
        private String semver;

        /**
         * Human-readable changelog for this version.
         */
        private String changelog;

        /**
         * Artifact coordinates or download URL for this version.
         */
        private String artifact;

        /**
         * Optional compatible API or schema range.
         */
        private String compatibleRange;

        /**
         * Release timestamp in epoch milliseconds.
         */
        private Long releasedAt;

        /**
         * Support end timestamp in epoch milliseconds.
         */
        private Long supportUntil;

        /**
         * Creates an empty version metadata view.
         */
        public Meta() {
            // No initialization required.
        }

        /**
         * Parses version metadata from a raw metadata JSON payload.
         *
         * @param metadata raw metadata JSON
         * @return parsed version metadata
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
                        "ReleaseAssets",
                        true,
                        ignore.getClass().getSimpleName());
                return new Meta();
            }
        }

        /**
         * Converts this version metadata to JSON.
         *
         * @return metadata JSON
         */
        public String merge() {
            return merge(null);
        }

        /**
         * Writes the complete version projection while preserving metadata fields owned by other extensions.
         *
         * @param metadata existing raw metadata JSON
         * @return merged metadata JSON
         * @throws IllegalArgumentException if existing nonblank metadata is not a valid JSON object
         */
        public String merge(String metadata) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("semver", semver);
            values.put("changelog", changelog);
            values.put("artifact", artifact);
            values.put("compatibleRange", compatibleRange);
            values.put("releasedAt", releasedAt);
            values.put("supportUntil", supportUntil);
            return MetadataCodec.merge(metadata, FIELDS, values);
        }

    }

}
