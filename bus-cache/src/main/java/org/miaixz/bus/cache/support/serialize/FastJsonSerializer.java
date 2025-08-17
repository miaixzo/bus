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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * FastJson序列化器
 * <p>
 * 基于FastJson实现的序列化器，将对象序列化为JSON字符串，然后再转换为字节数组。 反序列化时，将字节数组转换为JSON字符串，然后再转换为对象。 这种序列化方式具有良好的可读性和跨语言兼容性，但性能相对二进制序列化较低。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastJsonSerializer extends AbstractSerializer {

    /**
     * 目标类型，用于反序列化时确定对象类型
     */
    private Class<?> type;

    /**
     * 构造方法
     *
     * @param type 目标类型，用于反序列化时确定对象类型
     */
    public FastJsonSerializer(Class<?> type) {
        this.type = type;
    }

    /**
     * 执行序列化操作
     * <p>
     * 将对象序列化为JSON字符串，然后转换为字节数组
     * </p>
     *
     * @param object 要序列化的对象
     * @return 序列化后的字节数组
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        String json = JsonKit.toJsonString(object);
        return json.getBytes(Charset.DEFAULT_UTF_8);
    }

    /**
     * 执行反序列化操作
     * <p>
     * 将字节数组转换为JSON字符串，然后反序列化为对象
     * </p>
     *
     * @param bytes 要反序列化的字节数组
     * @return 反序列化后的对象
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        String json = new String(bytes, 0, bytes.length, Charset.DEFAULT_UTF_8);
        return JsonKit.toPojo(json, type);
    }

}