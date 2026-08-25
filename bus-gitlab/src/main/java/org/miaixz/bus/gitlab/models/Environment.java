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
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumCodec;

/**
 * The environment class.
 *
 * @author Kimi Liu
 */
public class Environment implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852252558305L;
    /**
     * The tier value.
     */
    private String tier;

    /**
     * The id value.
     */
    private Long id;
    /**
     * The name value.
     */
    private String name;
    /**
     * The slug value.
     */
    private String slug;
    /**
     * The external url value.
     */
    private String externalUrl;
    /**
     * The auto stop at value.
     */
    private Date autoStopAt;
    /**
     * The state value.
     */
    private EnvironmentState state;
    /**
     * The last deployment value.
     */
    private Deployment lastDeployment;

    /**
     * Constructs a new {@code Environment} instance.
     */
    public Environment() {
        // No initialization required.
    }

    /**
     * Returns the tier.
     *
     * @return the result
     */

    public String getTier() {
        return tier;
    }

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
     * Returns the slug.
     *
     * @return the result
     */

    public String getSlug() {
        return slug;
    }

    /**
     * Sets the slug.
     *
     * @param slug the slug value
     */

    public void setSlug(String slug) {
        this.slug = slug;
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
     * Sets the tier.
     *
     * @param tier the tier value
     */

    public void setTier(String tier) {
        this.tier = tier;
    }

    /**
     * Returns the auto stop at.
     *
     * @return the result
     */

    public Date getAutoStopAt() {
        return autoStopAt;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public EnvironmentState getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(EnvironmentState state) {
        this.state = state;
    }

    /**
     * Returns the last deployment.
     *
     * @return the result
     */

    public Deployment getLastDeployment() {
        return lastDeployment;
    }

    /**
     * Sets the last deployment.
     *
     * @param lastDeployment the last deployment value
     */

    public void setLastDeployment(Deployment lastDeployment) {
        this.lastDeployment = lastDeployment;
    }

    /**
     * Sets the auto stop at.
     *
     * @param autoStopAt the auto stop at value
     */

    public void setAutoStopAt(Date autoStopAt) {
        this.autoStopAt = autoStopAt;
    }

    /**
     * The environment state enum.
     *
     * @author Kimi Liu
     */
    public enum EnvironmentState {

        /**
         * The available environment state.
         */
        AVAILABLE,
        /**
         * The stopped environment state.
         */
        STOPPED;

        /**
         * The enum codec value.
         */
        private static JacksonJsonEnumCodec<EnvironmentState> enumCodec = new JacksonJsonEnumCodec<>(
                EnvironmentState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static EnvironmentState forValue(String value) {
            return enumCodec.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumCodec.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumCodec.toString(this));
        }

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
