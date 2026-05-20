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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The wiki attachment class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WikiAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283267221L;

    /**
     * The link class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Link implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852283337059L;

        private String url;
        private String markdown;

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
         * Returns the markdown.
         *
         * @return the result
         */

        public String getMarkdown() {
            return markdown;
        }

        /**
         * Sets the markdown.
         *
         * @param markdown the markdown value
         */

        public void setMarkdown(String markdown) {
            this.markdown = markdown;
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

    }

    private String fileName;
    private String filePath;
    private String branch;
    private Link link;

    /**
     * Returns the file name.
     *
     * @return the result
     */

    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name value
     */

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the file path.
     *
     * @return the result
     */

    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the file path.
     *
     * @param filePath the file path value
     */

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns the branch.
     *
     * @return the result
     */

    public String getBranch() {
        return branch;
    }

    /**
     * Sets the branch.
     *
     * @param branch the branch value
     */

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Returns the link.
     *
     * @return the result
     */

    public Link getLink() {
        return link;
    }

    /**
     * Sets the link.
     *
     * @param link the link value
     */

    public void setLink(Link link) {
        this.link = link;
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

}
