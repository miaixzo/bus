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

import java.security.MessageDigest;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.MD5;

/**
 * OpenSSL中加盐解析器 参考：
 * 
 * <pre>
 *     https://stackoverflow.com/questions/11783062/how-to-decrypt-file-in-java-encrypted-with-openssl-command-using-aes
 *     https://stackoverflow.com/questions/32508961/java-equivalent-of-an-openssl-aes-cbc-encryption
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SaltParser {

    private final MessageDigest digest;
    private final int keyLength;
    private final int ivLength;
    private String algorithm;

    /**
     * 构造
     *
     * @param digest    {@link MessageDigest}
     * @param keyLength 密钥长度
     * @param algorithm 算法
     */
    public SaltParser(final MessageDigest digest, final int keyLength, final String algorithm) {
        int ivLength = 16;
        if (StringKit.containsIgnoreCase(algorithm, "des")) {
            ivLength = 8;
        }
        this.digest = digest;
        this.keyLength = keyLength;
        this.ivLength = ivLength;
        this.algorithm = algorithm;
    }

    /**
     * 构造
     *
     * @param digest    {@link MessageDigest}
     * @param keyLength 密钥长度
     * @param ivLength  IV长度
     */
    public SaltParser(final MessageDigest digest, final int keyLength, final int ivLength) {
        this.digest = digest;
        this.keyLength = keyLength;
        this.ivLength = ivLength;
    }

    /**
     * 创建MD5 OpenSSLSaltParser
     *
     * @param keyLength 密钥长度
     * @param algorithm 算法
     * @return OpenSSLSaltParser
     */
    public static SaltParser ofMd5(final int keyLength, final String algorithm) {
        return of(MD5.of().getRaw(), keyLength, algorithm);
    }

    /**
     * 创建OpenSSLSaltParser
     *
     * @param digest    {@link MessageDigest}
     * @param keyLength 密钥长度
     * @param algorithm 算法
     * @return OpenSSLSaltParser
     */
    public static SaltParser of(final MessageDigest digest, final int keyLength, final String algorithm) {
        return new SaltParser(digest, keyLength, algorithm);
    }

    /**
     * 通过密钥和salt值，获取实际的密钥
     *
     * @param pass 密钥
     * @param salt 加盐值
     * @return 实际密钥
     */
    public byte[][] getKeyAndIV(final byte[] pass, final byte[] salt) {
        final byte[][] keyAndIvResult = new byte[2][];
        if (null == salt) {
            keyAndIvResult[0] = pass;
            return keyAndIvResult;
        }
        Assert.isTrue(SaltMagic.SALT_LEN == salt.length);

        final byte[] passAndSalt = ByteKit.concat(pass, salt);

        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        for (int i = 0; i < 3 && keyAndIv.length < keyLength + ivLength; i++) {
            final byte[] hashData = ByteKit.concat(hash, passAndSalt);
            hash = digest.digest(hashData);
            keyAndIv = ByteKit.concat(keyAndIv, hash);
        }

        keyAndIvResult[0] = Arrays.copyOfRange(keyAndIv, 0, keyLength);
        if (!StringKit.containsAnyIgnoreCase(algorithm, "RC", "DES")) {
            keyAndIvResult[1] = Arrays.copyOfRange(keyAndIv, keyLength, keyLength + ivLength);
        }
        return keyAndIvResult;
    }

}
