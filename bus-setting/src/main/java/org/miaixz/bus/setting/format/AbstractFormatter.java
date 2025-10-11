/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting.format;

import org.miaixz.bus.setting.metric.ini.IniElement;

/**
 * An abstract base class for formatters that convert string values into {@link IniElement} objects. All concrete
 * formatters, except for the {@code CommentFormatter} itself, typically require a {@code CommentFormatter} to handle
 * inline or trailing comments.
 *
 * @param <E> The type of {@link IniElement} this formatter produces.
 * @author Kimi Liu
 * @since Java 17+
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
