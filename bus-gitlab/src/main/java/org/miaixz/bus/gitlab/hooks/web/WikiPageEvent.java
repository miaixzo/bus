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
package org.miaixz.bus.gitlab.hooks.web;

import java.io.Serial;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The wiki page event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WikiPageEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852233220608L;
    /**
     * The x gitlab event value.
     */

    public static final String X_GITLAB_EVENT = "Wiki Page Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "wiki_page";

    private EventUser user;
    private EventProject project;
    private Wiki wiki;
    private ObjectAttributes objectAttributes;

    /**
     * Returns the object kind.
     *
     * @return the result
     */

    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    /**
     * Sets the object kind.
     *
     * @param objectKind the object kind value
     */

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    /**
     * Returns the user.
     *
     * @return the result
     */

    public EventUser getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(EventUser user) {
        this.user = user;
    }

    /**
     * Returns the project.
     *
     * @return the result
     */

    public EventProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(EventProject project) {
        this.project = project;
    }

    /**
     * Returns the wiki.
     *
     * @return the result
     */

    public Wiki getWiki() {
        return wiki;
    }

    /**
     * Sets the wiki.
     *
     * @param wiki the wiki value
     */

    public void setWiki(Wiki wiki) {
        this.wiki = wiki;
    }

    /**
     * Returns the object attributes.
     *
     * @return the result
     */

    public ObjectAttributes getObjectAttributes() {
        return this.objectAttributes;
    }

    /**
     * Sets the object attributes.
     *
     * @param objectAttributes the object attributes value
     */

    public void setObjectAttributes(ObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    /**
     * The wiki class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Wiki {

        private String webUrl;
        private String git_http_url;
        private String git_ssh_url;
        private String pathWithNamespace;
        private String defaultBranch;

        /**
         * Returns the web url.
         *
         * @return the result
         */

        public String getWebUrl() {
            return webUrl;
        }

        /**
         * Sets the web url.
         *
         * @param webUrl the web url value
         */

        public void setWebUrl(String webUrl) {
            this.webUrl = webUrl;
        }

        /**
         * Returns the git http url.
         *
         * @return the result
         */

        public String getGit_http_url() {
            return git_http_url;
        }

        /**
         * Sets the git http url.
         *
         * @param git_http_url the git http url value
         */

        public void setGit_http_url(String git_http_url) {
            this.git_http_url = git_http_url;
        }

        /**
         * Returns the git ssh url.
         *
         * @return the result
         */

        public String getGit_ssh_url() {
            return git_ssh_url;
        }

        /**
         * Sets the git ssh url.
         *
         * @param git_ssh_url the git ssh url value
         */

        public void setGit_ssh_url(String git_ssh_url) {
            this.git_ssh_url = git_ssh_url;
        }

        /**
         * Returns the path with namespace.
         *
         * @return the result
         */

        public String getPathWithNamespace() {
            return pathWithNamespace;
        }

        /**
         * Sets the path with namespace.
         *
         * @param pathWithNamespace the path with namespace value
         */

        public void setPathWithNamespace(String pathWithNamespace) {
            this.pathWithNamespace = pathWithNamespace;
        }

        /**
         * Returns the default branch.
         *
         * @return the result
         */

        public String getDefaultBranch() {
            return defaultBranch;
        }

        /**
         * Sets the default branch.
         *
         * @param defaultBranch the default branch value
         */

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }

    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * The object attributes class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ObjectAttributes {

        private String title;
        private String content;
        private String format;
        private String message;
        private String slug;
        private String url;
        private String action;
        private String diffUrl;
        private String versionId;

        /**
         * Returns the title.
         *
         * @return the result
         */

        public String getTitle() {
            return title;
        }

        /**
         * Sets the title.
         *
         * @param title the title value
         */

        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * Returns the content.
         *
         * @return the result
         */

        public String getContent() {
            return content;
        }

        /**
         * Sets the content.
         *
         * @param content the content value
         */

        public void setContent(String content) {
            this.content = content;
        }

        /**
         * Returns the format.
         *
         * @return the result
         */

        public String getFormat() {
            return format;
        }

        /**
         * Sets the format.
         *
         * @param format the format value
         */

        public void setFormat(String format) {
            this.format = format;
        }

        /**
         * Returns the message.
         *
         * @return the result
         */

        public String getMessage() {
            return message;
        }

        /**
         * Sets the message.
         *
         * @param message the message value
         */

        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * Returns the slug.
         *
         * @return the result
         */

        public String getSlug() {
            return slug;
        }

        /**
         * Sets the slug.
         *
         * @param slug the slug value
         */

        public void setSlug(String slug) {
            this.slug = slug;
        }

        /**
         * Returns the action.
         *
         * @return the result
         */

        public String getAction() {
            return action;
        }

        /**
         * Sets the action.
         *
         * @param action the action value
         */

        public void setAction(String action) {
            this.action = action;
        }

        /**
         * Returns the url.
         *
         * @return the result
         */

        public String getUrl() {
            return url;
        }

        /**
         * Sets the url.
         *
         * @param url the url value
         */

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns the diff url.
         *
         * @return the result
         */

        public String getDiffUrl() {
            return diffUrl;
        }

        /**
         * Sets the diff url.
         *
         * @param diffUrl the diff url value
         */

        public void setDiffUrl(String diffUrl) {
            this.diffUrl = diffUrl;
        }

        /**
         * Returns the version id.
         *
         * @return the result
         */

        public String getVersionId() {
            return versionId;
        }

        /**
         * Sets the version id.
         *
         * @param versionId the version id value
         */

        public void setVersionId(String versionId) {
            this.versionId = versionId;
        }

    }

}
