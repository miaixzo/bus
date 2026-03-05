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
package org.miaixz.bus.gitlab;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.Release;
import org.miaixz.bus.gitlab.models.ReleaseParams;

import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab Releases API calls.
 * 
 * @see <a href="https://docs.gitlab.com/ce/api/releases">Releases API at GitLab</a>
 */
public class ReleasesApi extends AbstractApi {

    public ReleasesApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get a list of releases for a project, sorted by release date.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/releases</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @return the list of releases for the specified project
     * @throws GitLabApiException if any exception occurs
     */
    public List<Release> getReleases(Object projectIdOrPath) throws GitLabApiException {
        return (getReleases(projectIdOrPath, getDefaultPerPage()).all());
    }

    /**
     * Get a Pager of releases for a project, sorted by release date.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/releases</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param itemsPerPage    the number of Release instances that will be fetched per page
     * @return the Pager of Release instances for the specified project ID
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Release> getReleases(Object projectIdOrPath, int itemsPerPage) throws GitLabApiException {
        return (new Pager<Release>(this, Release.class, itemsPerPage, null, "projects",
                getProjectIdOrPath(projectIdOrPath), "releases"));
    }

    /**
     * Get a Stream of releases for a project, sorted by release date.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/releases</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @return a Stream of Release instances for the specified project ID
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Release> getReleasesStream(Object projectIdOrPath) throws GitLabApiException {
        return (getReleases(projectIdOrPath, getDefaultPerPage()).stream());
    }

    /**
     * Get a Release for the given tag name.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/releases/:tagName</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param tagName         the name of the tag to fetch the Release for
     * @return a Releases instance with info on the specified tag
     * @throws GitLabApiException if any exception occurs
     */
    public Release getRelease(Object projectIdOrPath, String tagName) throws GitLabApiException {
        Response response = get(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "releases",
                urlEncode(tagName));
        return (response.readEntity(Release.class));
    }

    /**
     * Get an Optional instance holding a Release instance for the specific tag name.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/releases/:tagName</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param tagName         the name of the tag to fetch the Release for
     * @return an Optional instance with the specified Release as the value
     * @throws GitLabApiException if any exception occurs
     */
    public Optional<Release> getOptionalRelease(Object projectIdOrPath, String tagName) throws GitLabApiException {
        try {
            return (Optional.ofNullable(getRelease(projectIdOrPath, tagName)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Create a Release. You need push access to the repository to create a Release.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:id/releases</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param params          a ReleaseParams instance holding the parameters for the release
     * @return a Release instance containing the newly created Release info
     * @throws GitLabApiException if any exception occurs
     */
    public Release createRelease(Object projectIdOrPath, ReleaseParams params) throws GitLabApiException {
        Response response = post(
                Response.Status.CREATED,
                params,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "releases");
        return (response.readEntity(Release.class));
    }

    /**
     * Updates the release notes of a given release.
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /projects/:id/releases/:tag_name</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param params          a ReleaseParams instance holding the parameters for the release
     * @return a Release instance containing info on the updated Release
     * @throws GitLabApiException if any exception occurs
     */
    public Release updateRelease(Object projectIdOrPath, ReleaseParams params) throws GitLabApiException {

        String tagName = params.getTagName();
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new RuntimeException("params.tagName cannot be null or empty");
        }

        Response response = put(
                Response.Status.OK,
                params,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "releases",
                urlEncode(tagName));
        return (response.readEntity(Release.class));
    }

    /**
     * Delete a Release. Deleting a Release will not delete the associated tag.
     *
     * <pre>
     * <code>GitLab Endpoint: DELETE /projects/:id/releases/:tag_name</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param tagName         the tag name that the release was created from
     * @throws GitLabApiException if any exception occurs
     */
    public void deleteRelease(Object projectIdOrPath, String tagName) throws GitLabApiException {
        delete(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "releases",
                urlEncode(tagName));
    }

}
