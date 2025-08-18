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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

/**
 * Hessian2序列化器
 * <p>
 * 基于Hessian2实现的序列化器，提供高效的二进制序列化功能。 Hessian是一种轻量级的二进制RPC协议，具有跨语言支持和较高的序列化效率。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Hessian2Serializer extends AbstractSerializer {

    /**
     * 执行序列化操作
     * <p>
     * 使用Hessian2将对象序列化为字节数组
     * </p>
     *
     * @param object 要序列化的对象
     * @return 序列化后的字节数组
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Hessian2Output out = new Hessian2Output(os);
            out.writeObject(object);
            os.close();
            return os.toByteArray();
        }
    }

    /**
     * 执行反序列化操作
     * <p>
     * 使用Hessian2将字节数组反序列化为对象
     * </p>
     *
     * @param bytes 要反序列化的字节数组
     * @return 反序列化后的对象
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            Hessian2Input in = new Hessian2Input(is);
            Object result = in.readObject();
            in.close();
            return result;
        }
    }

}