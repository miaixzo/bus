/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

/**
 * ClassPath单一资源访问类
 * 传入路径path必须为相对路径，如果传入绝对路径，Linux路径会去掉开头的“/”，而Windows路径会直接报错。
 * 传入的path所指向的资源必须存在，否则报错
 *
 * @author Kimi Liu
 * @version 3.2.5
 * @since JDK 1.8
 */
public class ClassPathResource extends UriResource {

    private String path;
    private ClassLoader classLoader;
    private Class<?> clazz;

    /**
     * 构造
     *
     * @param path 相对于ClassPath的路径
     */
    public ClassPathResource(String path) {
        this(path, null, null);
    }

    /**
     * 构造
     *
     * @param path        相对于ClassPath的路径
     * @param classLoader {@link ClassLoader}
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        this(path, classLoader, null);
    }

    /**
     * 构造
     *
     * @param path  相对于给定Class的路径
     * @param clazz {@link Class} 用于定位路径
     */
    public ClassPathResource(String path, Class<?> clazz) {
        this(path, null, clazz);
    }

    /**
     * 构造
     *
     * @param pathBaseClassLoader 相对路径
     * @param classLoader         {@link ClassLoader}
     * @param clazz               {@link Class} 用于定位路径
     */
    public ClassPathResource(String pathBaseClassLoader, ClassLoader classLoader, Class<?> clazz) {
        super(null);
        Assert.notNull(pathBaseClassLoader, "Path must not be null");

        final String path = normalizePath(pathBaseClassLoader);
        this.path = path;
        this.name = StringUtils.isBlank(path) ? null : FileUtils.getName(path);

        this.classLoader = ObjectUtils.defaultIfNull(classLoader, ClassUtils.getClassLoader());
        this.clazz = clazz;
        initUrl();
    }

    /**
     * 获得Path
     *
     * @return path
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * 获得绝对路径Path
     * 对于不存在的资源，返回拼接后的绝对路径
     *
     * @return 绝对路径path
     */
    public final String getAbsolutePath() {
        if (FileUtils.isAbsolutePath(this.path)) {
            return this.path;
        }
        // url在初始化的时候已经断言，此处始终不为null
        return FileUtils.normalize(UriUtils.getDecodedPath(this.url));
    }

    /**
     * 获得 {@link ClassLoader}
     *
     * @return {@link ClassLoader}
     */
    public final ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * 根据给定资源初始化URL
     */
    private void initUrl() {
        if (null != this.clazz) {
            super.url = this.clazz.getResource(this.path);
        } else if (null != this.classLoader) {
            super.url = this.classLoader.getResource(this.path);
        } else {
            super.url = ClassLoader.getSystemResource(this.path);
        }
        if (null == super.url) {
            throw new InstrumentException("Resource of path [{}] not exist!", this.path);
        }
    }

    @Override
    public String toString() {
        return (null == this.path) ? super.toString() : "classpath:" + this.path;
    }

    /**
     * 标准化Path格式
     *
     * @param path Path
     * @return 标准化后的path
     */
    private String normalizePath(String path) {
        // 标准化路径
        path = FileUtils.normalize(path);
        path = StringUtils.removePrefix(path, Symbol.SLASH);

        Assert.isFalse(FileUtils.isAbsolutePath(path), "Path [{}] must be a relative path !", path);
        return path;
    }

}
