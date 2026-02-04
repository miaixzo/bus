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
package org.miaixz.bus.setting.format;

import org.miaixz.bus.setting.metric.ini.IniElement;

/**
 * An interface for formatters that convert a string value from a configuration file into a specific {@link IniElement}
 * type.
 *
 * @param <E> The type of {@link IniElement} this formatter produces.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ElementFormatter<E extends IniElement> {

    /**
     * Checks if this formatter can handle the given string value.
     *
     * @param value The string value to check.
     * @return {@code true} if this formatter can process the value, {@code false} otherwise.
     */
    boolean check(String value);

    /**
     * Formats the string value into an element of type {@code E}. This method assumes that {@link #check(String)} has
     * already returned true for the given value.
     *
     * @param value A string value that this formatter can handle.
     * @param line  The line number where the value originated.
     * @return The formatted {@link IniElement}, which should not be null.
     */
    E format(String value, int line);

}
