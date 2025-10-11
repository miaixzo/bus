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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.cache.SimpleCache;
import org.miaixz.bus.core.io.resource.MultiResource;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * A list-based service loader, intended as a replacement for the JDK's {@link java.util.ServiceLoader}. Compared to the
 * JDK version, this implementation offers several enhancements:
 * <ul>
 * <li>Optional service storage locations (defaults to {@code META-INF/services/}).</li>
 * <li>Customizable character encoding.</li>
 * <li>Ability to load specific service instances by name.</li>
 * <li>Ability to load specific service classes, allowing user-controlled instantiation (e.g., with custom constructor
 * arguments).</li>
 * <li>A more flexible loading mechanism that avoids loading unnecessary services when a specific one is requested.</li>
 * </ul>
 * <p>
 * Service files are located by default under {@code META-INF/services/}, with the file name being the fully qualified
 * name of the service interface class. The content is a list of implementation class names:
 * 
 * <pre>
 *     # This is a comment
 *     com.example.Service1
 *     com.example.Service2
 * </pre>
 * 
 * Services can be retrieved by their index using the {@link #getService(int)} method.
 *
 * @param <S> The type of the service.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListServiceLoader<S> extends AbstractServiceLoader<S> {

    /**
     * A list of fully qualified service class names.
     */
    private final List<String> serviceNames;
    /**
     * Cache for service instances, mapping class names to service objects.
     */
    private final SimpleCache<String, S> serviceCache;

    /**
     * Constructs a new {@code ListServiceLoader}.
     *
     * @param pathPrefix   The path prefix for the service files.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @param charset      The character set to use for reading the service files, defaults to UTF-8.
     */
    public ListServiceLoader(final String pathPrefix, final Class<S> serviceClass, final ClassLoader classLoader,
            final Charset charset) {
        super(pathPrefix, serviceClass, classLoader, charset);
        this.serviceNames = new ArrayList<>();
        this.serviceCache = new SimpleCache<>(new HashMap<>());
        load();
    }

    /**
     * Creates a new {@code ListServiceLoader} with the default path prefix.
     *
     * @param <S>          The type of the service.
     * @param serviceClass The service interface class.
     * @return A new {@code ListServiceLoader} instance.
     */
    public static <S> ListServiceLoader<S> of(final Class<S> serviceClass) {
        return of(serviceClass, null);
    }

    /**
     * Creates a new {@code ListServiceLoader} with the default path prefix and a specified class loader.
     *
     * @param <S>          The type of the service.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @return A new {@code ListServiceLoader} instance.
     */
    public static <S> ListServiceLoader<S> of(final Class<S> serviceClass, final ClassLoader classLoader) {
        return of(Normal.META_INF_SERVICES, serviceClass, classLoader);
    }

    /**
     * Creates a new {@code ListServiceLoader} with a specified path prefix and class loader.
     *
     * @param <S>          The type of the service.
     * @param pathPrefix   The path prefix for the service files.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @return A new {@code ListServiceLoader} instance.
     */
    public static <S> ListServiceLoader<S> of(
            final String pathPrefix,
            final Class<S> serviceClass,
            final ClassLoader classLoader) {
        return new ListServiceLoader<>(pathPrefix, serviceClass, classLoader, null);
    }

    @Override
    public void load() {
        final MultiResource resources = ResourceKit.getResources(pathPrefix + serviceClass.getName(), this.classLoader);
        for (final Resource resource : resources) {
            parse(resource);
        }
    }

    @Override
    public int size() {
        return this.serviceNames.size();
    }

    @Override
    public List<String> getServiceNames() {
        return ListKit.view(this.serviceNames);
    }

    /**
     * Gets the implementation class for the service at the specified index.
     *
     * @param index The index of the service.
     * @return The implementation class corresponding to the service name.
     */
    public Class<S> getServiceClass(final int index) {
        final String serviceClassName = this.serviceNames.get(index);
        if (StringKit.isBlank(serviceClassName)) {
            return null;
        }
        return getServiceClass(serviceClassName);
    }

    @Override
    public Class<S> getServiceClass(final String serviceName) {
        return ClassKit.loadClass(serviceName);
    }

    /**
     * Gets the service at the specified index, using a cache. Multiple calls will return the same service object.
     *
     * @param index The index of the service.
     * @return The service object.
     */
    public S getService(final int index) {
        final String serviceClassName = this.serviceNames.get(index);
        if (null == serviceClassName) {
            return null;
        }
        return getService(serviceClassName);
    }

    @Override
    public S getService(final String serviceName) {
        return this.serviceCache.get(serviceName, () -> createService(serviceName));
    }

    @Override
    public Iterator<S> iterator() {
        return new Iterator<>() {

            /**
             * Iterator for service names.
             */
            private final Iterator<String> nameIter = serviceNames.iterator();

            @Override
            public boolean hasNext() {
                return nameIter.hasNext();
            }

            @Override
            public S next() {
                return getService(nameIter.next());
            }
        };
    }

    /**
     * Parses a single resource file.
     *
     * @param resource The resource to parse.
     */
    private void parse(final Resource resource) {
        try (final BufferedReader reader = resource.getReader(this.charset)) {
            int lc = 1;
            while (lc >= 0) {
                lc = parseLine(resource, reader, lc);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Parses a single line from a service file.
     *
     * @param resource The resource being parsed.
     * @param reader   The {@link BufferedReader} for the resource.
     * @param lineNo   The current line number.
     * @return The next line number, or -1 if the end of the file is reached.
     * @throws IOException If an I/O error occurs.
     */
    private int parseLine(final Resource resource, final BufferedReader reader, final int lineNo) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return -1; // End of file
        }
        final int ci = line.indexOf(Symbol.C_HASH);
        if (ci >= 0) {
            // Strip comments
            line = line.substring(0, ci);
        }
        line = StringKit.trim(line);
        if (!line.isEmpty()) {
            checkLine(resource, lineNo, line);
            // In non-override mode, add only if not already present.
            if (!serviceCache.containsKey(line) && !this.serviceNames.contains(line)) {
                this.serviceNames.add(line);
            }
        }
        return lineNo + 1;
    }

    /**
     * Checks the syntax of a service provider class name.
     *
     * @param resource The resource being parsed.
     * @param lineNo   The current line number.
     * @param line     The line content.
     */
    private void checkLine(final Resource resource, final int lineNo, final String line) {
        if (StringKit.containsBlank(line)) {
            fail(resource, lineNo, "Illegal configuration-file syntax");
        }
        int cp = line.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) {
            fail(resource, lineNo, "Illegal provider-class name: " + line);
        }
        final int n = line.length();
        for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
            cp = line.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                fail(resource, lineNo, "Illegal provider-class name: " + line);
            }
        }
    }

    /**
     * Throws an {@link InternalException} with a formatted error message.
     *
     * @param resource The resource where the error occurred.
     * @param lineNo   The line number of the error.
     * @param msg      The error message.
     */
    private void fail(final Resource resource, final int lineNo, final String msg) {
        throw new InternalException(
                this.serviceClass + Symbol.COLON + resource.getUrl() + Symbol.COLON + lineNo + ": " + msg);
    }

    /**
     * Creates a new service instance without caching.
     *
     * @param serviceClassName The fully qualified class name of the service.
     * @return The new service object.
     */
    private S createService(final String serviceClassName) {
        return ReflectKit.newInstance(ClassKit.loadClass(serviceClassName));
    }

}
