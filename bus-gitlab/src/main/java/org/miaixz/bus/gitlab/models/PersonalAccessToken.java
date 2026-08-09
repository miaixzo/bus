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

import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The personal access token class.
 *
 * @author Kimi Liu
 */
public class PersonalAccessToken implements Serializable {

    /**
     * Constructs a new {@code PersonalAccessToken} instance.
     */
    public PersonalAccessToken() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852269203067L;

    /**
     * The user id value.
     */
    private Long userId;
    /**
     * The scopes value.
     */
    private List<Constants.ProjectAccessTokenScope> scopes;
    /**
     * The name value.
     */
    private String name;
    /**
     * The description value.
     */
    private String description;

    /**
     * The expires at value.
     */
    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date expiresAt;

    /**
     * The id value.
     */
    private Long id;
    /**
     * The active value.
     */
    private Boolean active;
    /**
     * The created at value.
     */
    private Date createdAt;
    /**
     * The revoked value.
     */
    private Boolean revoked;
    /**
     * The last used at value.
     */
    private Date lastUsedAt;
    /**
     * The token value.
     */
    private String token;

    /**
     * Returns the user id.
     *
     * @return the result
     */

    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the user id value
     */

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the scopes.
     *
     * @return the result
     */

    public List<Constants.ProjectAccessTokenScope> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes.
     *
     * @param scopes the scopes value
     */

    public void setScopes(List<Constants.ProjectAccessTokenScope> scopes) {
        this.scopes = scopes;
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
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
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
     * @param expiredAt the expired at value
     */

    public void setExpiresAt(Date expiredAt) {
        this.expiresAt = expiredAt;
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
     * Returns whether the active is enabled.
     *
     * @return the result
     */

    public Boolean isActive() {
        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the active value
     */

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns whether the revoked is enabled.
     *
     * @return the result
     */

    public Boolean isRevoked() {
        return revoked;
    }

    /**
     * Sets the revoked.
     *
     * @param revoked the revoked value
     */

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    /**
     * Returns the last used at.
     *
     * @return the result
     */

    public Date getLastUsedAt() {
        return lastUsedAt;
    }

    /**
     * Sets the last used at.
     *
     * @param lastUsedAt the last used at value
     */

    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
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
