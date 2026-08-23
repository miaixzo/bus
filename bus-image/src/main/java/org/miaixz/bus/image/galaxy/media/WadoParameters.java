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
package org.miaixz.bus.image.galaxy.media;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import org.miaixz.bus.core.lang.Normal;

/**
 * WADO-URI/WADO-RS access parameters for manifest query results.
 *
 * @author Kimi Liu
 */
public class WadoParameters extends ArchiveParameters {

    /**
     * The tag wado query value.
     */
    public static final String TAG_WADO_QUERY = "wado_query";

    /**
     * The wado url value.
     */
    public static final String WADO_URL = "wadoURL";

    /**
     * The wado only sop uid value.
     */
    public static final String WADO_ONLY_SOP_UID = "requireOnlySOPInstanceUID";

    /**
     * The require only sop instance uid value.
     */
    private final boolean requireOnlySOPInstanceUID;

    /**
     * The wado rs value.
     */
    private final boolean wadoRS;

    /**
     * The query mode value.
     */
    private final QueryMode queryMode;

    /**
     * Creates a new instance.
     *
     * @param archiveID                 the archive id.
     * @param wadoURL                   the wado url.
     * @param requireOnlySOPInstanceUID the require only sop instance uid.
     * @param additionalParameters      the additional parameters.
     * @param overrideDicomTagsList     the override dicom tags list.
     * @param webLogin                  the web login.
     * @param wadoRS                    the wado rs.
     */
    public WadoParameters(String archiveID, String wadoURL, boolean requireOnlySOPInstanceUID,
            String additionalParameters, String overrideDicomTagsList, String webLogin, boolean wadoRS) {
        this(archiveID, wadoURL, requireOnlySOPInstanceUID, additionalParameters, overrideDicomTagsList, webLogin,
                wadoRS, QueryMode.DEFAULT);
    }

    /**
     * Creates a new instance.
     *
     * @param archiveID                 the archive id.
     * @param wadoURL                   the wado url.
     * @param requireOnlySOPInstanceUID the require only sop instance uid.
     * @param additionalParameters      the additional parameters.
     * @param overrideDicomTagsList     the override dicom tags list.
     * @param webLogin                  the web login.
     * @param wadoRS                    the wado rs.
     * @param queryMode                 the query mode.
     */
    public WadoParameters(String archiveID, String wadoURL, boolean requireOnlySOPInstanceUID,
            String additionalParameters, String overrideDicomTagsList, String webLogin, boolean wadoRS,
            QueryMode queryMode) {
        super(Objects.requireNonNull(archiveID, "Archive ID cannot be null"),
                Objects.requireNonNullElse(validateAndNormalizeUrl(wadoURL), Normal.EMPTY), additionalParameters,
                overrideDicomTagsList, webLogin);
        this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
        this.wadoRS = wadoRS;
        this.queryMode = Objects.requireNonNullElse(queryMode, QueryMode.DEFAULT);
    }

