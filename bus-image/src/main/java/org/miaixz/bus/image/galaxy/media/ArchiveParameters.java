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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Archive access parameters for DICOM manifest and web retrieval workflows.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ArchiveParameters {

    /**
     * The tag document root value.
     */
    public static final String TAG_DOCUMENT_ROOT = "manifest";

    /**
     * The manifest uid value.
     */
    public static final String MANIFEST_UID = "uid";

    /**
     * The tag arc query value.
     */
    public static final String TAG_ARC_QUERY = "arcQuery";

    /**
     * The archive id value.
     */
    public static final String ARCHIVE_ID = "arcId";

    /**
     * The base url value.
     */
    public static final String BASE_URL = "baseUrl";

    /**
     * The tag http tag value.
     */
    public static final String TAG_HTTP_TAG = "httpTag";

    /**
     * The additional parameters value.
     */
    public static final String ADDITIONAL_PARAMETERS = "additionalParameters";

    /**
     * The override tags value.
     */
    public static final String OVERRIDE_TAGS = "overrideDicomTagsList";

    /**
     * The web login value.
     */
    public static final String WEB_LOGIN = "webLogin";

    /**
     * The tag delimiter value.
     */
    public static final String TAG_DELIMITER = ",";

    /**
     * The base url value.
     */
    private final String baseURL;

    /**
     * The archive id value.
     */
    private final String archiveID;

    /**
     * The additional parameters value.
     */
    private final String additionalParameters;

    /**
     * The override dicom tag id list value.
     */
    private final int[] overrideDicomTagIDList;

    /**
     * The web login value.
     */
    private final String webLogin;

    /**
     * The http tags value.
     */
    private final List<HttpTag> httpTags;

    /**
     * Creates a new instance.
     *
     * @param archiveID             the archive id.
     * @param baseURL               the base url.
     * @param additionalParameters  the additional parameters.
     * @param overrideDicomTagsList the override dicom tags list.
     * @param webLogin              the web login.
     */
    public ArchiveParameters(String archiveID, String baseURL, String additionalParameters,
            String overrideDicomTagsList, String webLogin) {
        this.archiveID = Objects.requireNonNull(archiveID, "Archive ID cannot be null");
        this.baseURL = Objects.requireNonNull(baseURL, "Base URL cannot be null");
        this.webLogin = webLogin == null ? null : webLogin.trim();
        this.additionalParameters = hasText(additionalParameters) ? additionalParameters : "";
        this.httpTags = new ArrayList<>(2);
        this.overrideDicomTagIDList = parseOverrideTags(overrideDicomTagsList);
    }

    /**
     * Gets the http tags.
     *
     * @return the http tags.
     */
    public List<HttpTag> getHttpTags() {
        return Collections.unmodifiableList(httpTags);
    }

    /**
     * Gets the http taglist.
     *
     * @return the http taglist.
     */
    public List<HttpTag> getHttpTaglist() {
        return getHttpTags();
    }

    /**
     * Adds the http tag.
     *
     * @param key   the key.
     * @param value the value.
     */
    public void addHttpTag(String key, String value) {
        if (key != null && value != null) {
            httpTags.add(new HttpTag(key, value));
        }
    }

    /**
     * Gets the archive id.
     *
     * @return the archive id.
     */
    public String getArchiveID() {
        return archiveID;
    }

    /**
     * Gets the base url.
     *
     * @return the base url.
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Gets the web login.
     *
     * @return the web login.
     */
    public String getWebLogin() {
        return webLogin;
    }

    /**
     * Gets the additional parameters.
     *
     * @return the additional parameters.
     */
    public String getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * Gets the override dicom tag id list.
     *
     * @return the override dicom tag id list.
     */
    public int[] getOverrideDicomTagIDList() {
        return overrideDicomTagIDList == null ? null : overrideDicomTagIDList.clone();
    }

    /**
     * Gets the override dicom tags list.
     *
     * @return the override dicom tags list.
     */
    public String getOverrideDicomTagsList() {
        return overrideDicomTagIDList == null ? null
                : IntStream.of(overrideDicomTagIDList).mapToObj(String::valueOf)
                        .collect(Collectors.joining(TAG_DELIMITER));
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        return this == object || (object instanceof ArchiveParameters other && Objects.equals(baseURL, other.baseURL)
                && Objects.equals(archiveID, other.archiveID)
                && Objects.equals(additionalParameters, other.additionalParameters)
                && Arrays.equals(overrideDicomTagIDList, other.overrideDicomTagIDList)
                && Objects.equals(webLogin, other.webLogin) && Objects.equals(httpTags, other.httpTags));
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                baseURL,
                archiveID,
                additionalParameters,
                Arrays.hashCode(overrideDicomTagIDList),
                webLogin,
                httpTags);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "ArchiveParameters{" + "archiveID='" + archiveID + '\'' + ", baseURL='" + baseURL + '\'' + ", webLogin='"
                + webLogin + '\'' + ", additionalParameters='" + additionalParameters + '\'' + ", overrideTags="
                + Arrays.toString(overrideDicomTagIDList) + ", httpTagCount=" + httpTags.size() + '}';
    }

    /**
     * Parses the override tags.
     *
     * @param overrideDicomTagsList the override dicom tags list.
     * @return the operation result.
     */
    private static int[] parseOverrideTags(String overrideDicomTagsList) {
        if (!hasText(overrideDicomTagsList)) {
            return null;
        }
        return Arrays.stream(overrideDicomTagsList.split(TAG_DELIMITER)).map(String::trim)
                .mapToInt(ArchiveParameters::parseTagId).filter(tagId -> tagId != -1).toArray();
    }

    /**
     * Parses the tag id.
     *
     * @param tagString the tag string.
     * @return the operation result.
     */
    private static int parseTagId(String tagString) {
        try {
            return Integer.decode(tagString);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    /**
     * Determines whether text.
     *
     * @param value the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
