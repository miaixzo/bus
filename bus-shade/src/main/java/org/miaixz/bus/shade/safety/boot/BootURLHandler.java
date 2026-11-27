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
package org.miaixz.bus.shade.safety.boot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.loader.net.protocol.jar.Handler;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;

/**
 * A custom {@link Handler} for URLs that intercepts connections to JAR entries and wraps them with
 * {@link BootURLConnection} for on-the-fly decryption/encryption. This handler is crucial for enabling secure loading
 * of resources within encrypted Spring Boot JARs.
 *
 * @author Kimi Liu
 */
public class BootURLHandler extends Handler {

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
     * Constructs a new {@code BootURLHandler}.
     *
     * @param decryptorProvider The provider responsible for decrypting resources.
     * @param encryptorProvider The provider responsible for encrypting resources.
     * @param key               The cryptographic key used for decryption.
     * @param classLoader       The class loader used to find indexed resources.
     * @throws Exception If an error occurs during initialization, such as reading the index file.
     */
    public BootURLHandler(DecryptorProvider decryptorProvider, EncryptorProvider encryptorProvider, Key key,
            ClassLoader classLoader) throws Exception {
        this.decryptorProvider = decryptorProvider;
        this.encryptorProvider = encryptorProvider;
        this.key = key;
        this.indexes = new LinkedHashSet<>();
        Enumeration<URL> resources = classLoader.getResources(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try (InputStream in = resource.openStream();
                    InputStreamReader isr = new InputStreamReader(in);
                    LineNumberReader lnr = new LineNumberReader(isr)) {
                String name;
                while (null != (name = lnr.readLine())) {
                    indexes.add(normalize(name));
                }
            }
        }
        Logger.debug(false, "Shade", "Boot URL handler initialized: encryptedResourceCount={}", indexes.size());
    }

    /**
     * Resolves the archive entry name of a URL, supporting both the {@code jar:file:...!/entry} form and the nested
     * {@code jar:nested:.../!entry!/entry} form used by Spring Boot 3.2 and later.
     *
     * @param url the resource URL
     * @return the normalized entry name, or {@code null} when the URL does not point at an archive entry
     */
    private static String entryName(final URL url) {
        String spec = url.toString();
        int index = spec.indexOf("!/");
        if (index < 0) {
            return null;
        }
        String name = normalize(spec.substring(index + 2));
        return name.isEmpty() || name.endsWith(Symbol.SLASH) ? null : name;
    }

    /**
     * Normalizes an archive entry name by collapsing the nested marker and duplicate separators.
     *
     * @param value raw entry name
     * @return normalized entry name
     */
    private static String normalize(final String value) {
        if (null == value) {
            return Normal.EMPTY;
        }
        String name = value.replace("!/", Symbol.SLASH);
        while (name.contains("//")) {
            name = name.replace("//", Symbol.SLASH);
        }
        while (name.startsWith(Symbol.SLASH)) {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * Opens a connection to the specified URL. If the URL corresponds to an indexed encrypted resource within a JAR, it
     * returns a {@link BootURLConnection} to handle on-the-fly decryption. Otherwise, it delegates to the superclass's
     * {@code openConnection} method.
     *
     * @param url The URL to open a connection to.
     * @return A {@link URLConnection} for the specified URL.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        URLConnection urlConnection = super.openConnection(url);
        String name = entryName(url);
        if (null != name && indexes.contains(name) && urlConnection instanceof JarURLConnection) {
            Logger.trace(
                    true,
                    "Shade",
                    "Boot encrypted resource connection opened: protocol={}, resourceChars={}",
                    url.getProtocol(),
                    url.toString().length());
            return new BootURLConnection((JarURLConnection) urlConnection, decryptorProvider, encryptorProvider, key);
        }
        return urlConnection;
    }

}
