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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The application class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Application implements Serializable {

    /**
     * Constructs a new Application instance.
     */
    public Application() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852235903898L;

    private Long id;
    private String applicationId;
    private String applicationName;
    private String callbackUrl;
    private Boolean confidential;
    private String secret;

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
     * Returns the application id.
     *
     * @return the result
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application id.
     *
     * @param applicationId the application id value
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the application name.
     *
     * @return the result
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the application name.
     *
     * @param applicationName the application name value
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Returns the callback url.
     *
     * @return the result
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Sets the callback url.
     *
     * @param callbackUrl the callback url value
     */
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * Returns the confidential.
     *
     * @return the result
     */
    public Boolean getConfidential() {
        return confidential;
    }

    /**
     * Sets the confidential.
     *
     * @param confidential the confidential value
     */
    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

    /**
     * Returns the secret.
     *
     * @return the result
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret.
     *
     * @param secret the secret value
     */
    public void setSecret(String secret) {
        this.secret = secret;
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
