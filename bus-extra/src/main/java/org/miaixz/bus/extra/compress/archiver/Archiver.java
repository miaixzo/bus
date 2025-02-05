/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.compress.archiver;

import java.io.Closeable;
import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 数据归档封装，归档即将几个文件或目录打成一个压缩包
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Archiver extends Closeable {

    /**
     * 将文件或目录加入归档，目录采取递归读取方式按照层级加入
     *
     * @param file 文件或目录
     * @return this
     */
    default Archiver add(final File file) {
        return add(file, null);
    }

    /**
     * 将文件或目录加入归档，目录采取递归读取方式按照层级加入
     *
     * @param file      文件或目录
     * @param predicate 文件过滤器，指定哪些文件或目录可以加入，{@link Predicate#test(Object)}为{@code true}时加入，null表示全部加入
     * @return this
     */
    default Archiver add(final File file, final Predicate<File> predicate) {
        return add(file, null, predicate);
    }

    /**
     * 将文件或目录加入归档包，目录采取递归读取方式按照层级加入
     *
     * @param file   文件或目录
     * @param path   文件或目录的初始路径，null表示位于根路径
     * @param filter 文件过滤器，指定哪些文件或目录可以加入，{@link Predicate#test(Object)}为{@code true}保留，null表示全部加入
     * @return this
     */
    default Archiver add(final File file, final String path, final Predicate<File> filter) {
        return add(file, path, Function.identity(), filter);
    }

    /**
     * 将文件或目录加入归档包，目录采取递归读取方式按照层级加入
     *
     * @param file           文件或目录
     * @param path           文件或目录的初始路径，null表示位于根路径
     * @param fileNameEditor 文件名编辑器
     * @param filter         文件过滤器，指定哪些文件或目录可以加入，{@link Predicate#test(Object)}为{@code true}保留，null表示全部加入
     * @return this
     */
    Archiver add(File file, String path, Function<String, String> fileNameEditor, Predicate<File> filter);

    /**
     * 结束已经增加的文件归档，此方法不会关闭归档流，可以继续添加文件
     *
     * @return this
     */
    Archiver finish();

    /**
     * 无异常关闭
     */
    @Override
    void close();
}
