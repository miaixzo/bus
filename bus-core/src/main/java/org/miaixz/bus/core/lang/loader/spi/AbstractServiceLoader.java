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
 * @since Java 21+
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
