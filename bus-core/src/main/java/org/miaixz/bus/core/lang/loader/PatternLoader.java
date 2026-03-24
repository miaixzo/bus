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

import java.io.IOException;
import java.util.Enumeration;

import org.miaixz.bus.core.io.resource.Resource;

/**
 * An abstract resource loader that supports pattern matching for resource paths. This loader delegates the actual
 * resource loading to another {@link Loader} instance after interpreting the pattern to determine the base path,
 * recursion, and filtering logic.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class PatternLoader extends DelegateLoader implements Loader {

    /**
     * Constructs a new {@code PatternLoader} with a specified delegate {@link Loader}.
     *
     * @param delegate The delegate loader to use for actual resource loading.
     */
    protected PatternLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * Loads all resources that match the given pattern expression. The {@code recursively} parameter might be ignored
     * if the pattern itself implies recursion. Its behavior is determined by the return value of
     * {@link PatternLoader#recursively(String)}. If the subclass's pattern expression cannot express recursion, this
     * method should be overridden to meet more customized requirements. Additionally, if the {@code filter} parameter
     * is not {@code null}, the filter derived from the pattern will be combined with the {@code filter} parameter into
     * an {@link AllFilter} composite filter.
     *
     * @param pattern     The pattern expression for resources.
     * @param recursively Whether to load resources recursively (may be overridden by pattern interpretation).
     * @param filter      An additional filter to apply to resources, or {@code null} if no additional filter is needed.
     * @return An enumeration of all resources matching the pattern expression.
     * @throws IOException If an I/O error occurs during resource loading.
     */
    @Override
    public Enumeration<Resource> load(String pattern, boolean recursively, Filter filter) throws IOException {
        Filter matcher = filter(pattern);
        AllFilter allFilter = new AllFilter();
        if (null != matcher) {
            allFilter.add(matcher);
        }
        if (null != filter) {
            allFilter.add(filter);
        }
        return delegate.load(path(pattern), recursively(pattern), allFilter);
    }

    /**
     * Derives the root path for resources based on the pattern expression.
     *
     * @param pattern The pattern expression.
     * @return The root path for resources.
     */
    protected abstract String path(String pattern);

    /**
     * Determines whether to recursively load resources starting from the root path, based on the pattern expression.
     *
     * @param pattern The pattern expression.
     * @return {@code true} if resources should be loaded recursively, {@code false} otherwise.
     */
    protected abstract boolean recursively(String pattern);

    /**
     * Derives a resource filter based on the pattern expression.
     *
     * @param pattern The pattern expression.
     * @return A {@link Filter} instance derived from the pattern.
     */
    protected abstract Filter filter(String pattern);

}
