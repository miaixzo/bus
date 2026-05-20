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
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The user system hook event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UserSystemHookEvent extends AbstractSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229925117L;
    /**
     * The user create event value.
     */

    public static final String USER_CREATE_EVENT = "user_create";
    /**
     * The user destroy event value.
     */
    public static final String USER_DESTROY_EVENT = "user_destroy";
    /**
     * The user rename event value.
     */
    public static final String USER_RENAME_EVENT = "user_rename";
    /**
     * The user failed login event value.
     */
    public static final String USER_FAILED_LOGIN_EVENT = "user_failed_login";

    private String eventName;
    private Date createdAt;
    private Date updatedAt;
    private String email;
    private String name;
    private String username;
    private Long userId;
    private String oldUsername;
    private String state;

    /**
     * Returns the event name.
     *
     * @return the result
     */

    @Override
    public String getEventName() {
        return this.eventName;
    }

    /**
     * Sets the event name.
     *
     * @param eventName the event name value
     */

    public void setEventName(String eventName) {
        this.eventName = eventName;
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
     * Returns the updated at.
     *
     * @return the result
     */

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the email.
     *
     * @return the result
     */

    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the email value
     */

    public void setEmail(String email) {
        this.email = email;
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
     * Returns the old username.
     *
     * @return the result
     */

    public String getOldUsername() {
        return oldUsername;
    }

    /**
     * Sets the old username.
     *
     * @param oldUsername the old username value
     */

    public void setOldUsername(String oldUsername) {
        this.oldUsername = oldUsername;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(String state) {
        this.state = state;
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
