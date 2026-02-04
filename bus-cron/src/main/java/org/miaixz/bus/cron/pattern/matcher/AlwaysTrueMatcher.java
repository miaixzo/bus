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
package org.miaixz.bus.cron.pattern.matcher;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * A matcher that always returns {@code true}, representing the wildcard ('*') character in a cron expression.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlwaysTrueMatcher implements PartMatcher {

    /**
     * Singleton instance.
     */
    public static final AlwaysTrueMatcher INSTANCE = new AlwaysTrueMatcher();

    /**
     * Always returns {@code true}, indicating that any value matches.
     *
     * @param value The value to test (ignored).
     * @return Always {@code true}.
     */
    @Override
    public boolean test(final Integer value) {
        return true;
    }

    /**
     * Returns the input value itself, as every value is considered a match.
     *
     * @param value The value to find the next match after.
     * @return The input {@code value}.
     */
    @Override
    public int nextAfter(final int value) {
        return value;
    }

    @Override
    public String toString() {
        return StringKit.format("[Matcher]: always true.");
    }

}
