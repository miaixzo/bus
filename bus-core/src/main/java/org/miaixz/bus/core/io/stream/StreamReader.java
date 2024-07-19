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
package org.miaixz.bus.core.io.stream;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream}读取器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamReader {

    private final InputStream in;
    private final boolean closeAfterRead;

    /**
     * 构造
     *
     * @param in             {@link InputStream}
     * @param closeAfterRead 读取结束后是否关闭输入流
     */
    public StreamReader(final InputStream in, final boolean closeAfterRead) {
        this.in = in;
        this.closeAfterRead = closeAfterRead;
    }

    /**
     * 创建读取器
     *
     * @param in             {@link InputStream}
     * @param closeAfterRead 读取结束后是否关闭输入流
     * @return StreamReader
     */
    public static StreamReader of(final InputStream in, final boolean closeAfterRead) {
        return new StreamReader(in, closeAfterRead);
    }

    /**
     * 从流中读取bytes
     *
     * @return bytes
     * @throws InternalException IO异常
     */
    public byte[] readBytes() throws InternalException {
        return readBytes(-1);
    }

    /**
     * 读取指定长度的byte数组
     *
     * @param length 长度，小于0表示读取全部
     * @return bytes
     * @throws InternalException IO异常
     */
    public byte[] readBytes(final int length) throws InternalException {
        final InputStream in = this.in;
        if (null == in || length == 0) {
            return new byte[0];
        }
        return read(length).toByteArrayZeroCopyIfPossible();
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后可选是否关闭流
     *
     * @return 输出流
     * @throws InternalException IO异常
     */
    public FastByteArrayOutputStream read() throws InternalException {
        return read(-1);
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后可选是否关闭流
     *
     * @param limit 限制最大拷贝长度，-1表示无限制
     * @return 输出流
     * @throws InternalException IO异常
     */
    public FastByteArrayOutputStream read(final int limit) throws InternalException {
        final InputStream in = this.in;
        final FastByteArrayOutputStream out;
        if (in instanceof FileInputStream) {
            // 文件流的长度是可预见的，此时直接读取效率更高
            try {
                int length = in.available();
                if (limit > 0 && limit < length) {
                    length = limit;
                }
                out = new FastByteArrayOutputStream(length);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            out = new FastByteArrayOutputStream();
        }
        try {
            IoKit.copy(in, out, Normal._8192, limit, (StreamProgress) null);
        } finally {
            if (closeAfterRead) {
                IoKit.close(in);
            }
        }
        return out;
    }

    /**
     * 从流中读取对象，即对象的反序列化
     *
     * <p>注意！！！ 此方法不会检查反序列化安全，可能存在反序列化漏洞风险！！！</p>
     *
     * <p>
     * 此方法使用了{@link ValidateObjectInputStream}中的黑白名单方式过滤类，用于避免反序列化漏洞
     * 通过构造{@link ValidateObjectInputStream}，调用{@link ValidateObjectInputStream#accept(Class[])}
     * 或者{@link ValidateObjectInputStream#refuse(Class[])}方法添加可以被序列化的类或者禁止序列化的类。
     * </p>
     *
     * @param <T>           读取对象的类型
     * @param acceptClasses 读取对象类型
     * @return 输出流
     * @throws InternalException IO异常
     * @throws InternalException ClassNotFoundException包装
     */
    public <T> T readObject(final Class<?>... acceptClasses) throws InternalException {
        final InputStream in = this.in;
        if (null == in) {
            return null;
        }

        // 转换
        final ValidateObjectInputStream validateIn;
        if (in instanceof ValidateObjectInputStream) {
            validateIn = (ValidateObjectInputStream) in;
            validateIn.accept(acceptClasses);
        } else {
            try {
                validateIn = new ValidateObjectInputStream(in, acceptClasses);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }

        // 读取
        try {
            return (T) validateIn.readObject();
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

}
