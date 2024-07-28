/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.services;

import org.miaixz.bus.gitlab.GitLabApiForm;

public class CustomIssueTrackerService extends NotificationService {

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabApiForm servicePropertiesForm() {
        GitLabApiForm formData = new GitLabApiForm().withParam(DESCRIPTION_PROP, getDescription())
                .withParam(ISSUES_URL_PROP, getIssuesUrl(), true).withParam(NEW_ISSUE_URL_PROP, getNewIssueUrl(), true)
                .withParam(PROJECT_URL_PROP, getProjectUrl(), true).withParam(PUSH_EVENTS_PROP, getPushEvents())
                .withParam(TITLE_PROP, getTitle());
        return formData;
    }

    public String getNewIssueUrl() {
        return this.getProperty(NEW_ISSUE_URL_PROP);
    }

    public void setNewIssueUrl(String endpoint) {
        this.setProperty(NEW_ISSUE_URL_PROP, endpoint);
    }

    public CustomIssueTrackerService withNewIssueUrl(String endpoint) {
        setNewIssueUrl(endpoint);
        return this;
    }

    public String getIssuesUrl() {
        return this.getProperty(ISSUES_URL_PROP);
    }

    public void setIssuesUrl(String endpoint) {
        this.setProperty(ISSUES_URL_PROP, endpoint);
    }

    public CustomIssueTrackerService withIssuesUrl(String endpoint) {
        setIssuesUrl(endpoint);
        return this;
    }

    public String getProjectUrl() {
        return this.getProperty(PROJECT_URL_PROP);
    }

    public void setProjectUrl(String endpoint) {
        this.setProperty(PROJECT_URL_PROP, endpoint);
    }

    public CustomIssueTrackerService withProjectUrl(String endpoint) {
        setProjectUrl(endpoint);
        return this;
    }

    public String getDescription() {
        return this.getProperty(DESCRIPTION_PROP);
    }

    public void setDescription(String description) {
        this.setProperty(DESCRIPTION_PROP, description);
    }

    public CustomIssueTrackerService withDescription(String description) {
        setDescription(description);
        return this;
    }
}