    /**
     * Creates a new instance.
     *
     * @param wadoURL                   the wado url.
     * @param requireOnlySOPInstanceUID the require only sop instance uid.
     */
    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID) {
        this(Normal.EMPTY, wadoURL, requireOnlySOPInstanceUID, null, null, null, false);
    }

    /**
     * Creates a new instance.
     *
     * @param wadoURL                   the wado url.
     * @param requireOnlySOPInstanceUID the require only sop instance uid.
     * @param wadoRS                    the wado rs.
     */
    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID, boolean wadoRS) {
        this(Normal.EMPTY, wadoURL, requireOnlySOPInstanceUID, null, null, null, wadoRS);
    }

    /**
     * Executes the wado uri operation.
     *
     * @param wadoURL                   the wado url.
     * @param requireOnlySOPInstanceUID the require only sop instance uid.
     * @return the operation result.
     */
    public static WadoParameters wadoUri(String wadoURL, boolean requireOnlySOPInstanceUID) {
        return new WadoParameters(wadoURL, requireOnlySOPInstanceUID, false);
    }

    /**
     * Executes the wado rs operation.
     *
     * @param wadoURL the wado url.
     * @return the operation result.
     */
    public static Builder wadoRs(String wadoURL) {
        return new Builder(wadoURL, true);
    }

    /**
     * Builds the er.
     *
     * @param wadoURL the wado url.
     * @return the operation result.
     */
    public static Builder builder(String wadoURL) {
        return new Builder(wadoURL, false);
    }

    /**
     * Determines whether require only sop instance uid.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isRequireOnlySOPInstanceUID() {
        return requireOnlySOPInstanceUID;
    }

    /**
     * Determines whether wado rs.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isWadoRS() {
        return wadoRS;
    }

    /**
     * Gets the protocol name.
     *
     * @return the protocol name.
     */
    public String getProtocolName() {
        return wadoRS ? "WADO-RS" : "WADO-URI";
    }

    /**
     * Gets the query mode.
     *
     * @return the query mode.
     */
    public QueryMode getQueryMode() {
        return queryMode;
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        return this == object || (object instanceof WadoParameters other && super.equals(other)
                && requireOnlySOPInstanceUID == other.requireOnlySOPInstanceUID && wadoRS == other.wadoRS
                && queryMode == other.queryMode);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requireOnlySOPInstanceUID, wadoRS, queryMode);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "WadoParameters{" + "archiveID='" + getArchiveID() + '\'' + ", wadoURL='" + getBaseURL() + '\''
                + ", protocol='" + getProtocolName() + '\'' + ", queryMode=" + queryMode
                + ", requireOnlySOPInstanceUID=" + requireOnlySOPInstanceUID + ", httpTagCount=" + getHttpTags().size()
                + '}';
    }

    /**
     * Validates the and normalize url.
     *
     * @param wadoURL the wado url.
     * @return the operation result.
     */
    private static String validateAndNormalizeUrl(String wadoURL) {
        Objects.requireNonNull(wadoURL, "WADO URL cannot be null");
        if (wadoURL.isBlank()) {
            return Normal.EMPTY;
        }
        try {
            return new URI(wadoURL.trim()).toString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid WADO URI format: " + wadoURL, exception);
        }
    }

    /**
     * Represents the Builder type.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * The archive id value.
         */
        private String archiveID = Normal.EMPTY;

        /**
         * The wado url value.
         */
        private final String wadoURL;

        /**
         * The require only sop instance uid value.
         */
        private boolean requireOnlySOPInstanceUID;

        /**
         * The additional parameters value.
         */
        private String additionalParameters;

        /**
         * The override dicom tags list value.
         */
        private String overrideDicomTagsList;

        /**
         * The web login value.
         */
        private String webLogin;

        /**
         * The wado rs value.
         */
        private final boolean wadoRS;

        /**
         * The query mode value.
         */
        private QueryMode queryMode = QueryMode.DEFAULT;

        /**
         * Creates a new instance.
         *
         * @param wadoURL the wado url.
         * @param wadoRS  the wado rs.
         */
        public Builder(String wadoURL, boolean wadoRS) {
            this.wadoURL = wadoURL;
            this.wadoRS = wadoRS;
        }

        /**
         * Creates a copy configured with the archive id.
         *
         * @param archiveID the archive id.
         * @return the operation result.
         */
        public Builder withArchiveID(String archiveID) {
            this.archiveID = archiveID == null ? Normal.EMPTY : archiveID;
            return this;
        }

        /**
         * Creates a copy configured with the require only sop instance uid.
         *
         * @param requireOnlySOPInstanceUID the require only sop instance uid.
         * @return the operation result.
         */
        public Builder withRequireOnlySOPInstanceUID(boolean requireOnlySOPInstanceUID) {
            this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
            return this;
        }

        /**
         * Creates a copy configured with the additional parameters.
         *
         * @param additionalParameters the additional parameters.
         * @return the operation result.
         */
        public Builder withAdditionalParameters(String additionalParameters) {
            this.additionalParameters = additionalParameters;
            return this;
        }

        /**
         * Creates a copy configured with the override dicom tags list.
         *
         * @param overrideDicomTagsList the override dicom tags list.
         * @return the operation result.
         */
        public Builder withOverrideDicomTagsList(String overrideDicomTagsList) {
            this.overrideDicomTagsList = overrideDicomTagsList;
            return this;
        }

        /**
         * Creates a copy configured with the web login.
         *
         * @param webLogin the web login.
         * @return the operation result.
         */
        public Builder withWebLogin(String webLogin) {
            this.webLogin = webLogin;
            return this;
        }

        /**
         * Creates a copy configured with the query mode.
         *
         * @param queryMode the query mode.
         * @return the operation result.
         */
        public Builder withQueryMode(QueryMode queryMode) {
            this.queryMode = queryMode == null ? QueryMode.DEFAULT : queryMode;
            return this;
        }

        /**
         * Executes the build operation.
         *
         * @return the operation result.
         */
        public WadoParameters build() {
            return new WadoParameters(archiveID, wadoURL, requireOnlySOPInstanceUID, additionalParameters,
                    overrideDicomTagsList, webLogin, wadoRS, queryMode);
        }

    }

}
