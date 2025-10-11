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
package org.miaixz.bus.shade.safety.boot.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;

/**
 * A custom {@link URLStreamHandler} for URLs that intercepts connections to JAR entries and wraps them with
 * {@link JarURLConnection} for on-the-fly decryption/encryption. This handler is crucial for enabling secure loading of
 * resources within encrypted standard JARs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarURLHandler extends URLStreamHandler {

    /**
     * The provider responsible for decrypting input streams.
     */
    private final DecryptorProvider decryptorProvider;
    /**
     * The provider responsible for encrypting output streams.
     */
    private final EncryptorProvider encryptorProvider;
    /**
     * The cryptographic key used for encryption and decryption.
     */
    private final Key key;
    /**
     * A set of indexed resource paths that are known to be encrypted and require special handling.
     */
    private final Set<String> indexes;

    /**
     * Constructs a new {@code JarURLHandler}.
     *
     * @param decryptorProvider The provider responsible for decrypting resources.
     * @param encryptorProvider The provider responsible for encrypting resources.
     * @param key               The cryptographic key used for decryption.
     * @param classLoader       The class loader used to find indexed resources.
     * @throws Exception If an error occurs during initialization, such as reading the index file.
     */
    public JarURLHandler(DecryptorProvider decryptorProvider, EncryptorProvider encryptorProvider, Key key,
            ClassLoader classLoader) throws Exception {
        this.decryptorProvider = decryptorProvider;
        this.encryptorProvider = encryptorProvider;
        this.key = key;
        this.indexes = new LinkedHashSet<>();
        Enumeration<URL> resources = classLoader.getResources(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String url = resource.toString();
            String classpath = url.substring(0, url.lastIndexOf("!/") + 2);
            try (InputStream in = resource.openStream();
                    InputStreamReader isr = new InputStreamReader(in);
                    LineNumberReader lnr = new LineNumberReader(isr)) {
                String name;
                while (null != (name = lnr.readLine())) {
                    indexes.add(classpath + name);
                }
            }
        }
    }

    /**
     * Opens a connection to the specified URL. If the URL corresponds to an indexed encrypted resource within a JAR, it
     * returns a {@link JarURLConnection} to handle on-the-fly decryption. Otherwise, it delegates to the superclass's
     * {@code openConnection} method.
     *
     * @param url The URL to open a connection to.
     * @return A {@link URLConnection} for the specified URL.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        URLConnection urlConnection = new URL(url.toString()).openConnection();
        return indexes.contains(url.toString()) && urlConnection instanceof java.net.JarURLConnection
                ? new JarURLConnection((java.net.JarURLConnection) urlConnection, decryptorProvider, encryptorProvider,
                        key)
                : urlConnection;
    }

}
