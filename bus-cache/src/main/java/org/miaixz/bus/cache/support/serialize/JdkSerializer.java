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

import java.io.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;

/**
 * JDK原生序列化器
 * <p>
 * 基于Java原生序列化机制实现的序列化器，使用ObjectOutputStream和ObjectInputStream 进行对象的序列化和反序列化操作。要求被序列化的对象必须实现Serializable接口。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkSerializer extends AbstractSerializer {

    /**
     * 序列化对象到输出流
     *
     * @param object       要序列化的对象，必须实现Serializable接口
     * @param outputStream 输出流
     * @throws InternalException 如果序列化过程中发生I/O错误
     */
    private static void serialize(Serializable object, OutputStream outputStream) {
        if (null == outputStream) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(object);
            } catch (IOException e) {
                throw new InternalException(e);
            } finally {
                try {
                    if (null != out) {
                        out.close();
                    }
                } catch (IOException var10) {
                    // 忽略关闭流时的异常
                }
            }
        }
    }

    /**
     * 从输入流反序列化对象
     *
     * @param inputStream 输入流
     * @return 反序列化后的对象
     * @throws InternalException 如果反序列化过程中发生I/O错误或类未找到错误
     */
    private static Object deserialize(InputStream inputStream) {
        if (null == inputStream) {
            throw new IllegalArgumentException("The InputStream must not be null");
        } else {
            ObjectInputStream in = null;
            Object result;
            try {
                in = new ObjectInputStream(inputStream);
                result = in.readObject();
            } catch (ClassCastException | IOException | ClassNotFoundException ce) {
                throw new InternalException(ce);
            } finally {
                try {
                    if (null != in) {
                        in.close();
                    }
                } catch (IOException e) {
                    Logger.error(e, "close stream failed when deserialize error: ", e.getMessage());
                }
            }
            return result;
        }
    }

    /**
     * 执行序列化操作
     * <p>
     * 使用JDK原生序列化将对象转换为字节数组
     * </p>
     *
     * @param object 要序列化的对象，必须实现Serializable接口
     * @return 序列化后的字节数组
     * @throws InternalException 如果序列化过程中发生错误
     */
    @Override
    protected byte[] doSerialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Normal._512);
        serialize((Serializable) object, baos);
        return baos.toByteArray();
    }

    /**
     * 执行反序列化操作
     * <p>
     * 使用JDK原生反序列化将字节数组转换为对象
     * </p>
     *
     * @param bytes 要反序列化的字节数组
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 如果字节数组为null
     * @throws InternalException        如果反序列化过程中发生错误
     */
    @Override
    protected Object doDeserialize(byte[] bytes) {
        if (null == bytes) {
            throw new IllegalArgumentException("The byte[] must not be null");
        } else {
            return deserialize(new ByteArrayInputStream(bytes));
        }
    }

}
