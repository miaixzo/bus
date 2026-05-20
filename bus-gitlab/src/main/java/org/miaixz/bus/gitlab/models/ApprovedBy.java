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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * This class is used by various models to represent the approved_by property, which can contain a User or Group
 * instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ApprovedBy implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852236797551L;

    private User user;
    private Group group;

    /**
     * Returns the user.
     *
     * @return the result
     */

    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(User user) {
        if (group != null) {
            throw new RuntimeException("ApprovedBy is already set to a group, cannot be set to a user");
        }

        this.user = user;
    }

    /**
     * Returns the group.
     *
     * @return the result
     */

    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group.
     *
     * @param group the group value
     */

    public void setGroup(Group group) {
        if (user != null) {
            throw new RuntimeException("ApprovedBy is already set to a user, cannot be set to a group");
        }

        this.group = group;
    }

    /**
     * Return the user or group that represents this ApprovedBy instance. Returned object will either be an instance of
     * a User or Group.
     *
     * @return the user or group that represents this ApprovedBy instance
     */
    @JsonIgnore
    public Object getApprovedBy() {
        return (user != null ? user : group);
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
