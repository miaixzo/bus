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
package org.miaixz.bus.extra.compress.extractor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.function.Predicate;

/**
 * 归档数据解包封装，用于将zip、tar等包解包为文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Extractor extends Closeable {

    /**
     * 释放（解压）到指定目录，结束后自动关闭流，此方法只能调用一次
     *
     * @param targetDir 目标目录
     */
    default void extract(final File targetDir) {
        extract(targetDir, null);
    }

    /**
     * 释放（解压）到指定目录，结束后自动关闭流，此方法只能调用一次
     *
     * @param targetDir 目标目录
     * @param predicate 解压文件过滤器，用于指定需要释放的文件，{@code null}表示不过滤。{@link Predicate#test(Object)}为{@code true}时释放。
     */
    void extract(File targetDir, Predicate<ArchiveEntry> predicate);

    /**
     * 获取指定名称的文件流
     *
     * @param entryName entry名称
     * @return 文件流，无文件返回{@code null}
     */
    default InputStream get(final String entryName) {
        return getFirst((entry) -> StringKit.equals(entryName, entry.getName()));
    }

    /**
     * 获取满足指定过滤要求的压缩包内的第一个文件流
     *
     * @param predicate 用于指定需要释放的文件，null表示不过滤。当{@link Predicate#test(Object)}为{@code true}返回对应流。
     * @return 满足过滤要求的第一个文件的流, 无满足条件的文件返回{@code null}
     */
    InputStream getFirst(final Predicate<ArchiveEntry> predicate);

    /**
     * 无异常关闭
     */
    @Override
    void close();
}
