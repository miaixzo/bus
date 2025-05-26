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

import org.miaixz.bus.gitlab.models.GitLabCiTemplate;
import org.miaixz.bus.gitlab.models.GitLabCiTemplateElement;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * This class provides an entry point to all the GitLab CI YAML API calls.
 *
 * @see <a href="https://docs.gitlab.com/ee/api/templates/gitlab_ci_ymls.html">GitLab CI YAML API</a>
 */
public class GitLabCiYamlApi extends AbstractApi {

    public GitLabCiYamlApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get all GitLab CI/CD YAML templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/gitlab_ci_ymls</code>
     * </pre>
     *
     * @return a list of Gitlab CI YAML Templates
     * @throws GitLabApiException if any exception occurs
     */
    public List<GitLabCiTemplateElement> getAllGitLabCiYamlTemplates() throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "templates", "gitlab_ci_ymls");
        return (response.readEntity(new GenericType<List<GitLabCiTemplateElement>>() {
        }));
    }

    /**
     * Get a single GitLab CI/CD YAML template.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/gitlab_ci_ymls/:key</code>
     * </pre>
     *
     * @param key The key of the GitLab CI YAML template
     * @return an Gitlab CI YAML Template
     * @throws GitLabApiException if any exception occurs
     */
    public GitLabCiTemplate getSingleGitLabCiYamlTemplate(String key) throws GitLabApiException {
        Response response = get(Status.OK, null, "templates", "gitlab_ci_ymls", key);
        return (response.readEntity(GitLabCiTemplate.class));
    }

}
