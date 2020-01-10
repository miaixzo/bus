/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.metric.magic;

/**
 * 负责加解密
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public interface Safety {

    /**
     * AES文本加密
     *
     * @param content  明文
     * @param password 密码
     * @return 返回16进制内容
     * @throws Exception 异常
     */
    String aesEncryptToHex(String content, String password) throws Exception;

    /**
     * AES文本解密
     *
     * @param hex      待解密文本,16进制内容
     * @param password 密码
     * @return 返回明文
     * @throws Exception 异常
     */
    String aesDecryptFromHex(String hex, String password) throws Exception;

    /**
     * AES文本加密
     *
     * @param content  明文
     * @param password 密码
     * @return 返回base64内容
     * @throws Exception 异常
     */
    String aesEncryptToBase64String(String content, String password) throws Exception;

    /**
     * AES文本解密
     *
     * @param base64String 待解密文本,16进制内容
     * @param password     密码
     * @return 返回明文
     * @throws Exception 异常
     */
    String aesDecryptFromBase64String(String base64String, String password) throws Exception;

    /**
     * RSA私钥解密
     *
     * @param data       解密内容
     * @param privateKey 私钥
     * @return 返回明文
     * @throws Exception 异常
     */
    String rsaDecryptByPrivateKey(String data, String privateKey) throws Exception;

    /**
     * RSA私钥加密
     *
     * @param data       明文
     * @param privateKey 私钥
     * @return 返回密文
     * @throws Exception 异常
     */
    String rsaEncryptByPrivateKey(String data, String privateKey) throws Exception;

    /**
     * md5加密,全部小写
     *
     * @param value 数据
     * @return 返回md5内容
     */
    String md5(String value);

}
