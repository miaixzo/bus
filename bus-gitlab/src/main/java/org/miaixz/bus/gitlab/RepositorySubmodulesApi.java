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
    public Commit updateExistingSubmoduleReference(Object projectIdOrPath, String submodule, String branch,
            String commitSha, String commitMessage) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("branch", branch, true)
                .withParam("commit_sha", commitSha, true).withParam("commit_message", commitMessage);
        Response response = put(Response.Status.OK, formData.asMap(), "projects", getProjectIdOrPath(projectIdOrPath),
                "repository", "submodules", urlEncode(submodule));
        return (response.readEntity(Commit.class));
    }

}
