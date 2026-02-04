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
package org.miaixz.bus.setting.metric.ini;

import java.util.Optional;
import java.util.function.Function;

/**
 * IniElement, like {@code sections, properties, comments}. they all can be like {@link String} .
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface IniElement extends CharSequence, java.io.Serializable {

    /**
     * this element's value. maybe a {@code toString} value like {@code comment}, a property's value like
     * {@code property} or a title value like {@code section} .
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
     * there may be comments at the end of each element. or null. if this element is comment, return null. so, nullable,
     * or see {@link #getCommentOptional}.
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
     * Get complete information. Take sec as an
     * example：{@code section.toString() + all properties.toString() + comment.toString()} In general, it is about the
     * same as {@link #toString()}.
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
     * there may be comments at the end of each element. if this element is comment, return itself.
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
     * @param valueEditor function to edit old value, {@code oldValue -> { // edit ... return newValue; }}
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
