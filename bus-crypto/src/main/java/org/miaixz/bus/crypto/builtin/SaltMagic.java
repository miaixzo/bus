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
package org.miaixz.bus.crypto.builtin;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 加盐值魔数 用于在OpenSSL生成的密文中，提取加盐值等相关信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SaltMagic {

    /**
     * 加盐值长度
     */
    public static final byte SALT_LEN = 8;
    /**
     * OpenSSL's magic initial bytes.
     */
    public static final byte[] SALTED_MAGIC = "Salted__".getBytes(StandardCharsets.US_ASCII);

    /**
     * 获取魔术值和随机盐的长度：16（128位）
     */
    public static final int MAGIC_SALT_LENGTH = SALTED_MAGIC.length + SALT_LEN;

    /**
     * 获取去除头部盐的加密数据
     *
     * @param encryptedData 密文
     * @return 实际密文
     */
    public static byte[] getData(final byte[] encryptedData) {
        if (ArrayKit.startWith(encryptedData, SALTED_MAGIC)) {
            return Arrays.copyOfRange(encryptedData, SALTED_MAGIC.length + SALT_LEN, encryptedData.length);
        }
        return encryptedData;
    }

    /**
     * 获取流中的加盐值 不关闭流
     *
     * @param in 流
     * @return salt
     * @throws InternalException IO异常
     */
    public static byte[] getSalt(final InputStream in) throws InternalException {
        final byte[] headerBytes = new byte[SALTED_MAGIC.length];

        try {
            final int readHeaderSize = in.read(headerBytes);
            if (readHeaderSize < headerBytes.length || !Arrays.equals(SALTED_MAGIC, headerBytes)) {
                throw new InternalException("Unexpected magic header " + StringKit.toString(headerBytes));
            }

            final byte[] salt = new byte[SALT_LEN];
            final int readSaltSize = in.read(salt);
            if (readSaltSize < salt.length) {
                throw new InternalException("Unexpected salt: " + StringKit.toString(salt));
            }
            return salt;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取8位salt随机数
     *
     * @param encryptedData 密文
     * @return salt随机数
     */
    public static byte[] getSalt(final byte[] encryptedData) {
        if (ArrayKit.startWith(encryptedData, SALTED_MAGIC)) {
            return Arrays.copyOfRange(encryptedData, SALTED_MAGIC.length, MAGIC_SALT_LENGTH);
        }
        return null;
    }

    /**
     * 为加密后的数据添加Magic头，生成的密文格式为：
     * 
     * <pre>
     * Salted__[salt][data]
     * </pre>
     *
     * @param data 数据
     * @param salt 加盐值，必须8位，{@code null}表示返回原文
     * @return 密文
     */
    public static byte[] addMagic(final byte[] data, final byte[] salt) {
        if (null == salt) {
            return data;
        }
        Assert.isTrue(SALT_LEN == salt.length);
        return ByteKit.concat(SALTED_MAGIC, salt, data);
    }

    /**
     * 获取Magic头，生成的密文格式为：
     * 
     * <pre>
     * Salted__[salt]
     * </pre>
     *
     * @param salt 加盐值，必须8位，不能为{@code null}
     * @return Magic头
     */
    public static byte[] getSaltedMagic(final byte[] salt) {
        return ByteKit.concat(SALTED_MAGIC, salt);
    }

}
