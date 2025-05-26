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
/**
 * MAC，全称为“Message Authentication Code”，中文名“消息鉴别码”。
 *
 * <p>
 * HMAC，全称为“Hash Message Authentication Code”，中文名“散列消息鉴别码” 主要是利用哈希算法，以一个密钥和一个消息为输入，生成一个消息摘要作为输出。 一般的，消息鉴别码用于验证传输于两个共
 * 同享有一个密钥的单位之间的消息。 HMAC 可以与任何迭代散列函数捆绑使用。MD5 和 SHA-1 就是这种散列函数。HMAC 还可以使用一个用于计算和确认消息鉴别值的密钥。
 * </p>
 *
 * <pre>
 *     {@code
 *         MacEngineFactory
 *               ||(创建)
 *           MacEngine----------------（包装）-----------------> Mac
 *          _____|_______________                                |
 *         /                     \                              HMac
 *  JCEMacEngine             BCMacEngine
 *                            /        \
 *                   BCHMacEngine  CBCBlockCipherMacEngine
 *                                          |
 *                                     SM4MacEngine

 * }
 * </pre>
 * <p>
 * 通过MacEngine，封装支持了BouncyCastle和JCE实现的一些MAC算法，通过MacEngineFactory自动根据算法名称创建对应对象。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.crypto.builtin.digest.mac;
