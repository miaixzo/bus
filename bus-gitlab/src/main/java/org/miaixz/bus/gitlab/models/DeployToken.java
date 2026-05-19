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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The deploy token class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DeployToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852251025919L;

    private Long id;
    private String name;
    private String username;
    private Date expiresAt;
    private List<Constants.DeployTokenScope> scopes;
    private String token;

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
     * Returns the username.
     *
     * @return the result
     */

    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username value
     */

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the expires at.
     *
     * @return the result
     */

    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expires at.
     *
     * @param expiresAt the expires at value
     */

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the scopes.
     *
     * @return the result
     */

    public List<Constants.DeployTokenScope> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes.
     *
     * @param scopes the scopes value
     */

    public void setScopes(List<Constants.DeployTokenScope> scopes) {
        this.scopes = scopes;
    }

    /**
     * Returns the token.
     *
     * @return the result
     */

    public String getToken() {
        return token;
    }

    /**
     * Sets the token.
     *
     * @param token the token value
     */

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return JacksonJson.toJsonString(this);
    }

}
