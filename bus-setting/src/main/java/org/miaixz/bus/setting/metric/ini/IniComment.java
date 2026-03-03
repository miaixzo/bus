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
package org.miaixz.bus.setting.metric.ini;

/**
 * Ini file's comment.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface IniComment extends IniElement {

    /**
     * there may be comments at the end of each element. or null. if this element is comment, return itself. so,
     * nullable, or see {@link #getCommentOptional}.
     *
     * @return comment end of the element or null. if element, return itself.
     * @see #getCommentOptional()
     */
    @Override
    default IniComment getComment() {
        return null;
    }

    /**
     * clear comment (if exists).
     */
    @Override
    default void clearComment() {
    }

    /**
     * like {@link #toString()}, without comment value(if exists). comment to no comment string? no, return original
     * value.
     *
     * @return to string value without comment value.
     */
    @Override
    default String toNoCommentString() {
        return getOriginalValue();
    }

}
