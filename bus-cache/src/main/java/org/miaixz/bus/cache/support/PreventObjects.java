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
package org.miaixz.bus.cache.support;

import java.io.Serial;
import java.io.Serializable;

/**
 * 防击穿对象工具类
 * <p>
 * 提供防击穿对象的创建和判断功能，用于缓存系统中防止缓存击穿问题。 缓存击穿是指大量请求同时查询一个不存在的缓存数据，导致这些请求直接穿透缓存访问数据库。 通过在缓存中存储特殊的防击穿对象，可以有效减少对数据库的访问压力。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PreventObjects {

    /**
     * 获取防击穿对象
     * <p>
     * 返回一个单例的防击穿对象，用于标识缓存中的空值或不存在的情况
     * </p>
     *
     * @return 防击穿对象
     */
    public static Object getPreventObject() {
        return PreventObject.INSTANCE;
    }

    /**
     * 判断对象是否为防击穿对象
     *
     * @param object 要判断的对象
     * @return 如果是防击穿对象则返回true，否则返回false
     */
    public static boolean isPrevent(Object object) {
        return object == PreventObject.INSTANCE || object instanceof PreventObject;
    }

    /**
     * 防击穿对象内部类
     * <p>
     * 实现Serializable接口，确保可以被序列化存储到缓存中 使用单例模式，确保全局只有一个实例
     * </p>
     */
    private static final class PreventObject implements Serializable {

        /**
         * 序列化版本号
         */
        @Serial
        private static final long serialVersionUID = 2852290208329L;

        /**
         * 单例实例
         */
        private static final PreventObject INSTANCE = new PreventObject();
    }

}
