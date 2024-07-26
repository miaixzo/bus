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
package org.miaixz.bus.gitlab;

import org.miaixz.bus.gitlab.models.IssueEvent;

import java.util.List;
import java.util.stream.Stream;

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
