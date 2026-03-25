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
package org.miaixz.bus.core.text.finder;

import java.io.Serial;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;

/**
 * Character matcher finder. Finds the position of a character that satisfies the specified {@link Predicate}. This
 * class is often used to find a certain type of character, such as digits.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MatcherFinder extends TextFinder {

    @Serial
    private static final long serialVersionUID = 2852236936800L;

    /**
     * The character matcher predicate.
     */
    private final Predicate<Character> matcher;

    /**
     * Constructor.
     *
     * @param matcher The character matcher predicate to be used for searching.
     */
    public MatcherFinder(final Predicate<Character> matcher) {
        this.matcher = matcher;
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
                if (null == matcher || matcher.test(text.charAt(i))) {
                    return i;
                }
            }
        } else {
            for (int i = from; i < limit; i++) {
                if (null == matcher || matcher.test(text.charAt(i))) {
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
