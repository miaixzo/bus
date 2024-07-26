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
package org.miaixz.bus.gitlab.models;

import org.miaixz.bus.gitlab.GitLabApiForm;

import java.io.Serializable;
import java.util.List;

/**
 * This class is used to filter Groups when getting lists of groups for a specified project.
 */
public class ProjectGroupsFilter implements Serializable {
    private static final long serialVersionUID = -1L;

    private String search;
    private AccessLevel sharedMinAccessLevel;
    private Boolean sharedVisibleOnly;
    private List<Long> skipGroups;
    private Boolean withShared;

    /**
     * Search for specific groups.
     *
     * @param search the search criteria
     * @return the reference to this ProjectGroupsFilter instance
     */
    public ProjectGroupsFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    /**
     * Limit to shared groups with at least this role.
     *
     * @param sharedMinAccessLevel the minimal role
     * @return the reference to this ProjectGroupsFilter instance
     */
    public ProjectGroupsFilter withSharedMinAccessLevel(AccessLevel sharedMinAccessLevel) {
        this.sharedMinAccessLevel = sharedMinAccessLevel;
        return (this);
    }

    /**
     * Limit to shared groups user has access to.
     *
     * @param sharedVisibleOnly if true limit to the shared groups user has access to.
     * @return the reference to this ProjectGroupsFilter instance
     */
    public ProjectGroupsFilter withSharedVisibleOnly(Boolean sharedVisibleOnly) {
        this.sharedVisibleOnly = sharedVisibleOnly;
        return (this);
    }

    /**
     * Do not include the provided groups IDs.
     *
     * @param skipGroups List of group IDs to not include in the search
     * @return the reference to this ProjectGroupsFilter instance
     */
    public ProjectGroupsFilter withSkipGroups(List<Long> skipGroups) {
        this.skipGroups = skipGroups;
        return (this);
    }

    /**
     * Include projects shared with this group.
     *
     * @param withShared if true include projects shared with this group.
     * @return the reference to this ProjectGroupsFilter instance
     */
    public ProjectGroupsFilter withWithShared(Boolean withShared) {
        this.withShared = withShared;
        return (this);
    }

    /**
     * Get the query params specified by this filter.
     *
     * @return a GitLabApiForm instance holding the query parameters for this ProjectGroupsFilter instance
     */
    public GitLabApiForm getQueryParams() {
        return (new GitLabApiForm().withParam("search", search)
                .withParam("shared_min_access_level", sharedMinAccessLevel)
                .withParam("shared_visible_only", sharedVisibleOnly).withParam("skip_groups", skipGroups)
                .withParam("with_shared", withShared));
    }
}
