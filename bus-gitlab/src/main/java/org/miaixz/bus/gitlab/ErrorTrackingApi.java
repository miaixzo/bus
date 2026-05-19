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

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.miaixz.bus.gitlab.models.ErrorTrackingClientKey;

/**
 * This class provides an entry point to the GitLab API error tracking.
 * <a href="https://docs.gitlab.com/api/error_tracking/">GitLab Error tracking API Documentation</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ErrorTrackingApi extends AbstractApi {

    /**
     * Constructs a new {@code ErrorTrackingApi} instance.
     *
     * @param gitLabApi the git lab api value
     */

    public ErrorTrackingApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Creates an integrated error tracking client key for a specified project.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:id/error_tracking/client_keys</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @return the created ErrorTrackingClientKey
     * @throws GitLabApiException if any exception occurs
     */
    public ErrorTrackingClientKey createClientKey(Object projectIdOrPath) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm();
        Response response = post(
                Response.Status.CREATED,
                formData,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "error_tracking",
                "client_keys");
        return (response.readEntity(ErrorTrackingClientKey.class));
    }

    /**
     * Lists all integrated error tracking client keys for a specified project.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/error_tracking/client_keys</code>
     * </pre>
     *
     * @param projectIdOrPath id, path of the project, or a Project instance holding the project ID or path
     * @return a list of ErrorTrackingClientKey
     * @throws GitLabApiException if any exception occurs
     */
    public List<ErrorTrackingClientKey> getClientKeys(Object projectIdOrPath) throws GitLabApiException {
        Response response = get(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "error_tracking",
                "client_keys");
        return (response.readEntity(new GenericType<>() {
        }));
    }

}
