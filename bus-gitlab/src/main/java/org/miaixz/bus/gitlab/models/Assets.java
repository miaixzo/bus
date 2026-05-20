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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.models.Constants.ArchiveFormat;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * This class is part of the Release class model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Assets implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852237093233L;

    private Integer count;
    private List<Source> sources;
    private List<Link> links;
    private String evidenceFilePath;

    /**
     * Returns the count.
     *
     * @return the result
     */

    public Integer getCount() {
        return count;
    }

    /**
     * Sets the count.
     *
     * @param count the count value
     */

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Returns the sources.
     *
     * @return the result
     */

    public List<Source> getSources() {
        return sources;
    }

    /**
     * Sets the sources.
     *
     * @param sources the sources value
     */

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    /**
     * Returns the links.
     *
     * @return the result
     */

    public List<Link> getLinks() {
        return links;
    }

    /**
     * Sets the links.
     *
     * @param links the links value
     */

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    /**
     * Returns the evidence file path.
     *
     * @return the result
     */

    public String getEvidenceFilePath() {
        return evidenceFilePath;
    }

    /**
     * Sets the evidence file path.
     *
     * @param evidenceFilePath the evidence file path value
     */

    public void setEvidenceFilePath(String evidenceFilePath) {
        this.evidenceFilePath = evidenceFilePath;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * The source class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Source implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852237123112L;

        private ArchiveFormat format;
        private String url;

        /**
         * Returns the format.
         *
         * @return the result
         */

        public ArchiveFormat getFormat() {
            return format;
        }

        /**
         * Sets the format.
         *
         * @param format the format value
         */

        public void setFormat(ArchiveFormat format) {
            this.format = format;
        }

        /**
         * Returns the url.
         *
         * @return the result
         */

        public String getUrl() {
            return url;
        }

        /**
         * Sets the url.
         *
         * @param url the url value
         */

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (JacksonJson.toJsonString(this));
        }

    }

    /**
     * The link class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Link implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852237162837L;

        private Long id;
        private String name;
        private String url;
        private Boolean external;

        /**
         * Returns the id.
         *
         * @return the result
         */

        public Long getId() {
            return id;
        }

        /**
         * Sets the id.
         *
         * @param id the id value
         */

        public void setId(Long id) {
            this.id = id;
        }

        /**
         * Returns the name.
         *
         * @return the result
         */

        public String getName() {
            return name;
        }

        /**
         * Sets the name.
         *
         * @param name the name value
         */

        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the url.
         *
         * @return the result
         */

        public String getUrl() {
            return url;
        }

        /**
         * Sets the url.
         *
         * @param url the url value
         */

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns the external.
         *
         * @return the result
         */

        public Boolean getExternal() {
            return external;
        }

        /**
         * Sets the external.
         *
         * @param external the external value
         */

        public void setExternal(Boolean external) {
            this.external = external;
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (JacksonJson.toJsonString(this));
        }

    }

}
