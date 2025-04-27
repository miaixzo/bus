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
package org.miaixz.bus.core.io.watch;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * 监听事件类型枚举，包括：
 *
 * <pre>
 *      1. 事件丢失 OVERFLOW - StandardWatchEventKinds.OVERFLOW
 *      2. 修改事件 MODIFY   - StandardWatchEventKinds.ENTRY_MODIFY
 *      3. 创建事件 CREATE   - StandardWatchEventKinds.ENTRY_CREATE
 *      4. 删除事件 DELETE   - StandardWatchEventKinds.ENTRY_DELETE
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum WatchKind {

    /**
     * 事件丢失
     */
    OVERFLOW(StandardWatchEventKinds.OVERFLOW),
    /**
     * 修改事件
     */
    MODIFY(StandardWatchEventKinds.ENTRY_MODIFY),
    /**
     * 创建事件
     */
    CREATE(StandardWatchEventKinds.ENTRY_CREATE),
    /**
     * 删除事件
     */
    DELETE(StandardWatchEventKinds.ENTRY_DELETE);

    /**
     * 全部事件
     */
    public static final WatchEvent.Kind<?>[] ALL = { //
            OVERFLOW.getValue(), // 事件丢失
            MODIFY.getValue(), // 修改
            CREATE.getValue(), // 创建
            DELETE.getValue() // 删除
    };

    private final WatchEvent.Kind<?> value;

    /**
     * 构造
     *
     * @param value 事件类型
     */
    WatchKind(final WatchEvent.Kind<?> value) {
        this.value = value;
    }

    /**
     * 获取枚举对应的事件类型
     *
     * @return 事件类型值
     */
    public WatchEvent.Kind<?> getValue() {
        return this.value;
    }

}
