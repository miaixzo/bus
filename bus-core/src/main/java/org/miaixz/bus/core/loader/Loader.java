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

import org.miaixz.bus.core.io.resource.Resource;

import java.io.IOException;
import java.util.Enumeration;

/**
 * 资源加载器,充分采用惰性加载的逻辑,
 * 让资源的加载延后到{@link Enumeration#hasMoreElements()}
 * 调用时,避免无用的提前全部预加载
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Loader {

    /**
     * 加载指定路径的所有资源,等效于Loader.load(path, false, Filters.ALWAYS)的调用
     * 通常情况下不递归加载,但是子类可以改变此方法的行为,
     * 例如ANT风格路径的资源加载器可以根据传入表达式来判断是否递归加载
     *
     * @param path 资源路径
     * @return 资源对象
     * @throws IOException I/O 异常
     */
    Enumeration<Resource> load(String path) throws IOException;

    /**
     * 加载指定路径的所有资源,等效于Loader.load(path, recursively, Filters.ALWAYS)的调用
     *
     * @param path        资源路径
     * @param recursively 递归加载
     * @return 资源枚举器
     * @throws IOException I/O 异常
     */
    Enumeration<Resource> load(String path, boolean recursively) throws IOException;

    /**
     * 加载指定路径的所有满足过滤条件的资源,等效于Loader.load(path, true, boot)的调用
     *
     * @param path  资源路径
     * @param clazz 对象信息
     * @return 资源枚举器
     * @throws IOException I/O 异常
     */
    Enumeration<Resource> load(String path, Filter clazz) throws IOException;

    /**
     * 加载动态库信息
     * 例如: .dll/.so等
     *
     * @param path  资源路径
     * @param clazz 对象信息
     * @return 资源枚举器
     * @throws IOException I/O 异常
     */
    default Enumeration<Resource> load(String path, Class<?> clazz) throws IOException {
        return load(path);
    }

    /**
     * 加载指定路径的所有满足过滤条件的资源
     *
     * @param path        资源路径
     * @param recursively 递归加载
     * @param filter      过滤器
     * @return 资源枚举器
     * @throws IOException I/O 异常
     */
    Enumeration<Resource> load(String path, boolean recursively, Filter filter) throws IOException;

}
