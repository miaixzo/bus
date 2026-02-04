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
package org.miaixz.bus.core.text.finder;

import java.io.Serial;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CharKit;

/**
 * Character finder. Finds the position information of a specified character within a string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharFinder extends TextFinder {

    @Serial
    private static final long serialVersionUID = 2852236682062L;

    /**
     * The character to be searched.
     */
    private final char c;
    /**
     * Whether to ignore case during the search.
     */
    private final boolean caseInsensitive;

    /**
     * Constructor, does not ignore character case.
     *
     * @param c The character to be searched.
     */
    public CharFinder(final char c) {
        this(c, false);
    }

    /**
     * Constructor.
     *
     * @param c               The character to be searched.
     * @param caseInsensitive Whether to ignore case.
     */
    public CharFinder(final char c, final boolean caseInsensitive) {
        this.c = c;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Start method.
     *
     * @return the int value
     */
    @Override
    public int start(final int from) {
        Assert.notNull(this.text, "Text to find must be not null!");
        final int limit = getValidEndIndex();
        if (negative) {
            for (int i = from; i > limit; i--) {
                if (CharKit.equals(c, text.charAt(i), caseInsensitive)) {
                    return i;
                }
            }
        } else {
            for (int i = from; i < limit; i++) {
                if (CharKit.equals(c, text.charAt(i), caseInsensitive)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * End method.
     *
     * @return the int value
     */
    @Override
    public int end(final int start) {
        if (start < 0) {
            return -1;
        }
        return start + 1;
    }

}
