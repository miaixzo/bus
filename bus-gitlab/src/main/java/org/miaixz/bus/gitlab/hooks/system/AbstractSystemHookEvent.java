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
package org.miaixz.bus.gitlab.hooks.system;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The abstract system hook event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractSystemHookEvent implements SystemHookEvent {

    /**
     * Constructs a new AbstractSystemHookEvent instance.
     */
    public AbstractSystemHookEvent() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852292516603L;

    private String requestUrl;
    private String requestQueryString;
    private String requestSecretToken;

    /**
     * Returns the request url.
     *
     * @return the result
     */

    @Override
    @JsonIgnore
    public String getRequestUrl() {
        return (requestUrl);
    }

    /**
     * Sets the request url.
     *
     * @param requestUrl the request url value
     */

    @Override
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * Returns the request query string.
     *
     * @return the result
     */

    @Override
    @JsonIgnore
    public String getRequestQueryString() {
        return (requestQueryString);
    }

    /**
     * Sets the request query string.
     *
     * @param requestQueryString the request query string value
     */

    @Override
    public void setRequestQueryString(String requestQueryString) {
        this.requestQueryString = requestQueryString;
    }

    /**
     * Returns the request secret token.
     *
     * @return the result
     */

    @Override
    @JsonIgnore
    public String getRequestSecretToken() {
        return (requestSecretToken);
    }

    /**
     * Sets the request secret token.
     *
     * @param requestSecretToken the request secret token value
     */

    @Override
    public void setRequestSecretToken(String requestSecretToken) {
        this.requestSecretToken = requestSecretToken;
    }

}
