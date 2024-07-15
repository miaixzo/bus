/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.io.resource.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * Resource资源工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceKit {

    /**
     * 读取Classpath下的资源为字符串，使用UTF-8编码
     *
     * @param resource 资源路径，使用相对ClassPath的路径
     * @return 资源内容
     */
    public static String readString(final String resource) {
        return getResource(resource).readString();
    }

    /**
     * 读取Classpath下的资源为字符串
     *
     * @param resource 可以是绝对路径，也可以是相对路径（相对ClassPath）
     * @param charset  编码
     * @return 资源内容
     */
    public static String readString(final String resource, final java.nio.charset.Charset charset) {
        return getResource(resource).readString(charset);
    }

    /**
     * 读取Classpath下的资源为byte[]
     *
     * @param resource 可以是绝对路径，也可以是相对路径（相对ClassPath）
     * @return 资源内容
     */
    public static byte[] readBytes(final String resource) {
        return getResource(resource).readBytes();
    }

    /**
     * 从ClassPath资源中获取{@link InputStream}
     *
     * @param resource ClassPath资源
     * @return {@link InputStream}
     * @throws InternalException 资源不存在异常
     */
    public static InputStream getStream(final String resource) throws InternalException {
        return getResource(resource).getStream();
    }

    /**
     * 从ClassPath资源中获取{@link InputStream}，当资源不存在时返回null
     *
     * @param resource ClassPath资源
     * @return {@link InputStream}
     */
    public static InputStream getStreamSafe(final String resource) {
        try {
            return getResource(resource).getStream();
        } catch (final InternalException e) {
            // ignore
        }
        return null;
    }

    /**
     * 从ClassPath资源中获取{@link BufferedReader}
     *
     * @param resource ClassPath资源
     * @return {@link InputStream}
     */
    public static BufferedReader getReader(final String resource) {
        return getReader(resource, Charset.UTF_8);
    }

    /**
     * 从ClassPath资源中获取{@link BufferedReader}
     *
     * @param resource ClassPath资源
     * @param charset  编码
     * @return {@link InputStream}
     */
    public static BufferedReader getReader(final String resource, final java.nio.charset.Charset charset) {
        return getResource(resource).getReader(charset);
    }

    /**
     * 获得资源的URL
     * 路径用/分隔，例如:
     *
     * <pre>
     * config/a/db.config
     * spring/xml/test.xml
     * </pre>
     *
     * @param resource 资源（相对Classpath的路径）
     * @return 资源URL
     * @throws InternalException IO异常
     */
    public static URL getResourceUrl(final String resource) throws InternalException {
        return getResourceUrl(resource, null);
    }

    /**
     * 获取指定路径下的资源列表
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     */
    public static List<URL> getResourceUrls(final String resource) {
        return getResourceUrls(resource, null);
    }

    /**
     * 获取指定路径下的资源列表
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @param filter   过滤器，用于过滤不需要的资源，{@code null}表示不过滤，保留所有元素
     * @return 资源列表
     */
    public static List<URL> getResourceUrls(final String resource, final Predicate<URL> filter) {
        return IteratorKit.filterToList(getResourceUrlIter(resource), filter);
    }

    /**
     * 获取指定路径下的资源Iterator
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     */
    public static EnumerationIterator<URL> getResourceUrlIter(final String resource) {
        return getResourceUrlIter(resource, null);
    }

    /**
     * 获取指定路径下的资源Iterator
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource    资源路径
     * @param classLoader {@link ClassLoader}
     * @return 资源列表
     */
    public static EnumerationIterator<URL> getResourceUrlIter(final String resource, final ClassLoader classLoader) {
        final Enumeration<URL> resources;
        try {
            resources = ObjectKit.defaultIfNull(classLoader, ClassKit::getClassLoader).getResources(resource);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new EnumerationIterator<>(resources);
    }

    /**
     * 获得资源相对路径对应的URL
     *
     * @param resource  资源相对路径，{@code null}和""都表示classpath根路径
     * @param baseClass 基准Class，获得的相对路径相对于此Class所在路径，如果为{@code null}则相对ClassPath
     * @return {@link URL}
     */
    public static URL getResourceUrl(String resource, final Class<?> baseClass) {
        resource = StringKit.toStringOrEmpty(resource);
        return (null != baseClass) ? baseClass.getResource(resource) : ClassKit.getClassLoader().getResource(resource);
    }

    /**
     * 获取{@link Resource} 资源对象
     * 如果提供路径为绝对路径或路径以file:开头，返回{@link FileResource}，否则返回{@link ClassPathResource}
     *
     * @param path 路径，可以是绝对路径，也可以是相对路径（相对ClassPath）
     * @return {@link Resource} 资源对象
     */
    public static Resource getResource(final String path) {
        if (StringKit.isNotBlank(path)) {
            if (path.startsWith(Normal.FILE_URL_PREFIX) || FileKit.isAbsolutePath(path)) {
                return new FileResource(path);
            }
        }
        return new ClassPathResource(path);
    }

    /**
     * 获取{@link UrlResource} 资源对象
     *
     * @param url URL
     * @return {@link Resource} 资源对象
     */
    public static Resource getResource(final URL url) {
        if (Normal.isJarURL(url)) {
            return new JarResource(url);
        } else if (Normal.isFileURL(url)) {
            return new FileResource(url.getFile());
        }
        return new UrlResource(url);
    }

    /**
     * 获取{@link FileResource} 资源对象
     *
     * @param file {@link File}
     * @return {@link Resource} 资源对象
     */
    public static Resource getResource(final File file) {
        return new FileResource(file);
    }

    /**
     * 获取同名的所有资源
     *
     * @param resource 资源名
     * @return {@link MultiResource}
     */
    public static MultiResource getResources(final String resource) {
        return getResources(resource, null);
    }

    /**
     * 获取同名的所有资源
     * 资源的加载顺序是：
     * <ul>
     *     <li>1. 首先在本项目下查找资源文件</li>
     *     <li>2. 按照classpath定义的顺序，去对应路径或jar包下寻找资源文件</li>
     * </ul>
     *
     * @param resource    资源名
     * @param classLoader {@link ClassLoader}，{@code null}表示使用默认的当前上下文ClassLoader
     * @return {@link MultiResource}
     */
    public static MultiResource getResources(final String resource, final ClassLoader classLoader) {
        final EnumerationIterator<URL> iter = getResourceUrlIter(resource, classLoader);
        final MultiResource resources = new MultiResource();
        for (final URL url : iter) {
            resources.add(getResource(url));
        }
        return resources;
    }

    /**
     * 加载配置文件内容到{@link Properties}中
     * 需要注意的是，如果资源文件的扩展名是.xml，会调用{@link Properties#loadFromXML(InputStream)} 读取。
     *
     * @param properties {@link Properties}文件
     * @param resource   资源
     * @param charset    编码，对XML无效，默认UTF-8
     */
    public static void loadTo(final Properties properties, final Resource resource, final java.nio.charset.Charset charset) {
        Assert.notNull(properties);
        Assert.notNull(resource);
        final String filename = resource.getName();
        if (filename != null && StringKit.endWithIgnoreCase(filename, ".xml")) {
            // XML
            try (final InputStream in = resource.getStream()) {
                properties.loadFromXML(in);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            // .properties
            try (final BufferedReader reader = resource.getReader(
                    ObjectKit.defaultIfNull(charset, Charset.UTF_8))) {
                properties.load(reader);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * 加载指定名称的所有配置文件内容到{@link Properties}中
     *
     * @param properties   {@link Properties}文件
     * @param resourceName 资源名，可以是相对classpath的路径，也可以是绝对路径
     * @param classLoader  {@link ClassLoader}，{@code null}表示使用默认的当前上下文ClassLoader
     * @param charset      编码，对XML无效，默认UTF-8
     * @param isOverride   是否覆盖模式
     */
    public static void loadAllTo(final Properties properties, final String resourceName,
                                 final ClassLoader classLoader, final java.nio.charset.Charset charset, final boolean isOverride) {
        if (isOverride) {
            for (final Resource resource : getResources(resourceName, classLoader)) {
                loadTo(properties, resource, charset);
            }
            return;
        }

        // 非覆盖模式下，读取配置文件后逐个检查key
        final Properties tmpProps = new Properties();
        for (final Resource resource : getResources(resourceName, classLoader)) {
            loadTo(tmpProps, resource, charset);
            tmpProps.forEach((name, value) -> {
                if (!properties.containsKey(name)) {
                    properties.put(name, value);
                }
            });
        }
    }

}
