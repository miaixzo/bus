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
package org.miaixz.bus.core.io.resource;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.AntPathMatcher;
import org.miaixz.bus.core.xyz.*;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

/**
 * 资源查找器
 * 参考Spring的PathMatchingResourcePatternResolver，实现classpath资源查找，利用{@link AntPathMatcher}筛选资源
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceFinder {

    private final ClassLoader classLoader;
    private final AntPathMatcher pathMatcher;

    /**
     * 构造
     *
     * @param classLoader 类加载器，用于定义查找资源的范围
     */
    public ResourceFinder(final ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.pathMatcher = new AntPathMatcher();
    }

    /**
     * 构建新的ResourceFinder，使用当前环境的类加载器
     *
     * @return ResourceFinder
     */
    public static ResourceFinder of() {
        return of(ClassKit.getClassLoader());
    }

    /**
     * 构建新的ResourceFinder
     *
     * @param classLoader 类加载器，用于限定查找范围
     * @return ResourceFinder
     */
    public static ResourceFinder of(final ClassLoader classLoader) {
        return new ResourceFinder(classLoader);
    }

    /**
     * 替换'\'为'/'
     *
     * @param path 路径
     * @return 替换后的路径
     */
    private static String replaceBackSlash(final String path) {
        return StringKit.isEmpty(path) ? path : path.replace(Symbol.C_BACKSLASH, Symbol.C_SLASH);
    }

    /**
     * 查找给定表达式对应的资源
     *
     * @param locationPattern 路径表达式
     * @return {@link MultiResource}
     */
    public MultiResource find(final String locationPattern) {
        // 根目录，如 "/WEB-INF/*.xml" 返回 "/WEB-INF/"
        final String rootDirPath = determineRootDir(locationPattern);
        // 子表达式，如"/WEB-INF/*.xml" 返回 "*.xml"
        final String subPattern = locationPattern.substring(rootDirPath.length());

        final MultiResource result = new MultiResource();
        // 遍历根目录下所有资源，并过滤保留符合条件的资源
        for (final Resource rootResource : ResourceKit.getResources(rootDirPath, classLoader)) {
            if (rootResource instanceof JarResource) {
                // 在jar包中
                try {
                    result.addAll(findInJar((JarResource) rootResource, subPattern));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            } else if (rootResource instanceof FileResource) {
                // 文件夹中
                result.addAll(findInDir((FileResource) rootResource, subPattern));
            } else {
                throw new InternalException("Unsupported resource type: {}", rootResource.getClass().getName());
            }
        }

        return result;
    }

    /**
     * 查找jar包中的资源
     *
     * @param rootResource 根资源，为jar包文件
     * @param subPattern   子表达式，如 *.xml
     * @return 符合条件的资源
     * @throws IOException IO异常
     */
    protected MultiResource findInJar(final JarResource rootResource, final String subPattern) throws IOException {
        final URL rootDirURL = rootResource.getUrl();
        final URLConnection conn = rootDirURL.openConnection();

        final JarFile jarFile;
        String rootEntryPath;
        final boolean closeJarFile;

        if (conn instanceof JarURLConnection) {
            final JarURLConnection jarCon = (JarURLConnection) conn;
            UrlKit.useCachesIfNecessary(jarCon);
            jarFile = jarCon.getJarFile();
            final JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : Normal.EMPTY);
            closeJarFile = !jarCon.getUseCaches();
        } else {
            // 去除子路径后重新获取jar文件
            final String urlFile = rootDirURL.getFile();
            try {
                int separatorIndex = urlFile.indexOf(Normal.WAR_URL_SEPARATOR);
                if (separatorIndex == -1) {
                    separatorIndex = urlFile.indexOf(Normal.JAR_URL_SEPARATOR);
                }
                if (separatorIndex != -1) {
                    final String jarFileUrl = urlFile.substring(0, separatorIndex);
                    rootEntryPath = urlFile.substring(separatorIndex + 2);  // both separators are 2 chars
                    jarFile = ZipKit.ofJar(jarFileUrl);
                } else {
                    jarFile = new JarFile(urlFile);
                    rootEntryPath = Normal.EMPTY;
                }
                closeJarFile = true;
            } catch (final ZipException ex) {
                return new MultiResource();
            }
        }

        rootEntryPath = StringKit.addSuffixIfNot(rootEntryPath, Symbol.SLASH);
        // 遍历jar中的entry，筛选之
        final MultiResource result = new MultiResource();

        try {
            String entryPath;
            for (final JarEntry entry : new EnumerationIterator<>(jarFile.entries())) {
                entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    final String relativePath = entryPath.substring(rootEntryPath.length());
                    if (pathMatcher.match(subPattern, relativePath)) {
                        result.add(ResourceKit.getResource(UrlKit.getURL(rootDirURL, relativePath)));
                    }
                }
            }
        } finally {
            if (closeJarFile) {
                IoKit.closeQuietly(jarFile);
            }
        }

        return result;
    }

    /**
     * 遍历目录查找指定表达式匹配的文件列表
     *
     * @param resource   文件资源
     * @param subPattern 子表达式
     * @return 满足条件的文件
     */
    protected MultiResource findInDir(final FileResource resource, final String subPattern) {
        final MultiResource result = new MultiResource();
        final File rootDir = resource.getFile();
        if (!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) {
            // 保证给定文件存在、为目录且可读
            return result;
        }

        final String fullPattern = replaceBackSlash(rootDir.getAbsolutePath() + Symbol.SLASH + subPattern);

        FileKit.walkFiles(rootDir, (file -> {
            final String currentPath = replaceBackSlash(file.getAbsolutePath());
            if (file.isDirectory()) {
                // 检查目录是否满足表达式开始规则，满足则继续向下查找，否则跳过
                return pathMatcher.matchStart(fullPattern, StringKit.addSuffixIfNot(currentPath, Symbol.SLASH));
            }

            if (pathMatcher.match(fullPattern, currentPath)) {
                result.add(new FileResource(file));
                return true;
            }

            return false;
        }));

        return result;
    }

    /**
     * 根据给定的路径表达式，找到跟路径
     * 根路径即不包含表达式的路径，如 "/WEB-INF/*.xml" 返回 "/WEB-INF/"
     *
     * @param location 路径表达式
     * @return root dir
     */
    protected String determineRootDir(final String location) {
        final int prefixEnd = location.indexOf(Symbol.C_COLON) + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && pathMatcher.isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf(Symbol.C_SLASH, rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

}
