/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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

import java.util.List;
import java.util.stream.Stream;

/**
 * This class provides an entry point to all the GitLab API Search API calls.
 * 
 * @see <a href="https://gitlab.com/help/api/search.md">Search API</a>
 */
public class SearchApi extends AbstractApi {

    public SearchApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Search globally across the GitLab instance.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param scope  search the expression within the specified scope. Currently these scopes are supported: projects,
     *               issues, merge_requests, milestones, snippet_titles, snippet_blobs, users
     * @param search the search query
     * @return a List containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> List<T> globalSearch(SearchScope<T> scope, String search) throws GitLabApiException {
        return (globalSearch(scope, search, this.getDefaultPerPage()).all());
    }

    /**
     * Search globally across the GitLab instance.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param scope  search the expression within the specified scope. Currently these scopes are supported: projects,
     *               issues, merge_requests, milestones, snippet_titles, snippet_blobs, users
     * @param search the search query
     * @return a Stream containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Stream<T> globalSearchStream(SearchScope<T> scope, String search) throws GitLabApiException {
        return (globalSearch(scope, search, getDefaultPerPage()).stream());
    }

    /**
     * Search globally across the GitLab instance.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param scope        search the expression within the specified scope. Currently these scopes are supported:
     *                     projects, issues, merge_requests, milestones, snippet_titles, snippet_blobs, users
     * @param search       the search query
     * @param itemsPerPage the number of items that will be fetched per page
     * @return a Pager containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Pager<T> globalSearch(SearchScope<T> scope, String search, int itemsPerPage) throws GitLabApiException {

        GitLabApiForm formData = new GitLabApiForm().withParam("scope", scope, true).withParam("search", search, true);

        return (new Pager<>(this, scope.getResultType(), itemsPerPage, formData.asMap(), "search"));
    }

    /**
     * Search within the specified group. If a user is not a member of a group and the group is private, a request on
     * that group will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /groups/:groupId/search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param groupIdOrPath the group ID, path of the group, or a Group instance holding the group ID or path, required
     * @param scope         search the expression within the specified scope. Currently these scopes are supported:
     *                      projects, issues, merge_requests, milestones, users
     * @param search        the search query
     * @return a List containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> List<T> groupSearch(Object groupIdOrPath, GroupSearchScope<T> scope, String search)
            throws GitLabApiException {
        return (groupSearch(groupIdOrPath, scope, search, this.getDefaultPerPage()).all());
    }

    /**
     * Search within the specified group. If a user is not a member of a group and the group is private, a request on
     * that group will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /groups/:groupId/search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param groupIdOrPath the group ID, path of the group, or a Group instance holding the group ID or path, required
     * @param scope         search the expression within the specified scope. Currently these scopes are supported:
     *                      projects, issues, merge_requests, milestones, users
     * @param search        the search query
     * @return a Stream containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Stream<T> groupSearchStream(Object groupIdOrPath, GroupSearchScope<T> scope, String search)
            throws GitLabApiException {
        return (groupSearch(groupIdOrPath, scope, search, getDefaultPerPage()).stream());
    }

    /**
     * Search within the specified group. If a user is not a member of a group and the group is private, a request on
     * that group will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /groups/:groupId/search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param groupIdOrPath the group ID, path of the group, or a Group instance holding the group ID or path, required
     * @param scope         search the expression within the specified scope. Currently these scopes are supported:
     *                      projects, issues, merge_requests, milestones, users
     * @param search        the search query
     * @param itemsPerPage  the number of items that will be fetched per page
     * @return a Pager containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Pager<T> groupSearch(Object groupIdOrPath, GroupSearchScope<T> scope, String search, int itemsPerPage)
            throws GitLabApiException {

        GitLabApiForm formData = new GitLabApiForm().withParam("scope", scope, true).withParam("search", search, true);

        return new Pager<>(this, scope.getResultType(), itemsPerPage, formData.asMap(), "groups",
                getGroupIdOrPath(groupIdOrPath), "search");
    }

    /**
     * Search within the specified project. If a user is not a member of a project and the project is private, a request
     * on that project will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:projectId/search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance, required
     * @param scope           search the expression within the specified scope. Currently these scopes are supported:
     *                        issues, merge_requests, milestones, notes, wiki_blobs, commits, blobs, users
     * @param search          the search query
     * @return a List containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> List<T> projectSearch(Object projectIdOrPath, ProjectSearchScope<T> scope, String search)
            throws GitLabApiException {
        return (projectSearch(projectIdOrPath, scope, search, null, this.getDefaultPerPage()).all());
    }

    /**
     * Search within the specified project. If a user is not a member of a project and the project is private, a request
     * on that project will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:projectId/search?scope=:scope&amp;search=:search-query&amp;ref=ref</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance, required
     * @param scope           search the expression within the specified scope. Currently these scopes are supported:
     *                        issues, merge_requests, milestones, notes, wiki_blobs, commits, blobs, users
     * @param search          the search query
     * @param ref             the name of a repository branch or tag to search on. The project’s default branch is used
     *                        by default. This is only applicable for scopes: commits, blobs, and wiki_blobs.
     * @return a List containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> List<T> projectSearch(Object projectIdOrPath, ProjectSearchScope<T> scope, String search, String ref)
            throws GitLabApiException {
        return (projectSearch(projectIdOrPath, scope, search, ref, this.getDefaultPerPage()).all());
    }

    /**
     * Search within the specified project. If a user is not a member of a project and the project is private, a request
     * on that project will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:projectId/search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance, required
     * @param scope           search the expression within the specified scope. Currently these scopes are supported:
     *                        issues, merge_requests, milestones, notes, wiki_blobs, commits, blobs, users
     * @param search          the search query
     * @return a Stream containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Stream<T> projectSearchStream(Object projectIdOrPath, ProjectSearchScope<T> scope, String search)
            throws GitLabApiException {
        return (projectSearch(projectIdOrPath, scope, search, null, getDefaultPerPage()).stream());
    }

    /**
     * Search within the specified project. If a user is not a member of a project and the project is private, a request
     * on that project will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:projectId/search?scope=:scope&amp;search=:search-query&amp;ref=ref</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance, required
     * @param scope           search the expression within the specified scope. Currently these scopes are supported:
     *                        issues, merge_requests, milestones, notes, wiki_blobs, commits, blobs, users
     * @param search          the search query
     * @param ref             the name of a repository branch or tag to search on. The project’s default branch is used
     *                        by default. This is only applicable for scopes: commits, blobs, and wiki_blobs.
     * @return a Stream containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Stream<T> projectSearchStream(Object projectIdOrPath, ProjectSearchScope<T> scope, String search,
            String ref) throws GitLabApiException {
        return (projectSearch(projectIdOrPath, scope, search, ref, getDefaultPerPage()).stream());
    }

    /**
     * Search within the specified project. If a user is not a member of a project and the project is private, a request
     * on that project will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /project/:projectId/search?scope=:scope&amp;search=:search-query</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance, required
     * @param scope           search the expression within the specified scope. Currently these scopes are supported:
     *                        issues, merge_requests, milestones, notes, wiki_blobs, commits, blobs, users
     * @param search          the search query
     * @param itemsPerPage    the number of items that will be fetched per page
     * @return a Pager containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Pager<T> projectSearch(Object projectIdOrPath, ProjectSearchScope<T> scope, String search,
            int itemsPerPage) throws GitLabApiException {
        return projectSearch(projectIdOrPath, scope, search, null, itemsPerPage);
    }

    /**
     * Search within the specified project. If a user is not a member of a project and the project is private, a request
     * on that project will result to a 404 status code.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /project/:projectId/search?scope=:scope&amp;search=:search-query&amp;ref=ref</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance, required
     * @param scope           search the expression within the specified scope. Currently these scopes are supported:
     *                        issues, merge_requests, milestones, notes, wiki_blobs, commits, blobs, users
     * @param search          the search query
     * @param ref             the name of a repository branch or tag to search on. The project’s default branch is used
     *                        by default. This is only applicable for scopes: commits, blobs, and wiki_blobs.
     * @param itemsPerPage    the number of items that will be fetched per page
     * @return a Pager containing the object type specified by the scope
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 10.5
     */
    public <T> Pager<T> projectSearch(Object projectIdOrPath, ProjectSearchScope<T> scope, String search, String ref,
            int itemsPerPage) throws GitLabApiException {

        GitLabApiForm formData = new GitLabApiForm().withParam("scope", scope, true).withParam("search", search, true)
                .withParam("ref", ref, false);

        if (ref != null) {
            if (!scope.equals(ProjectSearchScope.BLOBS) && !scope.equals(ProjectSearchScope.WIKI_BLOBS)
                    && !scope.equals(ProjectSearchScope.COMMITS)) {
                throw new GitLabApiException(
                        "Ref parameter is only applicable for scopes: commits, blobs, and wiki_blobs");
            }
        }

        return (new Pager<>(this, scope.getResultType(), itemsPerPage, formData.asMap(), "projects",
                getProjectIdOrPath(projectIdOrPath), "search"));
    }

}
