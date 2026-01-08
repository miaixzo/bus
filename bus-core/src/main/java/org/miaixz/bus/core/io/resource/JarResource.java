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
package org.miaixz.bus.core.io.resource;

import java.io.IOException;
import java.io.Serial;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ZipKit;

/**
 * Represents a resource located within a JAR file. This class extends {@link UrlResource} to provide specific handling
 * for JAR entries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarResource extends UrlResource {

    @Serial
    private static final long serialVersionUID = 2852231952129L;

    /**
     * Constructs a {@code JarResource} from a given {@link URI}.
     *
     * @param uri The {@link URI} pointing to the JAR entry.
     */
    public JarResource(final URI uri) {
        super(uri);
    }

    /**
     * Constructs a {@code JarResource} from a given {@link URL}.
     *
     * @param url The {@link URL} pointing to the JAR entry.
     */
    public JarResource(final URL url) {
        super(url);
    }

    /**
     * Constructs a {@code JarResource} from a given {@link URL} and a resource name.
     *
     * @param url  The {@link URL} pointing to the JAR entry.
     * @param name The name of the resource.
     */
    public JarResource(final URL url, final String name) {
        super(url, name);
    }

    /**
     * Retrieves the {@link JarFile} object corresponding to the resource's URL.
     *
     * @return The {@link JarFile} instance.
     * @throws InternalException If an I/O error occurs while obtaining the JAR file.
     */
    public JarFile getJarFile() throws InternalException {
        try {
            return doGetJarFile();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Internal method to obtain the {@link JarFile}. It first attempts to get it via {@link URLConnection}. If the
     * connection is not a {@link JarURLConnection}, it tries to extract the JAR file path by removing WAR or JAR
     * protocol separators from the URL file string.
     *
     * @return The {@link JarFile} instance.
     * @throws IOException If an I/O error occurs.
     */
    private JarFile doGetJarFile() throws IOException {
        final URLConnection con = getUrl().openConnection();
        if (con instanceof JarURLConnection jarCon) {
            return jarCon.getJarFile();
        } else {
            final String urlFile = getUrl().getFile();
            int separatorIndex = urlFile.indexOf(Normal.WAR_URL_SEPARATOR);
            if (separatorIndex == -1) {
                separatorIndex = urlFile.indexOf(Normal.JAR_URL_SEPARATOR);
            }
            if (separatorIndex != -1) {
                return ZipKit.ofJar(urlFile.substring(0, separatorIndex));
            } else {
                return new JarFile(urlFile);
            }
        }
    }

}
