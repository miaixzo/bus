/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab;

import java.util.List;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.IssueEvent;

/**
 * This class provides an entry point to all the GitLab Resource state events API
 * 
 * @see <a href="https://docs.gitlab.com/ce/api/resource_state_events.html">Resource state events API at GitLab</a>
 */
public class ResourceStateEventsApi extends AbstractApi {

    public ResourceStateEventsApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Gets a list of all state events for a single issue.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/issues/:issue_iid/resource_state_events</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param issueIid        the IID of the issue
     * @return a List of IssueEvent for the specified issue
     * @throws GitLabApiException if any exception occurs
     */
    public List<IssueEvent> getIssueStateEvents(Object projectIdOrPath, Long issueIid) throws GitLabApiException {
        return (getIssueStateEvents(projectIdOrPath, issueIid, getDefaultPerPage()).all());
    }

    /**
     * Gets a Pager of all state events for a single issue.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/issues/:issue_iid/resource_state_events</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param issueIid        the IID of the issue
     * @param itemsPerPage    the number of LabelEvent instances that will be fetched per page
     * @return the Pager of IssueEvent instances for the specified issue IID
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<IssueEvent> getIssueStateEvents(Object projectIdOrPath, Long issueIid, int itemsPerPage)
            throws GitLabApiException {
        return (new Pager<IssueEvent>(this, IssueEvent.class, itemsPerPage, null, "projects",
                getProjectIdOrPath(projectIdOrPath), "issues", issueIid, "resource_state_events"));
    }

    /**
     * Gets a Stream of all state events for a single issue.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/issues/:issue_iid/resource_state_events</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param issueIid        the IID of the issue
     * @return a Stream of IssueEvent for the specified issue
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<IssueEvent> getIssueStateEventsStream(Object projectIdOrPath, Long issueIid)
            throws GitLabApiException {
        return (getIssueStateEvents(projectIdOrPath, issueIid, getDefaultPerPage()).stream());
    }

}
