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
package org.miaixz.bus.fabric.protocol.http.agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Shared helpers for User-Agent classifiers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class AgentRules {

    /**
     * Prevents instantiation of this static classifier helper.
     */
    private AgentRules() {
        // No initialization required.
    }

    /**
     * Compiles a case-insensitive rule.
     *
     * @param rule regular expression text, or {@code null} to disable the rule
     * @return cached case-insensitive pattern, or {@code null} for a disabled rule
     */
    static Pattern compile(final String rule) {
        return rule == null ? null : org.miaixz.bus.core.center.regex.Pattern.get(rule, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Returns whether a pattern is found in text.
     *
     * @param pattern compiled search pattern, or {@code null}
     * @param text    User-Agent text to search, or {@code null}
     * @return {@code true} when both inputs exist and any substring matches
     */
    static boolean contains(final Pattern pattern, final String text) {
        return pattern != null && text != null && pattern.matcher(text).find();
    }

    /**
     * Returns group 1 from the first match.
     *
     * @param pattern compiled search pattern containing a first capture group, or {@code null}
     * @param text    User-Agent text to search, or {@code null}
     * @return first capture from the first match, or {@code null} when inputs are absent or no match exists
     * @throws IndexOutOfBoundsException if a matching pattern defines no first capture group
     */
    static String group1(final Pattern pattern, final String text) {
        if (pattern == null || text == null) {
            return null;
        }
        final Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Validates a component name.
     *
     * @param name classifier component name to validate
     * @return unchanged non-blank component name
     * @throws ValidateException if {@code name} is blank
     */
    static String name(final String name) {
        Assert.isFalse(StringKit.isBlank(name), () -> new ValidateException("Agent component name must be non-blank"));
        return name;
    }

}
