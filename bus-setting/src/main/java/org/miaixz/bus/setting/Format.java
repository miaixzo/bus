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
package org.miaixz.bus.setting;

import org.miaixz.bus.setting.metric.ini.IniElement;

import java.io.Closeable;

/**
 * An interface for an INI file line formatter. It is responsible for parsing a single line into a corresponding
 * {@link IniElement}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Format extends Closeable {

    /**
     * Formats a raw line from an INI file into an {@link IniElement}.
     *
     * @param line The line string to format.
     * @return The parsed {@link IniElement}, or null if the line should be ignored (e.g., an empty line).
     */
    IniElement formatLine(String line);

    /**
     * Resets the formatter to its initial state. This is useful when reusing the formatter to parse a new file.
     */
    void init();

    /**
     * Closes the formatter and resets its state by calling {@link #init()}.
     */
    @Override
    default void close() {
        init();
    }

}
