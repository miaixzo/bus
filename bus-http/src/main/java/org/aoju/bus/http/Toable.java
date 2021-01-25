/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http;

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.http.metric.Array;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public interface Toable {

    /**
     * @return 消息体转字节流
     */
    InputStream toByteStream();

    /**
     * @return 消息体转字节数组
     */
    byte[] toBytes();

    /**
     * @return ByteString
     */
    ByteString toByteString();

    /**
     * @return 消息体转字符流
     */
    Reader toCharStream();

    /**
     * @return 消息体转字符串
     */
    String toString();

    /**
     * @return 消息体转 Mapper 对象（不想定义 Java Bean 时使用）
     */
    Wapper toWapper();

    /**
     * @return 消息体转 Array 数组（不想定义 Java Bean 时使用）
     */
    Array toArray();

    /**
     * @param <T>  目标泛型
     * @param type 目标类型
     * @return 报文体Json文本转JavaBean
     */
    <T> T toBean(Class<T> type);

    /**
     * @param <T>  目标泛型
     * @param type 目标类型
     * @return 报文体Json文本转JavaBean列表
     */
    <T> List<T> toList(Class<T> type);

}
