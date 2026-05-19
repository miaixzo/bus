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

/**
 * The access request class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AccessRequest extends AbstractUser<AccessRequest> {

    /**
     * Constructs a new AccessRequest instance.
     */
    public AccessRequest() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852235619209L;

    private Date requestedAt;
    private AccessLevel accessLevel;

    /**
     * Returns the requested at.
     *
     * @return the result
     */
    public Date getRequestedAt() {
        return requestedAt;
    }

    /**
     * Sets the requested at.
     *
     * @param requestedAt the requested at value
     */
    public void setRequestedAt(Date requestedAt) {
        this.requestedAt = requestedAt;
    }

    /**
     * Returns the access level.
     *
     * @return the result
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the access level.
     *
     * @param accessLevel the access level value
     */
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

}
