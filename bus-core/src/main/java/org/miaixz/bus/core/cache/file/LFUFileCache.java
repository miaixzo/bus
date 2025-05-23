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
package org.miaixz.bus.core.cache.file;

import java.io.File;
import java.io.Serial;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.cache.provider.LFUCache;

/**
 * 使用LFU缓存文件，以解决频繁读取文件引起的性能问题
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LFUFileCache extends AbstractFileCache {

    @Serial
    private static final long serialVersionUID = 2852238556777L;

    /**
     * 构造 最大文件大小为缓存容量的一半 默认无超时
     *
     * @param capacity 缓存容量
     */
    public LFUFileCache(final int capacity) {
        this(capacity, capacity / 2, 0);
    }

    /**
     * 构造 默认无超时
     *
     * @param capacity    缓存容量
     * @param maxFileSize 最大文件大小
     */
    public LFUFileCache(final int capacity, final int maxFileSize) {
        this(capacity, maxFileSize, 0);
    }

    /**
     * 构造
     *
     * @param capacity    缓存容量
     * @param maxFileSize 文件最大大小
     * @param timeout     默认超时时间，0表示无默认超时
     */
    public LFUFileCache(final int capacity, final int maxFileSize, final long timeout) {
        super(capacity, maxFileSize, timeout);
    }

    @Override
    protected Cache<File, byte[]> initCache() {
        return new LFUCache<>(LFUFileCache.this.capacity, LFUFileCache.this.timeout) {
            @Serial
            private static final long serialVersionUID = 2852368337873L;

            @Override
            public boolean isFull() {
                return LFUFileCache.this.usedSize > this.capacity;
            }

            @Override
            protected void onRemove(final File key, final byte[] cachedObject) {
                usedSize -= cachedObject.length;
            }
        };
    }

}
