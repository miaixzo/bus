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
     */
    public Markdown getMarkdown(MarkdownRequest markdownRequest) throws GitLabApiException {
        if (!isApiVersion(ApiVersion.V4)) {
            throw new GitLabApiException("Api version must be v4");
        }

        Response response = post(Response.Status.OK, markdownRequest, "markdown");
        return (response.readEntity(Markdown.class));
    }

}