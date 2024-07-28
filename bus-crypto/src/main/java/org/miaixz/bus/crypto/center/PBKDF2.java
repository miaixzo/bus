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
package org.miaixz.bus.crypto.center;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.crypto.Keeper;

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2应用一个伪随机函数以导出密钥，PBKDF2简单而言就是将salted hash进行多次重复计算。 参考：https://blog.csdn.net/huoji555/article/details/83659687
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PBKDF2 {

    private String algorithm = Algorithm.PBKDF2WITHHMACSHA1.getValue();

    /**
     * 生成密文的长度
     */
    private int keyLength = 512;

    /**
     * 迭代次数
     */
    private int iterationCount = 1000;

    /**
     * 构造，算法PBKDF2WithHmacSHA1，盐长度16，密文长度512，迭代次数1000
     */
    public PBKDF2() {

    }

    /**
     * 构造
     *
     * @param algorithm      算法，一般为PBKDF2WithXXX
     * @param keyLength      生成密钥长度，默认512
     * @param iterationCount 迭代次数，默认1000
     */
    public PBKDF2(final String algorithm, final int keyLength, final int iterationCount) {
        this.algorithm = algorithm;
        this.keyLength = keyLength;
        this.iterationCount = iterationCount;
    }

    /**
     * 加密
     *
     * @param password 密码
     * @param salt     盐
     * @return 加密后的密码
     */
    public byte[] encrypt(final char[] password, final byte[] salt) {
        final PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        final SecretKey secretKey = Keeper.generateKey(algorithm, pbeKeySpec);
        return secretKey.getEncoded();
    }

    /**
     * 加密
     *
     * @param password 密码
     * @param salt     盐
     * @return 加密后的密码
     */
    public String encryptHex(final char[] password, final byte[] salt) {
        return HexKit.encodeString(encrypt(password, salt));
    }

}
