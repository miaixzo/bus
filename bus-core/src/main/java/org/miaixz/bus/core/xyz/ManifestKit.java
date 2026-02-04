/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Utility class for retrieving and parsing `manifest.mf` files from JARs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ManifestKit {

    private static final String[] MANIFEST_NAMES = { "Manifest.mf", "manifest.mf", "MANIFEST.MF" };

    /**
     * Gets the Manifest from the JAR file that contains the given class. Returns `null` if the class is not in a JAR
     * file.
     *
     * @param cls The class.
     * @return The Manifest.
     * @throws InternalException for IO errors.
     */
    public static Manifest getManifest(final Class<?> cls) throws InternalException {
        final URL url = ResourceKit.getResourceUrl(null, cls);
        final URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (connection instanceof JarURLConnection conn) {
            return getManifest(conn);
        }
        return null;
    }

    /**
     * Gets the Manifest from a JAR file or a project directory.
     *
     * @param classpathItem The file path.
     * @return The Manifest.
     * @throws InternalException for IO errors.
     */
    public static Manifest getManifest(final File classpathItem) throws InternalException {
        Manifest manifest = null;

        if (classpathItem.isFile()) {
            try (final JarFile jarFile = new JarFile(classpathItem)) {
                manifest = getManifest(jarFile);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            final File metaDir = new File(classpathItem, Normal.META_INF);
            File manifestFile = null;
            if (metaDir.isDirectory()) {
                for (final String name : MANIFEST_NAMES) {
                    final File mFile = new File(metaDir, name);
                    if (mFile.isFile()) {
                        manifestFile = mFile;
                        break;
                    }
                }
            }
            if (null != manifestFile) {
                try (final FileInputStream fis = new FileInputStream(manifestFile)) {
                    manifest = new Manifest(fis);
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        }

        return manifest;
    }

    /**
     * Gets the Manifest from a {@link JarURLConnection}.
     *
     * @param connection The {@link JarURLConnection}.
     * @return The Manifest.
     * @throws InternalException for IO errors.
     */
    public static Manifest getManifest(final JarURLConnection connection) throws InternalException {
        final JarFile jarFile;
        try {
            jarFile = connection.getJarFile();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return getManifest(jarFile);
    }

    /**
     * Gets the Manifest from a {@link JarFile}.
     *
     * @param jarFile The {@link JarFile}.
     * @return The Manifest.
     * @throws InternalException for IO errors.
     */
    public static Manifest getManifest(final JarFile jarFile) throws InternalException {
        try {
            return jarFile.getManifest();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
