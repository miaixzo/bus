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

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The abstract user class.
 *
 * @param <U> the concrete user model type
 * @author Kimi Liu
 * @since Java 21+
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public abstract class AbstractUser<U extends AbstractUser<U>> implements Serializable {

    /**
     * Constructs a new AbstractUser instance.
     */
    public AbstractUser() {
        // No initialization required.
    }

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

    /**
     * Sets the avatar URL and returns this user model.
     *
     * @param avatarUrl the avatar URL
     * @return this user model
     */
    public U withAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return (U) this;
    }

    /**
     * Sets the creation time and returns this user model.
     *
     * @param createdAt the creation time
     * @return this user model
     */
    public U withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return (U) this;
    }

    /**
     * Sets the email address and returns this user model.
     *
     * @param email the email address
     * @return this user model
     */
    public U withEmail(String email) {
        this.email = email;
        return (U) this;
    }

    /**
     * Sets the user ID and returns this user model.
     *
     * @param id the user ID
     * @return this user model
     */
    public U withId(Long id) {
        this.id = id;
        return (U) this;
    }

    /**
     * Sets the display name and returns this user model.
     *
     * @param name the display name
     * @return this user model
     */
    public U withName(String name) {
        this.name = name;
        return (U) this;
    }

    /**
     * Sets the user state and returns this user model.
     *
     * @param state the user state
     * @return this user model
     */
    public U withState(String state) {
        this.state = state;
        return (U) this;
    }

    /**
     * Sets the username and returns this user model.
     *
     * @param username the username
     * @return this user model
     */
    public U withUsername(String username) {
        this.username = username;
        return (U) this;
    }

    /**
     * Sets the web URL and returns this user model.
     *
     * @param webUrl the web URL
     * @return this user model
     */
    public U withWebUrl(String webUrl) {
        this.webUrl = webUrl;
        return (U) this;
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
