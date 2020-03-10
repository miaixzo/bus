/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.utils;

import org.aoju.bus.core.io.resource.ClassPathResource;
import org.aoju.bus.core.io.resource.FileResource;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;

/**
 * ClassPath资源工具类
 *
 * @author Kimi Liu
 * @version 5.6.6
 * @since JDK 1.8+
 */
public class ResourceUtils {

    /**
     * 读取Classpath下的资源为字符串,使用UTF-8编码
     *
     * @param resource 资源路径,使用相对ClassPath的路径
     * @return 资源内容
     * @since 3.1.1
     */
    public static String readUtf8Str(String resource) {
        return new ClassPathResource(resource).readUtf8Str();
    }

    /**
     * 读取Classpath下的资源为字符串
     *
     * @param resource 资源路径,使用相对ClassPath的路径
     * @param charset  编码
     * @return 资源内容
     * @since 3.1.1
     */
    public static String readStr(String resource, Charset charset) {
        return new ClassPathResource(resource).readStr(charset);
    }

    /**
     * 从ClassPath资源中获取{@link InputStream}
     *
     * @param resurce ClassPath资源
     * @return {@link InputStream}
     * @throws InstrumentException 资源不存在异常
     * @since 3.1.9
     */
    public static InputStream getStream(String resurce) throws InstrumentException {
        return new ClassPathResource(resurce).getStream();
    }

    /**
     * 从ClassPath资源中获取{@link InputStream},当资源不存在时返回null
     *
     * @param resurce ClassPath资源
     * @return {@link InputStream}
     */
    public static InputStream getStreamSafe(String resurce) {
        try {
            return new ClassPathResource(resurce).getStream();
        } catch (InstrumentException e) {
            // ignore
        }
        return null;
    }

    /**
     * 从ClassPath资源中获取{@link BufferedReader}
     *
     * @param resurce ClassPath资源
     * @param charset 编码
     * @return {@link InputStream}
     * @since 3.1.9
     */
    public static BufferedReader getReader(String resurce, Charset charset) {
        return new ClassPathResource(resurce).getReader(charset);
    }

    /**
     * 获得资源的URL
     * 路径用/分隔,例如:
     *
     * <pre>
     * config/a/db.config
     * spring/xml/test.xml
     * </pre>
     *
     * @param resource 资源（相对Classpath的路径）
     * @return 资源URL
     */
    public static URL getResource(String resource) throws InstrumentException {
        return getResource(resource, null);
    }

    /**
     * 获取指定路径下的资源列表
     * 路径格式必须为目录格式,用/分隔,例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     */
    public static List<URL> getResources(String resource) {
        final Enumeration<URL> resources;
        try {
            resources = ClassUtils.getClassLoader().getResources(resource);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return CollUtils.newArrayList(resources);
    }

    /**
     * 获取指定路径下的资源Iterator
     * 路径格式必须为目录格式,用/分隔,例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     */
    public static IterUtils.EnumerationIter<URL> getResourceIter(String resource) {
        final Enumeration<URL> resources;
        try {
            resources = ClassUtils.getClassLoader().getResources(resource);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return new IterUtils.EnumerationIter<>(resources);
    }

    /**
     * 获得资源相对路径对应的URL
     *
     * @param resource  资源相对路径
     * @param baseClass 基准Class,获得的相对路径相对于此Class所在路径,如果为{@code null}则相对ClassPath
     * @return {@link URL}
     */
    public static URL getResource(String resource, Class<?> baseClass) {
        return (null != baseClass) ? baseClass.getResource(resource) : ClassUtils.getClassLoader().getResource(resource);
    }

    /**
     * 获取{@link Resource} 资源对象
     * 如果提供路径为绝对路径,返回{@link FileResource},否则返回{@link ClassPathResource}
     *
     * @param path 路径,可以是绝对路径,也可以是相对路径
     * @return {@link Resource} 资源对象
     * @since 5.6.6
     */
    public static Resource getResourceObj(String path) {
        return FileUtils.isAbsolutePath(path) ? new FileResource(path) : new ClassPathResource(path);
    }

}
