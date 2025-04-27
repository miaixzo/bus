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
package org.miaixz.bus.core.codec.binary;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.miaixz.bus.core.codec.binary.provider.Base58Provider;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Base58工具类，提供Base58的编码和解码方案 参考：
 * <a href="https://github.com/Anujraval24/Base58Encoding">https://github.com/Anujraval24/Base58Encoding</a>
 * 规范见：<a href="https://en.bitcoin.it/wiki/Base58Check_encoding">https://en.bitcoin.it/wiki/Base58Check_encoding</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58 {

    private static final int CHECKSUM_SIZE = 4;

    /**
     * Base58编码 包含版本位和校验位
     *
     * @param version 编码版本，{@code null}表示不包含版本位
     * @param data    被编码的数组，添加校验和。
     * @return 编码后的字符串
     */
    public static String encodeChecked(final Integer version, final byte[] data) {
        return encode(addChecksum(version, data));
    }

    /**
     * Base58编码
     *
     * @param data 被编码的数据，不带校验和。
     * @return 编码后的字符串
     */
    public static String encode(final byte[] data) {
        return Base58Provider.INSTANCE.encode(data);
    }

    /**
     * Base58解码 解码包含标志位验证和版本呢位去除
     *
     * @param encoded 被解码的base58字符串
     * @return 解码后的bytes
     * @throws ValidateException 标志位验证错误抛出此异常
     */
    public static byte[] decodeChecked(final CharSequence encoded) throws ValidateException {
        try {
            return decodeChecked(encoded, true);
        } catch (final ValidateException ignore) {
            return decodeChecked(encoded, false);
        }
    }

    /**
     * Base58解码 解码包含标志位验证和版本呢位去除
     *
     * @param encoded     被解码的base58字符串
     * @param withVersion 是否包含版本位
     * @return 解码后的bytes
     * @throws ValidateException 标志位验证错误抛出此异常
     */
    public static byte[] decodeChecked(final CharSequence encoded, final boolean withVersion) throws ValidateException {
        final byte[] valueWithChecksum = decode(encoded);
        return verifyAndRemoveChecksum(valueWithChecksum, withVersion);
    }

    /**
     * Base58解码
     *
     * @param encoded 被编码的base58字符串
     * @return 解码后的bytes
     */
    public static byte[] decode(final CharSequence encoded) {
        return Base58Provider.INSTANCE.decode(encoded);
    }

    /**
     * 验证并去除验证位和版本位
     *
     * @param data        编码的数据
     * @param withVersion 是否包含版本位
     * @return 载荷数据
     */
    private static byte[] verifyAndRemoveChecksum(final byte[] data, final boolean withVersion) {
        final byte[] payload = Arrays.copyOfRange(data, withVersion ? 1 : 0, data.length - CHECKSUM_SIZE);
        final byte[] checksum = Arrays.copyOfRange(data, data.length - CHECKSUM_SIZE, data.length);
        final byte[] expectedChecksum = checksum(payload);
        if (!Arrays.equals(checksum, expectedChecksum)) {
            throw new ValidateException("Base58 check is invalid");
        }
        return payload;
    }

    /**
     * 数据 + 校验码
     *
     * @param version 版本，{@code null}表示不添加版本位
     * @param payload Base58数据（不含校验码）
     * @return Base58数据
     */
    private static byte[] addChecksum(final Integer version, final byte[] payload) {
        final byte[] addressBytes;
        if (null != version) {
            addressBytes = new byte[1 + payload.length + CHECKSUM_SIZE];
            addressBytes[0] = (byte) version.intValue();
            System.arraycopy(payload, 0, addressBytes, 1, payload.length);
        } else {
            addressBytes = new byte[payload.length + CHECKSUM_SIZE];
            System.arraycopy(payload, 0, addressBytes, 0, payload.length);
        }
        final byte[] checksum = checksum(payload);
        System.arraycopy(checksum, 0, addressBytes, addressBytes.length - CHECKSUM_SIZE, CHECKSUM_SIZE);
        return addressBytes;
    }

    /**
     * 获取校验码 计算规则为对数据进行两次sha256计算，然后取{@link #CHECKSUM_SIZE}长度
     *
     * @param data 数据
     * @return 校验码
     */
    private static byte[] checksum(final byte[] data) {
        final byte[] hash = hash256(hash256(data));
        return Arrays.copyOfRange(hash, 0, CHECKSUM_SIZE);
    }

    /**
     * 计算数据的SHA-256值
     *
     * @param data 数据
     * @return sha-256值
     */
    private static byte[] hash256(final byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
    }

}
