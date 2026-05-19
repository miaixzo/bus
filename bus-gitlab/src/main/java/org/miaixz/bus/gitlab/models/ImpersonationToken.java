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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The impersonation token class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImpersonationToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852257073750L;

    private String description;
    private Boolean active;
    private String token;
    private List<Scope> scopes;
    private Long userId;
    private Boolean revoked;
    private String name;
    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date expiresAt;
    private Long id;
    private Date createdAt;
    private Date lastUsedAt;
    private Boolean impersonation;

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Returns the active.
     *
     * @return the result
     */

    public Boolean getActive() {
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
     * Returns the scopes.
     *
     * @return the result
     */

    public List<Scope> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes.
     *
     * @param scopes the scopes value
     */

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

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
     * Returns the revoked.
     *
     * @return the result
     */

    public Boolean getRevoked() {
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
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Enum to specify the scope of an ImpersonationToken.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Scope {

        /**
         * The api scope.
         */
        API,
        /**
         * The read api scope.
         */
        READ_API,
        /**
         * The read user scope.
         */
        READ_USER,
        /**
         * The read repository scope.
         */
        READ_REPOSITORY,
        /**
         * The write repository scope.
         */
        WRITE_REPOSITORY,
        /**
         * The read registry scope.
         */
        READ_REGISTRY,
        /**
         * The write registry scope.
         */
        WRITE_REGISTRY,
        /**
         * The read virtual registry scope.
         */
        READ_VIRTUAL_REGISTRY,
        /**
         * The write virtual registry scope.
         */
        WRITE_VIRTUAL_REGISTRY,
        /**
         * The create runner scope.
         */
        CREATE_RUNNER,
        /**
         * The manage runner scope.
         */
        MANAGE_RUNNER,
        /**
         * The ai features scope.
         */
        AI_FEATURES,
        /**
         * The k8 s proxy scope.
         */
        K8S_PROXY,
        /**
         * The self rotate scope.
         */
        SELF_ROTATE,
        /**
         * The sudo scope.
         */
        SUDO;

        /**
         * JSON enum conversion helper.
         */
        private static JacksonJsonEnumHelper<Scope> enumHelper = new JacksonJsonEnumHelper<>(Scope.class);

        /**
         * Converts a GitLab API value into an impersonation token scope.
         *
         * @param value the GitLab API value
         * @return the matching impersonation token scope
         */
        @JsonCreator
        public static Scope forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Converts this impersonation token scope to the GitLab API value.
         *
         * @return the GitLab API value
         */
        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Converts this impersonation token scope to the GitLab API value.
         *
         * @return the GitLab API value
         */
        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

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
     * Returns the impersonation.
     *
     * @return the result
     */

    public Boolean getImpersonation() {
        return impersonation;
    }

    /**
     * Sets the impersonation.
     *
     * @param impersonation the impersonation value
     */

    public void setImpersonation(Boolean impersonation) {
        this.impersonation = impersonation;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
