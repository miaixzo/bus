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
package org.miaixz.bus.core.io.compress;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Zip文件替换，用户替换源Zip文件，并生成新的文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipReplacer implements Closeable {

    private final ZipReader zipReader;
    private final boolean ignoreCase;

    private final Map<String, Resource> replacedResources = new HashMap<>();

    /**
     * 构造
     *
     * @param zipReader  ZipReader
     * @param ignoreCase 是否忽略path大小写
     */
    public ZipReplacer(final ZipReader zipReader, final boolean ignoreCase) {
        this.zipReader = zipReader;
        this.ignoreCase = ignoreCase;
    }

    /**
     * 判断路径是否相等
     *
     * @param entryPath  路径A
     * @param targetPath 路径B
     * @param ignoreCase 是否忽略大小写
     * @return ture 路径相等
     */
    private static boolean isSamePath(String entryPath, String targetPath, final boolean ignoreCase) {
        entryPath = StringKit.removePrefix(FileKit.normalize(entryPath), Symbol.SLASH);
        targetPath = StringKit.removePrefix(FileKit.normalize(targetPath), Symbol.SLASH);
        return StringKit.equals(entryPath, targetPath, ignoreCase);
    }

    /**
     * 增加替换的内容，如果路径不匹配，则不做替换，也不加入
     *
     * @param entryPath 路径
     * @param resource  被压缩的内容
     * @return this
     */
    public ZipReplacer addReplace(final String entryPath, final Resource resource) {
        replacedResources.put(entryPath, resource);
        return this;
    }

    /**
     * 写出到{@link ZipWriter}
     *
     * @param writer {@link ZipWriter}
     */
    public void write(final ZipWriter writer) {
        zipReader.read((entry) -> {
            String entryName;
            for (final String key : replacedResources.keySet()) {
                entryName = entry.getName();
                if (isSamePath(entryName, key, ignoreCase)) {
                    writer.add(key, replacedResources.get(key).getStream());
                } else {
                    writer.add(entryName, zipReader.get(entryName));
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        this.zipReader.close();
    }

}
