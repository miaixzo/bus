/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serial;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractUser<U extends AbstractUser<U>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852235350570L;

    private String avatarUrl;
    private Date createdAt;
    private String email;
    private Long id;
    private String name;
    private String state;
    private Boolean locked;
    private String username;
    private String webUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public U withAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return (U) this;
    }

    public U withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return (U) this;
    }

    public U withEmail(String email) {
        this.email = email;
        return (U) this;
    }

    public U withId(Long id) {
        this.id = id;
        return (U) this;
    }

    public U withName(String name) {
        this.name = name;
        return (U) this;
    }

    public U withState(String state) {
        this.state = state;
        return (U) this;
    }

    public U withUsername(String username) {
        this.username = username;
        return (U) this;
    }

    public U withWebUrl(String webUrl) {
        this.webUrl = webUrl;
        return (U) this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
