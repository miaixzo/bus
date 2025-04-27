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
package org.miaixz.bus.logger;

import org.miaixz.bus.core.instance.Instances;

/**
 * 日志工厂接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Factory {

    /**
     * 获取日志框架名，用于打印当前所用日志框架
     *
     * @return 日志框架名
     */
    String getName();

    /**
     * 创建日志对象
     *
     * @param name 日志对象名
     * @return 日志对象
     */
    Provider create(String name);

    /**
     * 创建日志对象
     *
     * @param clazz 日志对应类
     * @return 日志对象
     */
    Provider create(Class<?> clazz);

    /**
     * 获得日志对象（单例）
     *
     * @param name 日志对象名
     * @return 日志对象
     */
    default Provider getProvider(final String name) {
        return Instances.get(getName() + name, () -> create(name));
    }

    /**
     * 获得日志对象（单例）
     *
     * @param clazz 日志对应类
     * @return 日志对象
     */
    default Provider getProvider(final Class<?> clazz) {
        return Instances.get(getName() + clazz.getName(), () -> create(clazz));
    }

}
