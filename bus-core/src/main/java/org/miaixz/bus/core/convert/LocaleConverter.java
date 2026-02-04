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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.util.Locale;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Converter for {@link Locale} objects, only provides String conversion support
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LocaleConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852269079839L;

    /**
     * Converts the given value to a Locale.
     * <p>
     * Supports string format: {@code language}, {@code language_country}, or {@code language_country_variant} separated
     * by underscores.
     * </p>
     *
     * @param targetClass the target class (should be Locale.class)
     * @param value       the value to convert (typically a String)
     * @return the converted Locale object, or null if conversion fails
     */
    @Override
    protected Locale convertInternal(final Class<?> targetClass, final Object value) {
        try {
            final String text = convertToString(value);
            if (StringKit.isEmpty(text)) {
                return null;
            }

            final String[] items = text.split(Symbol.UNDERLINE);
            if (items.length == 1) {
                return Locale.forLanguageTag(items[0]);
            }
            if (items.length == 2) {
                return new Locale.Builder().setLanguage(items[0]).setRegion(items[1]).build();
            }
            return new Locale.Builder().setLanguage(items[0]).setRegion(items[1]).setVariant(items[2]).build();
        } catch (final Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
