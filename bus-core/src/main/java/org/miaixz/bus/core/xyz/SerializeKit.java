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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 * 序列化工具类
 * 注意！此工具类依赖于JDK的序列化机制，某些版本的JDK中可能存在远程注入漏洞。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SerializeKit {

    /**
     * 序列化后拷贝流的方式克隆
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @throws InternalException IO异常和ClassNotFoundException封装
     */
    public static <T> T clone(final T obj) {
        if (!(obj instanceof Serializable)) {
            return null;
        }
        return deserialize(serialize(obj));
    }

    /**
     * 序列化
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 要被序列化的对象
     * @return 序列化后的字节码
     */
    public static <T> byte[] serialize(final T obj) {
        if (!(obj instanceof Serializable)) {
            return null;
        }
        final FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream();
        IoKit.writeObjects(byteOut, false, obj);
        return byteOut.toByteArray();
    }

    /**
     * 反序列化
     * 对象必须实现Serializable接口
     *
     * <p>
     * 注意！！！ 此方法不会检查反序列化安全，可能存在反序列化漏洞风险！！！
     * </p>
     *
     * @param <T>           对象类型
     * @param bytes         反序列化的字节码
     * @param acceptClasses 读取对象类型
     * @return 反序列化后的对象
     */
    public static <T> T deserialize(final byte[] bytes, final Class<?>... acceptClasses) {
        return IoKit.readObject(new ByteArrayInputStream(bytes), acceptClasses);
    }

}
