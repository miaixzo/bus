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
 * The identity class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Identity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852256910511L;

    private String provider;
    private String externUid;
    private Integer samlProviderId;

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
     * @param provider the provider value
     */

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Returns the extern uid.
     *
     * @return the result
     */

    public String getExternUid() {
        return externUid;
    }

    /**
     * Sets the extern uid.
     *
     * @param externUid the extern uid value
     */

    public void setExternUid(String externUid) {
        this.externUid = externUid;
    }

    /**
     * Returns the saml provider id.
     *
     * @return the result
     */

    public Integer getSamlProviderId() {
        return samlProviderId;
    }

    /**
     * Sets the saml provider id.
     *
     * @param samlProviderId the saml provider id value
     */

    public void setSamlProviderId(Integer samlProviderId) {
        this.samlProviderId = samlProviderId;
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
