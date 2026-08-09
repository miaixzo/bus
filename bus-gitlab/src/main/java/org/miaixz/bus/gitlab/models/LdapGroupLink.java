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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The LDAP group link class.
 *
 * @author Kimi Liu
 */
public class LdapGroupLink implements Serializable {

    /**
     * Constructs a new {@code LdapGroupLink} instance.
     */
    public LdapGroupLink() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852260326318L;

    /**
     * The cn value.
     */
    private String cn;

    /**
     * The group access value.
     */
    private AccessLevel groupAccess;

    /**
     * The provider value.
     */
    private String provider;

    /**
     * The filter value.
     */
    private String filter;

    /**
     * Returns the cn.
     *
     * @return the result
     */

    public String getCn() {
        return cn;
    }

    /**
     * Sets the cn.
     *
     * @param aCn the a cn value
     */

    public void setCn(String aCn) {
        cn = aCn;
    }

    /**
     * Returns the group access.
     *
     * @return the result
     */

    public AccessLevel getGroupAccess() {
        return groupAccess;
    }

    /**
     * Sets the group access.
     *
     * @param aGroupAccess the a group access value
     */

    public void setGroupAccess(AccessLevel aGroupAccess) {
        groupAccess = aGroupAccess;
    }

    /**
     * Returns the provider.
     *
     * @return the result
     */

    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider.
     *
     * @param aProvider the a provider value
     */

    public void setProvider(String aProvider) {
        provider = aProvider;
    }

    /**
     * Returns the filter.
     *
     * @return the result
     */

    public String getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param aFilter the a filter value
     */

    public void setFilter(String aFilter) {
        filter = aFilter;
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
