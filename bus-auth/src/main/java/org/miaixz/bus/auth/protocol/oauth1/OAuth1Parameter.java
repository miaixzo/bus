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
package org.miaixz.bus.auth.protocol.oauth1;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Retains one decoded RFC 5849 protocol or request parameter without discarding order, duplicates, or empty values.
 *
 * @param name  decoded case-sensitive parameter name
 * @param value decoded parameter value, which may be empty
 * @author Kimi Liu
 */
public record OAuth1Parameter(String name, String value) {

    /**
     * Validates one decoded parameter without applying percent encoding prematurely.
     *
     * @throws IllegalArgumentException if name is blank or value is {@code null}
     */
    public OAuth1Parameter {
        Assert.notBlank(name, "OAuth 1.0 parameter name must not be blank");
        Assert.notNull(value, "OAuth 1.0 parameter value must not be null");
    }

    /**
     * Validates and freezes an ordered parameter list while retaining duplicates.
     *
     * @param values source parameter list
     * @return immutable ordered parameter list
     */
    static List<OAuth1Parameter> immutable(final List<OAuth1Parameter> values) {
        Assert.notNull(values, "OAuth 1.0 parameter list must not be null");
        final List<OAuth1Parameter> copy = new ArrayList<>(values.size());
        for (OAuth1Parameter value : values) {
            copy.add(Assert.notNull(value, "OAuth 1.0 parameter entry must not be null"));
        }
        return List.copyOf(copy);
    }

    /**
     * Counts exact case-sensitive occurrences of one parameter name.
     *
     * @param values validated parameter list
     * @param name   exact parameter name
     * @return number of matching occurrences
     */
    static long count(final List<OAuth1Parameter> values, final String name) {
        Assert.notNull(values, "OAuth 1.0 parameter list must not be null");
        Assert.notBlank(name, "OAuth 1.0 counted parameter name must not be blank");
        return values.stream().filter(value -> value.name().equals(name)).count();
    }

    /**
     * Reports whether an exact case-sensitive parameter name occurs at least once.
     *
     * @param values validated parameter list
     * @param name   exact parameter name
     * @return whether a matching parameter exists
     */
    static boolean contains(final List<OAuth1Parameter> values, final String name) {
        return count(values, name) != 0;
    }

    /**
     * Returns a diagnostic representation that never reveals credential, signature, nonce, or extension values.
     *
     * @return parameter name and redacted value marker
     */
    @Override
    public String toString() {
        return "OAuth1Parameter[name=" + name + ", value=[REDACTED]]";
    }

}
