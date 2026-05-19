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
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The member class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Member extends AbstractUser<Member> {

    @Serial
    private static final long serialVersionUID = 2852261708829L;

    private AccessLevel accessLevel;
    private Date expiresAt;
    private Identity groupSamlIdentity;

    /**
     * Returns the access level.
     *
     * @return the result
     */

    public AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Sets the access level.
     *
     * @param accessLevel the access level value
     */

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Returns the expires at.
     *
     * @return the result
     */

    public Date getExpiresAt() {
        return this.expiresAt;
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
     * Returns the group saml identity.
     *
     * @return the result
     */

    public Identity getGroupSamlIdentity() {
        return groupSamlIdentity;
    }

    /**
     * Sets the group saml identity.
     *
     * @param groupSamlIdentity the group saml identity value
     */

    public void setGroupSamlIdentity(Identity groupSamlIdentity) {
        this.groupSamlIdentity = groupSamlIdentity;
    }

    /**
     * Sets the access level and returns this instance.
     *
     * @param accessLevel the access level value
     * @return the result
     */

    public Member withAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    /**
     * Sets the expires at and returns this instance.
     *
     * @param expiresAt the expires at value
     * @return the result
     */

    public Member withExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    /**
     * Sets the group saml identity and returns this instance.
     *
     * @param groupSamlIdentity the group saml identity value
     * @return the result
     */

    public Member withGroupSamlIdentity(Identity groupSamlIdentity) {
        this.groupSamlIdentity = groupSamlIdentity;
        return this;
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
