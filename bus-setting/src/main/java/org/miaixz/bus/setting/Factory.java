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

import org.miaixz.bus.setting.format.ElementFormatter;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniProperty;
import org.miaixz.bus.setting.metric.ini.IniSection;

/**
 * A functional interface for creating an INI {@link Format} instance. It provides the necessary element formatters for
 * comments, sections, and properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Factory {

    /**
     * Applies the given element formatters to create a new {@link Format} instance.
     *
     * @param commentElementFormatter  a formatter for comment lines.
     * @param sectionElementFormatter  a formatter for section headers.
     * @param propertyElementFormatter a formatter for property lines (key-value pairs).
     * @return a new {@link Format} instance.
     */
    Format apply(
            ElementFormatter<IniComment> commentElementFormatter,
            ElementFormatter<IniSection> sectionElementFormatter,
            ElementFormatter<IniProperty> propertyElementFormatter);

}
