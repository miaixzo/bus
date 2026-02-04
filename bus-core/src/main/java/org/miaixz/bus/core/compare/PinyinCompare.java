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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.util.Locale;

/**
 * A comparator for sorting Chinese character strings in Pinyin order (based on GBK).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinyinCompare extends LocaleCompare {

    @Serial
    private static final long serialVersionUID = 2852262615223L;

    /**
     * Constructs a new {@code PinyinCompare}, with {@code null} values placed at the end.
     */
    public PinyinCompare() {
        this(true);
    }

    /**
     * Constructs a new {@code PinyinCompare}.
     *
     * @param nullGreater whether {@code null} values should be placed at the end.
     */
    public PinyinCompare(final boolean nullGreater) {
        super(nullGreater, Locale.CHINESE);
    }

}
