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
package org.miaixz.bus.core.io.watcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * 观察者(监视器)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Watcher {

    /**
     * 文件创建时执行的方法
     *
     * @param event       事件
     * @param currentPath 事件发生的当前Path路径
     */
    void onCreate(WatchEvent<?> event, Path currentPath);

    /**
     * 文件修改时执行的方法
     * 文件修改可能触发多次
     *
     * @param event       事件
     * @param currentPath 事件发生的当前Path路径
     */
    void onModify(WatchEvent<?> event, Path currentPath);

    /**
     * 文件删除时执行的方法
     *
     * @param event       事件
     * @param currentPath 事件发生的当前Path路径
     */
    void onDelete(WatchEvent<?> event, Path currentPath);

    /**
     * 事件丢失或出错时执行的方法
     *
     * @param event       事件
     * @param currentPath 事件发生的当前Path路径
     */
    void onOverflow(WatchEvent<?> event, Path currentPath);

}
