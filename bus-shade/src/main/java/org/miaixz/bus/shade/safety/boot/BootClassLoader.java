/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.shade.safety.boot;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;
import org.springframework.boot.loader.LaunchedURLClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

/**
 * 类加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootClassLoader extends LaunchedURLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final BootURLHandler bootURLHandler;

    public BootClassLoader(URL[] urls, ClassLoader parent, DecryptorProvider decryptorProvider, EncryptorProvider encryptorProvider, Key key) throws Exception {
        super(urls, parent);
        this.bootURLHandler = new BootURLHandler(decryptorProvider, encryptorProvider, key, this);
    }

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

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> enumeration = super.findResources(name);
        if (null == enumeration) {
            return null;
        }
        return new XBootEnumeration(enumeration);
    }

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

    private class XBootEnumeration implements Enumeration<URL> {
        private final Enumeration<URL> enumeration;

        XBootEnumeration(Enumeration<URL> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasMoreElements() {
            return enumeration.hasMoreElements();
        }

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
