/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting.metric.ini;

import java.util.Optional;
import java.util.function.Function;

/**
 * IniElement, like {@code sections, properties, comments}.
 * they all can be like {@link String} .
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface IniElement extends CharSequence, java.io.Serializable {

    /**
     * this element's value.
     * maybe a {@code toString} value like {@code comment},
     * a property's value like {@code property} or a title value like {@code section} .
     *
     * @return some value
     */
    String value();

    /**
     * change this element's value.
     *
     * @param newValue a new value
     * @return old value
     * @see #value()
     */
    String setValue(String newValue);

    /**
     * there may be comments at the end of each element. or null.
     * if this element is comment, return null.
     * so, nullable, or see {@link #getCommentOptional}.
     *
     * @return comment end of the element or null. if element, return null.
     * @see #getCommentOptional()
     */
    IniComment getComment();

    /**
     * clear comment (if exists).
     */
    void clearComment();

    /**
     * like {@link #toString()}, without comment value(if exists).
     *
     * @return to string value without comment value.
     */
    String toNoCommentString();

    /**
     * Get complete information.
     * Take sec as an example：{@code section.toString() + all properties.toString() + comment.toString()}
     * In general, it is about the same as {@link #toString()}.
     *
     * @return the string
     */
    String toCompleteString();

    /**
     * need to override toString method, to show complete information.
     *
     * @return to string value.
     */
    @Override
    String toString();

    /**
     * get the original string.
     *
     * @return original string value.
     */
    String getOriginalValue();

    /**
     * the line number where you are.
     *
     * @return line number.
     */
    int line();

    /**
     * there may be comments at the end of each element.
     * if this element is comment, return itself.
     *
     * @return comment end of the element. if element, return itself.
     * @see #getComment()
     */
    default Optional<IniComment> getCommentOptional() {
        return Optional.ofNullable(getComment());
    }

    /**
     * Edit the value of this element on the basis of original value .
     *
     * @param valueEditor function to edit old value, {@code oldValue -> {
     *                    // edit ...
     *                    return newValue;
     *                    }}
     * @return old value
     */
    default String setValue(Function<String, String> valueEditor) {
        return setValue(valueEditor.apply(value()));
    }

    /**
     * Am I comment?
     *
     * @return is it a comment?
     */
    default boolean isComment() {
        return this instanceof IniComment;
    }

    /**
     * Am I property?
     *
     * @return is it a property?
     */
    default boolean isProperty() {
        return this instanceof IniProperty;
    }

    /**
     * Am I section?
     *
     * @return is it a section?
     */
    default boolean isSection() {
        return this instanceof IniSection;
    }

}
