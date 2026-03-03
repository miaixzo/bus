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
package org.miaixz.bus.setting.metric.toml;

import org.miaixz.bus.core.io.resource.Resource;

import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

/**
 * A utility class providing static methods for reading and writing TOML data.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Toml {

    /**
     * A {@link DateTimeFormatter} that parses and formats dates and times according to the TOML specification.
     */
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE).optionalStart().appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME).optionalStart().appendOffsetId().optionalEnd().optionalEnd()
            .toFormatter();

    /**
     * Reads and parses TOML data from a given {@link Resource}.
     *
     * @param resource The resource containing the TOML data.
     * @return A map representing the parsed TOML data.
     */
    public static Map<String, Object> read(final Resource resource) {
        return new TomlReader(resource.readString(), false).read();
    }

    /**
     * Writes the given data map to a {@link Writer} in TOML format.
     *
     * @param data   The map of data to be written.
     * @param writer The writer to which the TOML data will be written.
     */
    public static void write(final Map<String, Object> data, final Writer writer) {
        new TomlWriter(writer).write(data);
    }

}
