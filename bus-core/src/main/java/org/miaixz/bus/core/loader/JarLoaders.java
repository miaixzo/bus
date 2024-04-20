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
package org.miaixz.bus.core.loader;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * 外部Jar的类加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarLoaders extends URLClassLoader {

    /**
     * 构造
     */
    public JarLoaders() {
        this(new URL[]{});
    }

    /**
     * 构造
     *
     * @param urls 被加载的URL
     */
    public JarLoaders(URL[] urls) {
        super(urls, ClassKit.getClassLoader());
    }

    /**
     * 构造
     *
     * @param urls        被加载的URL
     * @param classLoader 类加载器
     */
    public JarLoaders(URL[] urls, ClassLoader classLoader) {
        super(urls, classLoader);
    }

    /**
     * 加载Jar到ClassPath
     *
     * @param dir jar文件或所在目录
     * @return JarLoaders
     */
    public static JarLoaders load(File dir) {
        final JarLoaders loader = new JarLoaders();
        loader.addJar(dir);
        loader.addURL(dir);
        return loader;
    }

    /**
     * 加载Jar到ClassPath
     *
     * @param jarFile jar文件或所在目录
     * @return JarLoaders
     */
    public static JarLoaders loadJar(File jarFile) {
        final JarLoaders loader = new JarLoaders();
        try {
            loader.addJar(jarFile);
        } finally {
            IoKit.close(loader);
        }
        return loader;
    }

    /**
     * 加载Jar文件到指定loader中
     *
     * @param loader  {@link URLClassLoader}
     * @param jarFile 被加载的jar
     * @throws InternalException IO异常包装和执行异常
     */
    public static void loadJar(URLClassLoader loader, File jarFile) throws InternalException {
        try {
            final Method method = ClassKit.getDeclaredMethod(URLClassLoader.class, "addURL", URL.class);
            if (null != method) {
                final List<File> jars = loopJar(jarFile);
                for (File jar : jars) {
                    ReflectKit.invoke(loader, method, jar.toURI().toURL());
                }
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 加载Jar文件到System ClassLoader中
     *
     * @param jarFile 被加载的jar
     * @return System ClassLoader
     */
    public static URLClassLoader loadJarToSystemClassLoader(File jarFile) {
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        loadJar(urlClassLoader, jarFile);
        return urlClassLoader;
    }

    /**
     * 递归获得Jar文件
     *
     * @param file jar文件或者包含jar文件的目录
     * @return jar文件列表
     */
    private static List<File> loopJar(File file) {
        return FileKit.loopFiles(file, file1 -> {
            final String path = file1.getPath();
            return null != path && path.toLowerCase().endsWith(".jar");
        });
    }

    /**
     * 是否为jar文件
     *
     * @param file 文件
     * @return 是否为jar文件
     */
    private static boolean isJarFile(File file) {
        if (false == FileKit.isFile(file)) {
            return false;
        }
        return file.getPath().toLowerCase().endsWith(".jar");
    }

    /**
     * 加载Jar文件,或者加载目录
     *
     * @param jarFile jar文件或者jar文件所在目录
     * @return this
     */
    public JarLoaders addJar(File jarFile) {
        final List<File> jars = loopJar(jarFile);
        try {
            for (File jar : jars) {
                super.addURL(jar.toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     * 增加class所在目录或文件
     * 如果为目录,此目录用于搜索class文件,如果为文件,需为jar文件
     *
     * @param dir 目录
     * @return the jarClassLoader
     */
    public JarLoaders addURL(File dir) {
        super.addURL(UriKit.getURL(dir));
        return this;
    }

}
