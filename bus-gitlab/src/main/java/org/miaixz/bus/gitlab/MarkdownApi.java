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

import org.miaixz.bus.gitlab.GitLabApi.ApiVersion;
import org.miaixz.bus.gitlab.models.Markdown;
import org.miaixz.bus.gitlab.models.MarkdownRequest;

import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API markdown calls.
 */
public class MarkdownApi extends AbstractApi {

    public MarkdownApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Render an arbitrary Markdown document.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /api/v4/markdown</code>
     * </pre>
     *
     * @param text text to be transformed
     * @return a Markdown instance with transformed info
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 11.0
     */
    public Markdown getMarkdown(String text) throws GitLabApiException {

        if (!isApiVersion(ApiVersion.V4)) {
            throw new GitLabApiException("Api version must be v4");
        }

        return getMarkdown(new MarkdownRequest(text, true));
    }

    /**
     * Render an arbitrary Markdown document.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /api/v4/markdown</code>
     * </pre>
     *
     * @param markdownRequest a request of markdown transformation
     * @return a Markdown instance with transformed info
     * @throws GitLabApiException if any exception occurs
     * @since GitLab 11.0
     */
    public Markdown getMarkdown(MarkdownRequest markdownRequest) throws GitLabApiException {

        if (!isApiVersion(ApiVersion.V4)) {
            throw new GitLabApiException("Api version must be v4");
        }

        Response response = post(Response.Status.OK, markdownRequest, "markdown");
        return (response.readEntity(Markdown.class));
    }

}
