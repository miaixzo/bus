/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
