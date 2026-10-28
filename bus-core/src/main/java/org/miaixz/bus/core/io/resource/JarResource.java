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
package org.miaixz.bus.core.io.resource;

import java.io.Serial;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ZipKit;

/**
 * Represents a resource located within a JAR file. This class extends {@link UrlResource} to provide specific handling
 * for JAR entries.
 *
 * @author Kimi Liu
 */
public class JarResource extends UrlResource {

    @Serial
    private static final long serialVersionUID = 2536600013581L;

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
     * Retrieves the {@link JarFile} object corresponding to the resource's URL. The returned instance may be shared
     * through the URL connection cache and must not be closed by the caller.
     *
     * @return The {@link JarFile} instance.
     * @throws InternalException If an I/O error occurs while obtaining the JAR file.
     */
    public JarFile getJarFile() throws InternalException {
        return getJarFile(true);
    }

    /**
     * Retrieves the JAR file with optional URL connection caching. When {@code useCaches} is {@code false}, returns an
     * independent instance that the caller must close. Otherwise, the instance may be shared and must not be closed by
     * the caller.
     *
     * @param useCaches Whether to allow a cached, potentially shared JAR file.
     * @return The {@link JarFile} instance.
     * @throws InternalException If an I/O error occurs while obtaining the JAR file.
     */
    public JarFile getJarFile(final boolean useCaches) throws InternalException {
        return ZipKit.ofJar(getUrl(), useCaches);
    }

}
