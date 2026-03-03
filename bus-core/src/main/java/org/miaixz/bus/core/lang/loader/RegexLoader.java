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
package org.miaixz.bus.core.lang.loader;

import org.miaixz.bus.core.lang.Normal;

/**
 * A resource loader that uses regular expressions to match resource paths. This loader delegates the actual resource
 * loading to another {@link Loader} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexLoader extends PatternLoader implements Loader {

    /**
     * Constructs a new {@code RegexLoader} with a default {@link StdLoader} as its delegate.
     */
    public RegexLoader() {
        this(new StdLoader());
    }

    /**
     * Constructs a new {@code RegexLoader} with a specified {@link ClassLoader} for its delegate {@link StdLoader}.
     *
     * @param classLoader The class loader to use for the delegate {@link StdLoader}.
     */
    public RegexLoader(ClassLoader classLoader) {
        this(new StdLoader(classLoader));
    }

    /**
     * Constructs a new {@code RegexLoader} with a specified delegate {@link Loader}.
     *
     * @param delegate The delegate loader to use for actual resource loading.
     */
    public RegexLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * Returns an empty string as the base path for regular expression patterns. The regular expression itself defines
     * the path matching logic.
     *
     * @param pattern The regular expression pattern.
     * @return An empty string.
     */
    @Override
    protected String path(String pattern) {
        return Normal.EMPTY;
    }

    /**
     * Determines whether to recursively load resources based on the regular expression pattern. For
     * {@code RegexLoader}, this method always returns {@code true} to allow recursive matching by default.
     *
     * @param pattern The regular expression pattern.
     * @return Always {@code true}.
     */
    @Override
    protected boolean recursively(String pattern) {
        return true;
    }

    /**
     * Creates a {@link RegexFilter} based on the provided regular expression pattern.
     *
     * @param pattern The regular expression pattern.
     * @return A new {@link RegexFilter} instance.
     */
    @Override
    protected Filter filter(String pattern) {
        return new RegexFilter(pattern);
    }

}
