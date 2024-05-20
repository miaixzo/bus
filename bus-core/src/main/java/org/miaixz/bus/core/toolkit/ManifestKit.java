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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Jar包中manifest.mf文件获取和解析工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ManifestKit {

    private static final String[] MANIFEST_NAMES = {"Manifest.mf", "manifest.mf", "MANIFEST.MF"};

    /**
     * 根据 class 获取 所在 jar 包文件的 Manifest
     * 如果这个类不在jar包中，返回{@code null}
     *
     * @param cls 类
     * @return Manifest
     * @throws InternalException IO异常
     */
    public static Manifest getManifest(final Class<?> cls) throws InternalException {
        final URL url = ResourceKit.getResourceUrl(null, cls);
        final URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (connection instanceof JarURLConnection) {
            final JarURLConnection conn = (JarURLConnection) connection;
            return getManifest(conn);
        }
        return null;
    }

    /**
     * 获取 jar 包文件或项目目录下的 Manifest
     *
     * @param classpathItem 文件路径
     * @return Manifest
     * @throws InternalException IO异常
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
     * 根据 {@link JarURLConnection} 获取 jar 包文件的 Manifest
     *
     * @param connection {@link JarURLConnection}
     * @return Manifest
     * @throws InternalException IO异常
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
     * 根据 {@link JarURLConnection} 获取 jar 包文件的 Manifest
     *
     * @param jarFile {@link JarURLConnection}
     * @return Manifest
     * @throws InternalException IO异常
     */
    public static Manifest getManifest(final JarFile jarFile) throws InternalException {
        try {
            return jarFile.getManifest();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
