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
 * A resource loader that interprets paths as package names. This loader delegates the actual resource loading to
 * another {@link Loader} instance, converting package names (e.g., "com.example.package") into path-like strings (e.g.,
 * "com/example/package").
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PkgLoader extends DelegateLoader implements Loader {

    /**
     * Constructs a new {@code PkgLoader} with a default {@link StdLoader} as its delegate.
     */
    public PkgLoader() {
        this(new StdLoader());
    }

    /**
     * Constructs a new {@code PkgLoader} with a specified {@link ClassLoader} for its delegate {@link StdLoader}.
     *
     * @param classLoader The class loader to use for the delegate {@link StdLoader}.
     */
    public PkgLoader(ClassLoader classLoader) {
        this(new StdLoader(classLoader));
    }

    /**
     * Constructs a new {@code PkgLoader} with a specified delegate {@link Loader}.
     *
     * @param delegate The delegate loader to use for actual resource loading.
     */
    public PkgLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * Loads resources based on a package name, optionally recursively, and applies a filter. The package name is
     * converted to a path (e.g., "com.example" becomes "com/example").
     *
     * @param pkg         The package name to search for resources.
     * @param recursively Whether to load resources from subpackages recursively.
     * @param filter      The filter to apply to resources.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs during resource loading.
     */
    @Override
    public Enumeration<Resource> load(String pkg, boolean recursively, Filter filter) throws IOException {
        String path = pkg.replace(Symbol.C_DOT, Symbol.C_SLASH);
        return delegate.load(path, recursively, filter);
    }

}
