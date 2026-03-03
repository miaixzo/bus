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

import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;
import java.io.Serial;

public class LdapGroupLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852260326318L;

    private String cn;

    private AccessLevel groupAccess;

    private String provider;

    private String filter;

    public String getCn() {
        return cn;
    }

    public void setCn(String aCn) {
        cn = aCn;
    }

    public AccessLevel getGroupAccess() {
        return groupAccess;
    }

    public void setGroupAccess(AccessLevel aGroupAccess) {
        groupAccess = aGroupAccess;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String aProvider) {
        provider = aProvider;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String aFilter) {
        filter = aFilter;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
