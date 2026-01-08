/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.safety.boot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;
import org.springframework.boot.loader.launch.LaunchedClassLoader;

/**
 * A custom {@link LaunchedClassLoader} that supports decryption of resources within a Spring Boot JAR. It intercepts
 * resource loading to apply decryption using provided {@link DecryptorProvider} and {@link Key}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootClassLoader extends LaunchedClassLoader {

    static {
        // Registers this class loader as parallel-capable, allowing concurrent loading of classes.
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * The custom URL handler used by this class loader to manage encrypted resources.
     */
    private final BootURLHandler bootURLHandler;

    /**
     * Constructs a new {@code BootClassLoader}.
     *
     * @param urls              The URLs from which to load classes and resources.
     * @param parent            The parent class loader.
     * @param decryptorProvider The provider responsible for decrypting resources.
     * @param encryptorProvider The provider responsible for encrypting resources (though primarily used for decryption
     *                          context here).
     * @param key               The cryptographic key used for decryption.
     * @throws Exception If an error occurs during initialization.
     */
    public BootClassLoader(URL[] urls, ClassLoader parent, DecryptorProvider decryptorProvider,
            EncryptorProvider encryptorProvider, Key key) throws Exception {
        super(true, urls, parent);
        this.bootURLHandler = new BootURLHandler(decryptorProvider, encryptorProvider, key, this);
    }

    /**
     * Finds the resource with the given name. If found, it wraps the URL with a custom handler to enable on-the-fly
     * decryption.
     *
     * @param name The name of the resource.
     * @return A {@link URL} object for the resource, or {@code null} if the resource could not be found.
     */
    @Override
    public URL findResource(String name) {
        URL url = super.findResource(name);
        if (null == url) {
            return null;
        }
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), bootURLHandler);
        } catch (MalformedURLException e) {
            return url;
        }
    }

    /**
     * Returns an enumeration of all the resources with the given name. Each URL in the enumeration is wrapped with a
     * custom handler to enable on-the-fly decryption.
     *
     * @param name The name of the resource.
     * @return An {@link Enumeration} of {@link URL} objects for the resources.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> enumeration = super.findResources(name);
        if (null == enumeration) {
            return null;
        }
        return new XBootEnumeration(enumeration);
    }

    /**
     * Finds the class with the specified binary name. This method overrides the default behavior to handle
     * {@link ClassFormatError}s that might occur if a class is encrypted. In such cases, it attempts to load and
     * decrypt the class resource.
     *
     * @param name The binary name of the class.
     * @return The resulting {@link Class} object.
     * @throws ClassNotFoundException If the class could not be found or loaded.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassFormatError e) {
            URL resource = findResource(name.replace(Symbol.C_DOT, Symbol.C_SLASH) + ".class");
            if (null == resource) {
                throw new ClassNotFoundException(name, e);
            }
            try (InputStream in = resource.openStream()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Builder.transfer(in, bos);
                byte[] bytes = bos.toByteArray();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (Throwable t) {
                throw new ClassNotFoundException(name, t);
            }
        }
    }

    /**
     * An internal enumeration wrapper that applies the custom {@link BootURLHandler} to each URL.
     */
    private class XBootEnumeration implements Enumeration<URL> {

        private final Enumeration<URL> enumeration;

        /**
         * Constructs a new {@code XBootEnumeration}.
         *
         * @param enumeration The original enumeration of URLs.
         */
        XBootEnumeration(Enumeration<URL> enumeration) {
            this.enumeration = enumeration;
        }

        /**
         * Tests if this enumeration contains more elements.
         *
         * @return {@code true} if and only if this enumeration contains at least one more element to provide;
         *         {@code false} otherwise.
         */
        @Override
        public boolean hasMoreElements() {
            return enumeration.hasMoreElements();
        }

        /**
         * Returns the next element of this enumeration. If the URL is not null, it wraps it with the custom handler.
         *
         * @return The next element of this enumeration.
         */
        @Override
        public URL nextElement() {
            URL url = enumeration.nextElement();
            if (null == url) {
                return null;
            }
            try {
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), bootURLHandler);
            } catch (MalformedURLException e) {
                return url;
            }
        }
    }

}
