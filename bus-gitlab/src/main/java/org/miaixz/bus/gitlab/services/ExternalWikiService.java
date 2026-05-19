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
package org.miaixz.bus.gitlab.services;

import java.io.Serial;

import org.miaixz.bus.gitlab.models.GitLabForm;

/**
 * The external wiki service class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ExternalWikiService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852285037171L;
    /**
     * The wikiurl key prop value.
     */

    public static final String WIKIURL_KEY_PROP = "external_wiki_url";

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam("external_wiki_url", getExternalWikiUrl(), true);
        return formData;
    }

    /**
     * Returns the external wiki url.
     *
     * @return the result
     */

    public String getExternalWikiUrl() {
        return this.getProperty(WIKIURL_KEY_PROP);
    }

    /**
     * Sets the external wiki url.
     *
     * @param endpoint the endpoint value
     */

    public void setExternalWikiUrl(String endpoint) {
        this.setProperty(WIKIURL_KEY_PROP, endpoint);
    }

    /**
     * Sets the external wiki url and returns this instance.
     *
     * @param endpoint the endpoint value
     * @return the result
     */

    public ExternalWikiService withExternalWikiUrl(String endpoint) {
        setExternalWikiUrl(endpoint);
        return this;
    }

}
