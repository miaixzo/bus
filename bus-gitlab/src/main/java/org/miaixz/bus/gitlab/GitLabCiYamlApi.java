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
