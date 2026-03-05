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

import org.miaixz.bus.gitlab.models.Commit;

import jakarta.ws.rs.core.Response;

/**
 * <p>
 * This class provides an entry point to all the GitLab API repository submodules calls. For more information on the
 * repository APIs see:
 * </p>
 *
 * @see <a href="https://docs.gitlab.com/ee/api/repository_submodules.html">Repository Submodules API</a>
 */
public class RepositorySubmodulesApi extends AbstractApi {

    public RepositorySubmodulesApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Update existing submodule reference in repository.
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /projects/:id/repository/submodules/:submodule</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param submodule       full path to the submodule
     * @param branch          name of the branch to commit into
     * @param commitSha       full commit SHA to update the submodule to
     * @param commitMessage   commit message (optional). If no message is provided, a default is set
     * @return the created commit
     * @throws GitLabApiException if any exception occurs
     */
    public Commit updateExistingSubmoduleReference(
            Object projectIdOrPath,
            String submodule,
            String branch,
            String commitSha,
            String commitMessage) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("branch", branch, true)
                .withParam("commit_sha", commitSha, true).withParam("commit_message", commitMessage);
        Response response = put(
                Response.Status.OK,
                formData.asMap(),
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "repository",
                "submodules",
                urlEncode(submodule));
        return (response.readEntity(Commit.class));
    }

}
