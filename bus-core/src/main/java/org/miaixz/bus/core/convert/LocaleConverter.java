/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
