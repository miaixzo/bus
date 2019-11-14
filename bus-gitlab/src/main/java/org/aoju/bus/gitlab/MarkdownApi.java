package org.aoju.bus.gitlab;

import org.aoju.bus.gitlab.GitLabApi.ApiVersion;
import org.aoju.bus.gitlab.models.Markdown;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

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
     * <pre><code>GitLab Endpoint: POST /api/v4/markdown</code></pre>
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

        Form formData = new GitLabApiForm().withParam("text", text, true);
        Response response = post(Response.Status.OK, formData.asMap(), "markdown");
        return (response.readEntity(Markdown.class));
    }
}