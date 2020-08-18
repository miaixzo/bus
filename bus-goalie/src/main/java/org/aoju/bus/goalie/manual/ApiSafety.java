/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.goalie.manual;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.crypto.Builder;
import org.aoju.bus.crypto.asymmetric.KeyType;

/**
 * 负责各类加解密
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public class ApiSafety implements Safety {

    @Override
    public String aesEncryptToHex(String content, String password) {
        return Builder.aes(Builder.generateKey(password).getEncoded()).encryptHex(content);
    }

    @Override
    public String aesDecryptFromHex(String hex, String password) {
        return Builder.aes(Builder.generateKey(password).getEncoded()).decryptStr(hex);
    }

    @Override
    public String aesEncryptToBase64String(String content, String password) {
        return Builder.aes(Builder.generateKey(password).getEncoded()).encryptBase64(content);
    }

    @Override
    public String aesDecryptFromBase64String(String content, String password) {
        return StringKit.toString(Builder.aes(Builder.generateKey(password).getEncoded()).decrypt(content), Charset.DEFAULT_UTF_8);
    }

    @Override
    public String rsaDecryptByPrivateKey(String data, String privateKey) {
        return StringKit.toString(Builder.rsa(privateKey, null).decrypt(data, KeyType.PrivateKey), Charset.DEFAULT_UTF_8);
    }

    @Override
    public String rsaEncryptByPrivateKey(String data, String privateKey) {
        return StringKit.toString(Builder.rsa(privateKey, null).encrypt(data, KeyType.PrivateKey), Charset.DEFAULT_UTF_8);
    }

    @Override
    public String md5(String value) {
        return Builder.md5Hex(value);
    }

}
