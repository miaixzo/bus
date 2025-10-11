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
package org.miaixz.bus.core.lang.loader.spi;

import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Abstract base class for service loaders, providing common properties such as path prefix, service class, class
 * loader, and character set.
 *
 * @param <S> The type of the service.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractServiceLoader<S> implements ServiceLoader<S> {

    /**
     * The path prefix for the service files.
     */
    protected final String pathPrefix;
    /**
     * The service interface class.
     */
    protected final Class<S> serviceClass;
    /**
     * The class loader to use.
     */
    protected final ClassLoader classLoader;
    /**
     * The character set for reading service files.
     */
    protected final Charset charset;

    /**
     * Constructs a new abstract service loader.
     *
     * @param pathPrefix   The path prefix for the service files.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @param charset      The character set to use for reading the service files, defaults to UTF-8.
     */
    public AbstractServiceLoader(final String pathPrefix, final Class<S> serviceClass, final ClassLoader classLoader,
            final Charset charset) {
        this.pathPrefix = StringKit.addSuffixIfNot(pathPrefix, Symbol.SLASH);
        this.serviceClass = serviceClass;
        this.classLoader = classLoader;
        this.charset = charset;
    }

}
