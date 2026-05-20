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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The metadata class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Metadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852265316087L;

    private String version;
    private String revision;
    private Kas kas;
    private Boolean enterprise;

    /**
     * Returns the version.
     *
     * @return the result
     */

    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the version value
     */

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the revision.
     *
     * @return the result
     */

    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision.
     *
     * @param revision the revision value
     */

    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the kas.
     *
     * @return the result
     */

    public Kas getKas() {
        return kas;
    }

    /**
     * Sets the kas.
     *
     * @param kas the kas value
     */

    public void setKas(Kas kas) {
        this.kas = kas;
    }

    /**
     * Returns the enterprise.
     *
     * @return the result
     */

    public Boolean getEnterprise() {
        return enterprise;
    }

    /**
     * Sets the enterprise.
     *
     * @param enterprise the enterprise value
     */

    public void setEnterprise(Boolean enterprise) {
        this.enterprise = enterprise;
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
     * The kas class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Kas {

        private Boolean enabled;

        @JsonProperty("externalUrl")
        private String externalUrl;

        private String version;

        /**
         * Returns the enabled.
         *
         * @return the result
         */

        public Boolean getEnabled() {
            return enabled;
        }

        /**
         * Sets the enabled.
         *
         * @param enabled the enabled value
         */

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Returns the external url.
         *
         * @return the result
         */

        public String getExternalUrl() {
            return externalUrl;
        }

        /**
         * Sets the external url.
         *
         * @param externalUrl the external url value
         */

        public void setExternalUrl(String externalUrl) {
            this.externalUrl = externalUrl;
        }

        /**
         * Returns the version.
         *
         * @return the result
         */

        public String getVersion() {
            return version;
        }

        /**
         * Sets the version.
         *
         * @param version the version value
         */

        public void setVersion(String version) {
            this.version = version;
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
