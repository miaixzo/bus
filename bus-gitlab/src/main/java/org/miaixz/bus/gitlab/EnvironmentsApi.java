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
import java.util.Optional;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.Environment;

import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API Environments API calls.
 * 
 * @see <a href="https://docs.gitlab.com/ce/api/environments.html">Environments API</a>
 */
public class EnvironmentsApi extends AbstractApi {

    public EnvironmentsApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get all environments for a given project.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/environments</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @return a List of Environment instances
     * @throws GitLabApiException if any exception occurs
     */
    public List<Environment> getEnvironments(Object projectIdOrPath) throws GitLabApiException {
        return (getEnvironments(projectIdOrPath, getDefaultPerPage()).all());
    }

    /**
     * Get a Stream of all environments for a given project.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/environments</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @return a Stream of Environment instances
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Environment> getEnvironmentsStream(Object projectIdOrPath) throws GitLabApiException {
        return (getEnvironments(projectIdOrPath, getDefaultPerPage()).stream());
    }

    /**
     * Get a Pager of all environments for a given project.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/environments</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param itemsPerPage    the number of Environment instances that will be fetched per page
     * @return a Pager of Environment instances
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Environment> getEnvironments(Object projectIdOrPath, int itemsPerPage) throws GitLabApiException {
        return (new Pager<Environment>(this, Environment.class, itemsPerPage, null, "projects",
                getProjectIdOrPath(projectIdOrPath), "environments"));
    }

    /**
     * Get a specific environment.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/environments/:environment_id</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param environmentId   the ID of the environment to get
     * @return an Environment instance
     * @throws GitLabApiException if any exception occurs
     */
    public Environment getEnvironment(Object projectIdOrPath, Long environmentId) throws GitLabApiException {
        Response response = get(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "environments",
                environmentId);
        return (response.readEntity(Environment.class));
    }

    /**
     * Get a specific environment. as an Optional instance.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/environments/:environment_id</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param environmentId   the ID of the environment to get
     * @return the Environment as an Optional instance
     */
    public Optional<Environment> getOptionalEnvironment(Object projectIdOrPath, Long environmentId) {
        try {
            return (Optional.ofNullable(getEnvironment(projectIdOrPath, environmentId)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Create a new environment with the given name, external_url and tier.
     *
     * <pre>
     * <code>GitLab Endpoint:POST /projects/:id/environments</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param name            the name of the environment
     * @param externalUrl     the place to link to for this environment
     * @param tier            the tier of the environment
     * @return the created Environment instance
     * @throws GitLabApiException if any exception occurs
     */
    public Environment createEnvironment(Object projectIdOrPath, String name, String externalUrl, String tier)
            throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("name", name, true)
                .withParam("external_url", externalUrl).withParam("tier", tier);
        Response response = post(
                Response.Status.CREATED,
                formData,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "environments");
        return (response.readEntity(Environment.class));
    }

    /**
     * Update an existing environment.
     *
     * <pre>
     * <code>GitLab Endpoint:POST /projects/:id/environments</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param environmentId   the ID of the environment to update
     * @param name            the name of the environment
     * @param externalUrl     the place to link to for this environment
     * @param tier            the tier of the environment
     * @return the created Environment instance
     * @throws GitLabApiException if any exception occurs
     */
    public Environment updateEnvironment(
            Object projectIdOrPath,
            Long environmentId,
            String name,
            String externalUrl,
            String tier) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("name", name).withParam("external_url", externalUrl)
                .withParam("tier", tier);
        Response response = putWithFormData(
                Response.Status.OK,
                formData,
                formData,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "environments",
                environmentId);
        return (response.readEntity(Environment.class));
    }

    /**
     * Stop an environment.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:id/environments/:environment_id/stop</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param environmentId   the ID of the environment to stop
     * @return the stopped Environment instance
     * @throws GitLabApiException if any exception occurs
     */
    public Environment stopEnvironment(Object projectIdOrPath, Long environmentId) throws GitLabApiException {
        Response response = post(
                Response.Status.OK,
                (GitLabApiForm) null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "environments",
                environmentId,
                "stop");
        return (response.readEntity(Environment.class));
    }

    /**
     * Delete an environment.
     *
     * <pre>
     * <code>GitLab Endpoint: DELETE /projects/:id/environments/:environment_id</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param environmentId   the ID of the environment to delete
     * @throws GitLabApiException if any exception occurs
     */
    public void deleteEnvironment(Object projectIdOrPath, Long environmentId) throws GitLabApiException {
        delete(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "environments",
                environmentId);
    }

    /**
     * Stop an environment.
     *
     * <pre>
     * <code>GitLab Endpoint:POST /projects/:id/environments/:environment_id/stop</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @param environmentId   the ID of the environment to stop
     * @return the Environment instance of the stopped environment
     * @throws GitLabApiException if any exception occurs
     */
    public Environment createEnvironment(Object projectIdOrPath, Long environmentId) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm();
        Response response = post(
                Response.Status.CREATED,
                formData,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "environments",
                environmentId,
                "stop");
        return (response.readEntity(Environment.class));
    }

}
