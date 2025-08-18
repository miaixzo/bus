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
package org.miaixz.bus.cache.support.serialize;

import org.miaixz.bus.logger.Logger;

/**
 * 抽象序列化器
 * <p>
 * 提供序列化和反序列化的基本框架，包含异常处理和日志记录功能。 子类需要实现具体的序列化和反序列化逻辑。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSerializer implements BaseSerializer {

    /**
     * 执行序列化操作
     *
     * @param object 要序列化的对象
     * @return 序列化后的字节数组
     * @throws Throwable 可能抛出的异常
     */
    protected abstract byte[] doSerialize(Object object) throws Throwable;

    /**
     * 执行反序列化操作
     *
     * @param bytes 要反序列化的字节数组
     * @return 反序列化后的对象
     * @throws Throwable 可能抛出的异常
     */
    protected abstract Object doDeserialize(byte[] bytes) throws Throwable;

    /**
     * 序列化对象
     *
     * @param <T>    对象类型
     * @param object 要序列化的对象
     * @return 序列化后的字节数组，如果对象为null或序列化失败则返回null
     */
    @Override
    public <T> byte[] serialize(T object) {
        if (null == object) {
            return null;
        }
        try {
            return doSerialize(object);
        } catch (Throwable t) {
            Logger.error("{} serialize error.", this.getClass().getName(), t);
            return null;
        }
    }

    /**
     * 反序列化字节数组
     *
     * @param <T>   对象类型
     * @param bytes 要反序列化的字节数组
     * @return 反序列化后的对象，如果字节数组为null或反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try {
            return (T) doDeserialize(bytes);
        } catch (Throwable t) {
            Logger.error("{} deserialize error.", this.getClass().getName(), t);
            return null;
        }
    }

}