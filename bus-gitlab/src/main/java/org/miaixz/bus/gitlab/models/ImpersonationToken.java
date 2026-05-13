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
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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

    public String getDescription() {
        return description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
         * Grants complete read/write access to the API.
         */
        API,

        /**
         * Grants read access to the API.
         */
        READ_API,

        /**
         * Grants read-only access to user profile information.
         */
        READ_USER,

        /**
         * Grants read-only access to repositories.
         */
        READ_REPOSITORY,

        /**
         * Grants read-write access to repositories.
         */
        WRITE_REPOSITORY,

        /**
         * Grants read (pull) access to the container registry.
         */
        READ_REGISTRY,

        /**
         * Grants write (push) access to the container registry.
         */
        WRITE_REGISTRY,

        /**
         * Grants read access through the virtual registry.
         */
        READ_VIRTUAL_REGISTRY,

        /**
         * Grants write access through the virtual registry.
         */
        WRITE_VIRTUAL_REGISTRY,

        /**
         * Grants create access to runners.
         */
        CREATE_RUNNER,

        /**
         * Grants access to manage runners.
         */
        MANAGE_RUNNER,

        /**
         * Grants access to GitLab Duo related API endpoints.
         */
        AI_FEATURES,

        /**
         * Grants permission to perform Kubernetes API calls using the agent for Kubernetes.
         */
        K8S_PROXY,

        /**
         * Grants permission to rotate this token using the impersonation token API.
         */
        SELF_ROTATE,

        /**
         * Grants permission to perform API actions as any user when authenticated as an administrator.
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Boolean getImpersonation() {
        return impersonation;
    }

    public void setImpersonation(Boolean impersonation) {
        this.impersonation = impersonation;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
