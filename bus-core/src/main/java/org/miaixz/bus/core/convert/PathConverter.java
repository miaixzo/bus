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
package org.miaixz.bus.core.convert;

import java.io.File;
import java.io.Serial;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path string converter.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PathConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852270693527L;

    /**
     * Converts the given value to a Path.
     * <p>
     * Supports conversion from URI, URL, File, and String.
     * </p>
     *
     * @param targetClass the target class (should be Path.class)
     * @param value       the value to convert
     * @return the converted Path object, or null if conversion fails
     */
    @Override
    protected Path convertInternal(final Class<?> targetClass, final Object value) {
        try {
            if (value instanceof URI) {
                return Paths.get((URI) value);
            }

            if (value instanceof URL) {
                return Paths.get(((URL) value).toURI());
            }

            if (value instanceof File) {
                return ((File) value).toPath();
            }

            return Paths.get(convertToString(value));
        } catch (final Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
