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
 * The bugzilla service class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BugzillaService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852283785895L;

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam(DESCRIPTION_PROP, getDescription())
                .withParam(ISSUES_URL_PROP, getIssuesUrl(), true).withParam(NEW_ISSUE_URL_PROP, getNewIssueUrl(), true)
                .withParam(PROJECT_URL_PROP, getProjectUrl(), true).withParam(PUSH_EVENTS_PROP, getPushEvents())
                .withParam(TITLE_PROP, getTitle());
        return formData;
    }

    /**
     * Returns the new issue url.
     *
     * @return the result
     */

    public String getNewIssueUrl() {
        return this.getProperty(NEW_ISSUE_URL_PROP);
    }

    /**
     * Sets the new issue url.
     *
     * @param endpoint the endpoint value
     */

    public void setNewIssueUrl(String endpoint) {
        this.setProperty(NEW_ISSUE_URL_PROP, endpoint);
    }

    /**
     * Sets the new issue url and returns this instance.
     *
     * @param endpoint the endpoint value
     * @return the result
     */

    public BugzillaService withNewIssueUrl(String endpoint) {
        setNewIssueUrl(endpoint);
        return this;
    }

    /**
     * Returns the issues url.
     *
     * @return the result
     */

    public String getIssuesUrl() {
        return this.getProperty(ISSUES_URL_PROP);
    }

    /**
     * Sets the issues url.
     *
     * @param endpoint the endpoint value
     */

    public void setIssuesUrl(String endpoint) {
        this.setProperty(ISSUES_URL_PROP, endpoint);
    }

    /**
     * Sets the issues url and returns this instance.
     *
     * @param endpoint the endpoint value
     * @return the result
     */

    public BugzillaService withIssuesUrl(String endpoint) {
        setIssuesUrl(endpoint);
        return this;
    }

    /**
     * Returns the project url.
     *
     * @return the result
     */

    public String getProjectUrl() {
        return this.getProperty(PROJECT_URL_PROP);
    }

    /**
     * Sets the project url.
     *
     * @param endpoint the endpoint value
     */

    public void setProjectUrl(String endpoint) {
        this.setProperty(PROJECT_URL_PROP, endpoint);
    }

    /**
     * Sets the project url and returns this instance.
     *
     * @param endpoint the endpoint value
     * @return the result
     */

    public BugzillaService withProjectUrl(String endpoint) {
        setProjectUrl(endpoint);
        return this;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return this.getProperty(DESCRIPTION_PROP);
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.setProperty(DESCRIPTION_PROP, description);
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public BugzillaService withDescription(String description) {
        setDescription(description);
        return this;
    }

}
