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
import org.miaixz.bus.core.lang.Symbol;

/**
 * An ANT-style path resource loader. This loader supports ANT-style path expressions for loading resources.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AntLoader extends PatternLoader implements Loader {

    /**
     * Constructs a new {@code AntLoader} with a default {@link StdLoader} as its delegate.
     */
    public AntLoader() {
        this(new StdLoader());
    }

    /**
     * Constructs a new {@code AntLoader} with a specified {@link ClassLoader} for its delegate {@link StdLoader}.
     *
     * @param classLoader The class loader to use for the delegate {@link StdLoader}.
     */
    public AntLoader(ClassLoader classLoader) {
        this(new StdLoader(classLoader));
    }

    /**
     * Constructs a new {@code AntLoader} with a specified delegate {@link Loader}.
     *
     * @param delegate The delegate loader to use for actual resource loading.
     */
    public AntLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * Load method.
     *
     * @return the Enumeration&lt;Resource&gt; value
     */
    @Override
    public Enumeration<Resource> load(String pattern, boolean recursively, Filter filter) throws IOException {
        if (Math.max(pattern.indexOf(Symbol.C_STAR), pattern.indexOf(Symbol.C_QUESTION_MARK)) < 0) {
            // If no wildcard characters are present, delegate to the base loader without pattern matching.
            return delegate.load(pattern, recursively, filter);
        } else {
            // Otherwise, use pattern matching provided by the superclass (PatternLoader).
            return super.load(pattern, recursively, filter);
        }
    }

    /**
     * Extracts the base path from an ANT-style path expression.
     *
     * @param ant The ANT-style path expression.
     * @return The base path without wildcard characters.
     */
    @Override
    protected String path(String ant) {
        int index = Integer.MAX_VALUE - 1;
        if (ant.contains(Symbol.STAR) && ant.indexOf(Symbol.C_STAR) < index)
            index = ant.indexOf(Symbol.C_STAR);
        if (ant.contains(Symbol.QUESTION_MARK) && ant.indexOf(Symbol.C_QUESTION_MARK) < index)
            index = ant.indexOf(Symbol.C_QUESTION_MARK);
        return ant.substring(0, ant.lastIndexOf(Symbol.C_SLASH, index) + 1);
    }

    /**
     * Determines whether to recursively load resources based on the ANT-style path expression. For {@code AntLoader},
     * this method always returns {@code true} to enable recursive loading by default when wildcards are present.
     *
     * @param ant The ANT-style path expression.
     * @return Always {@code true}.
     */
    @Override
    protected boolean recursively(String ant) {
        return true;
    }

    /**
     * Creates an {@link AntFilter} based on the provided ANT-style path expression.
     *
     * @param ant The ANT-style path expression.
     * @return A new {@link AntFilter} instance.
     */
    @Override
    protected Filter filter(String ant) {
        return new AntFilter(ant);
    }

}
