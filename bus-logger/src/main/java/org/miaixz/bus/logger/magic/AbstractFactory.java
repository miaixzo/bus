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
package org.miaixz.bus.logger.magic;

import org.miaixz.bus.logger.Factory;

/**
 * 抽象日志
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFactory implements Factory {

    /**
     * 日志框架名，用于打印当前所用日志框架
     */
    private final String name;

    /**
     * 构造
     *
     * @param name 日志框架名
     */
    public AbstractFactory(final String name) {
        this.name = name;
    }

    /**
     * 获取日志框架名，用于打印当前所用日志框架
     *
     * @return 日志框架名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 检查日志实现是否存在 此方法仅用于检查所提供的日志相关类是否存在，当传入的日志类类不存在时抛出ClassNotFoundException
     * 此方法的作用是在detectLogFactory方法自动检测所用日志时，如果实现类不存在，调用此方法会自动抛出异常，从而切换到下一种日志的检测。
     *
     * @param clazz 日志实现相关类
     */
    protected void exists(final Class<?> clazz) {
        // 不做任何操作
    }

}
