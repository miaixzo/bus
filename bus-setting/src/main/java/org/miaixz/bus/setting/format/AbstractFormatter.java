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
package org.miaixz.bus.setting.format;

import org.miaixz.bus.setting.metric.ini.IniElement;

/**
 * An abstract base class for formatters that convert string values into {@link IniElement} objects. All concrete
 * formatters, except for the {@code CommentFormatter} itself, typically require a {@code CommentFormatter} to handle
 * inline or trailing comments.
 *
 * @param <E> The type of {@link IniElement} this formatter produces.
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractFormatter<E extends IniElement> implements ElementFormatter<E> {

    /** The formatter used to parse comments. */
    private CommentFormatter commentElementFormatter;

    /**
     * Constructs an AbstractFormatter with a specific comment formatter.
     *
     * @param commentElementFormatter The formatter for parsing comments.
     */
    public AbstractFormatter(CommentFormatter commentElementFormatter) {
        this.commentElementFormatter = commentElementFormatter;
    }

    /**
     * Constructs an AbstractFormatter with a default comment formatter.
     */
    public AbstractFormatter() {
        this.commentElementFormatter = new CommentFormatter();
    }

    /**
     * Gets the current comment element formatter.
     *
     * @return The {@link CommentFormatter}.
     */
    protected CommentFormatter getCommentElementFormatter() {
        return this.commentElementFormatter;
    }

    /**
     * Sets the comment element formatter.
     *
     * @param commentElementFormatter The new {@link CommentFormatter}.
     */
    protected void setCommentElementFormatter(CommentFormatter commentElementFormatter) {
        this.commentElementFormatter = commentElementFormatter;
    }

}
