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

/**
 * The markdown request class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MarkdownRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852261307076L;

    private String text;
    private boolean gfm;
    private String project;

    /**
     * Constructs a new {@code MarkdownRequest} instance.
     *
     * @param text the text value
     * @param gfm  the gfm value
     */

    public MarkdownRequest(String text, boolean gfm) {
        this.text = text;
        this.gfm = gfm;
    }

    /**
     * Constructs a new {@code MarkdownRequest} instance.
     *
     * @param text    the text value
     * @param gfm     the gfm value
     * @param project the project value
     */

    public MarkdownRequest(String text, boolean gfm, String project) {
        this.text = text;
        this.gfm = gfm;
        this.project = project;
    }

    /**
     * Returns the text.
     *
     * @return the result
     */

    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     *
     * @param text the text value
     */

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns whether the gfm is enabled.
     *
     * @return the result
     */

    public boolean isGfm() {
        return gfm;
    }

    /**
     * Sets the gfm.
     *
     * @param gfm the gfm value
     */

    public void setGfm(boolean gfm) {
        this.gfm = gfm;
    }

    /**
     * Returns the project.
     *
     * @return the result
     */

    public String getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(String project) {
        this.project = project;
    }

}
