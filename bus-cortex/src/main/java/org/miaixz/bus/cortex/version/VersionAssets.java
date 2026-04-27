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
package org.miaixz.bus.cortex.version;

import jakarta.persistence.Transient;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.extra.json.JsonKit;

import lombok.Getter;
import lombok.Setter;

/**
 * Versioned artifact definition.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VersionAssets extends Assets {

    /**
     * Version-specific metadata view.
     */
    @Transient
    private Meta meta;

    /**
     * Creates a VersionAssets with the default type set to VERSION.
     */
    public VersionAssets() {
        setType(Type.VERSION.key());
    }

    /**
     * Replaces the raw metadata payload and clears the parsed version metadata view.
     *
     * @param metadata raw metadata JSON
     */
    @Override
    public void metadata(String metadata) {
        super.metadata(metadata);
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
        this.meta = meta == null ? new Meta() : meta;
        super.metadata(this.meta.merge());
    }

    /**
     * Writes the current version metadata view back to the raw asset metadata JSON payload.
     *
     * @return this asset
     */
    public VersionAssets normalizeMeta() {
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
     * Returns the current release state of this version.
     *
     * @return version status
     */
    public VersionStatus versionStatus() {
        return meta().getVersionStatus();
    }

    /**
     * Stores the current release state into version metadata.
     *
     * @param versionStatus version status
     */
    public void versionStatus(VersionStatus versionStatus) {
        Meta meta = meta();
        meta.setVersionStatus(versionStatus);
        meta(meta);
    }

    /**
     * Version-specific metadata payload stored directly in the raw asset metadata JSON payload.
     */
    @Getter
    @Setter
    public static class Meta {

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
         * Current release state of this version.
         */
        private VersionStatus versionStatus;

        /**
         * Creates an empty version metadata view.
         */
        public Meta() {
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
                return new Meta();
            }
        }

        /**
         * Converts this version metadata to JSON.
         *
         * @return metadata JSON
         */
        public String merge() {
            return JsonKit.toJsonString(this);
        }

    }

}
