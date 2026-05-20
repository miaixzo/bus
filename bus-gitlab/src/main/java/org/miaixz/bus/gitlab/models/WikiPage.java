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
 * The wiki page class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WikiPage implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283502293L;

    private String title;
    private String content;
    private String slug;
    private String format;

    /**
     * Constructs a new {@code WikiPage} instance.
     */

    public WikiPage() {
        // No initialization required.
    }

    /**
     * Constructs a new {@code WikiPage} instance.
     *
     * @param title   the title value
     * @param slug    the slug value
     * @param content the content value
     */

    public WikiPage(String title, String slug, String content) {
        this.title = title;
        this.slug = slug;
        this.content = content;
    }

    /**
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return this.title;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
