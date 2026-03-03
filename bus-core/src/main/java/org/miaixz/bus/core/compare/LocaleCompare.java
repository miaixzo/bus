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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.text.Collator;
import java.util.Locale;

/**
 * A {@code Comparator} for {@link String} that considers a specific {@link Locale}. This is useful for building search
 * and sorting routines for natural language text.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LocaleCompare extends NullCompare<String> {

    @Serial
    private static final long serialVersionUID = 2852261895676L;

    /**
     * Constructs a new {@code LocaleCompare}.
     *
     * @param nullGreater   whether {@code null} values should be placed at the end.
     * @param desiredLocale the {@link Locale} to use for comparison.
     */
    public LocaleCompare(final boolean nullGreater, final Locale desiredLocale) {
        super(nullGreater, Collator.getInstance(desiredLocale));
    }

}
