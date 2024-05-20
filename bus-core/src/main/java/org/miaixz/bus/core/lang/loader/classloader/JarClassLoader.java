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
package org.miaixz.bus.core.lang.loader.classloader;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.UrlKit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * 外部Jar的类加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarClassLoader extends URLClassLoader {

    /**
     * 构造
     */
    public JarClassLoader() {
        this(new URL[]{});
    }

    /**
     * 构造
     *
     * @param urls 被加载的URL
     */
    public JarClassLoader(final URL[] urls) {
        super(urls, ClassKit.getClassLoader());
    }

    /**
     * 构造
     *
     * @param urls        被加载的URL
     * @param classLoader 类加载器
     */
    public JarClassLoader(final URL[] urls, final ClassLoader classLoader) {
        super(urls, classLoader);
    }

    /**
     * 加载Jar到ClassPath
     *
     * @param dir jar文件或所在目录
     * @return JarClassLoader
     */
    public static JarClassLoader load(final File dir) {
        final JarClassLoader loader = new JarClassLoader();
        loader.addJar(dir);//查找加载所有jar
        loader.addURL(dir);//查找加载所有class
        return loader;
    }

    /**
     * 加载Jar到ClassPath
     *
     * @param jarFile jar文件或所在目录
     * @return JarClassLoader
     */
    public static JarClassLoader loadJar(final File jarFile) {
        final JarClassLoader loader = new JarClassLoader();
        loader.addJar(jarFile);
        return loader;
    }

    /**
     * 加载Jar文件到指定loader中
     *
     * @param loader  {@link URLClassLoader}
     * @param jarFile 被加载的jar
     * @throws InternalException IO异常包装和执行异常
     */
    public static void loadJar(final URLClassLoader loader, final File jarFile) throws InternalException {
        try {
            final Method method = MethodKit.getMethod(URLClassLoader.class, "addURL", URL.class);
            if (null != method) {
                final List<File> jars = loopJar(jarFile);
                for (final File jar : jars) {
                    MethodKit.invoke(loader, method, jar.toURI().toURL());
                }
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 加载Jar文件到System ClassLoader中
     *
     * @param jarFile 被加载的jar
     * @return System ClassLoader
     */
    public static URLClassLoader loadJarToSystemClassLoader(final File jarFile) {
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        loadJar(urlClassLoader, jarFile);
        return urlClassLoader;
    }

    /**
     * 递归获得Jar文件
     *
     * @param file jar文件或者包含jar文件的目录
     * @return jar文件列表
     */
    private static List<File> loopJar(final File file) {
        return FileKit.loopFiles(file, JarClassLoader::isJarFile);
    }

    /**
     * 是否为jar文件
     *
     * @param file 文件
     * @return 是否为jar文件
     */
    private static boolean isJarFile(final File file) {
        return FileKit.isFile(file) &&
                FileName.isType(file.getName(), FileName.EXT_JAR);
    }

    /**
     * 加载Jar文件，或者加载目录
     *
     * @param jarFileOrDir jar文件或者jar文件所在目录
     * @return this
     */
    public JarClassLoader addJar(final File jarFileOrDir) {
        // loopJar方法中，如果传入的是jar文件，直接返回此文件
        final List<File> jars = loopJar(jarFileOrDir);
        for (final File jar : jars) {
            addURL(jar);
        }
        return this;
    }

    @Override
    public void addURL(final URL url) {
        super.addURL(url);
    }

    /**
     * 增加class所在目录或文件
     * 如果为目录，此目录用于搜索class文件，如果为文件，需为jar文件
     *
     * @param dir 目录
     * @return this
     */
    public JarClassLoader addURL(final File dir) {
        super.addURL(UrlKit.getURL(dir));
        return this;
    }

}
