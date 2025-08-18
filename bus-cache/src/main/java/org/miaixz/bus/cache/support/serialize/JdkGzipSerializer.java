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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * JDK GZIP序列化器
 * <p>
 * 基于JDK原生序列化和GZIP压缩实现的序列化器。 先使用JDK原生序列化将对象转换为字节数组，然后使用GZIP进行压缩， 可以有效减少序列化后的数据大小，适用于存储大对象或需要节省存储空间的场景。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkGzipSerializer extends AbstractSerializer {

    /**
     * 执行序列化操作
     * <p>
     * 使用JDK原生序列化将对象转换为字节数组，然后使用GZIP进行压缩
     * </p>
     *
     * @param object 要序列化的对象
     * @return 序列化并压缩后的字节数组
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gzout = new GZIPOutputStream(bos);
                ObjectOutputStream out = new ObjectOutputStream(gzout)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    /**
     * 执行反序列化操作
     * <p>
     * 先使用GZIP解压字节数组，然后使用JDK原生反序列化将字节数组转换为对象
     * </p>
     *
     * @param bytes 要反序列化的字节数组
     * @return 反序列化后的对象
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                GZIPInputStream gzin = new GZIPInputStream(bis);
                ObjectInputStream ois = new ObjectInputStream(gzin)) {
            return ois.readObject();
        }
    }

}