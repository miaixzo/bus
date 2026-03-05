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

/**
 * Converter for URI objects
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class URIConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852272395520L;

    /**
     * Converts the given value to a URI.
     * <p>
     * Supports conversion from File, URL, and string representations.
     * </p>
     *
     * @param targetClass the target class (should be URI.class)
     * @param value       the value to convert
     * @return the converted URI object, or null if conversion fails
     */
    @Override
    protected URI convertInternal(final Class<?> targetClass, final Object value) {
        try {
            if (value instanceof File) {
                return ((File) value).toURI();
            }

            if (value instanceof URL) {
                return ((URL) value).toURI();
            }
            return new URI(convertToString(value));
        } catch (final Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
