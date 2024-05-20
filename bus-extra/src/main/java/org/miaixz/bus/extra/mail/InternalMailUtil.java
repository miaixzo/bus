/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.extra.mail;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 邮件内部工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InternalMailUtil {

    /**
     * 将多个字符串邮件地址转为{@link InternetAddress}列表
     * 单个字符串地址可以是多个地址合并的字符串
     *
     * @param addrStrs 地址数组
     * @param charset  编码（主要用于中文用户名的编码）
     * @return 地址数组
     */
    public static InternetAddress[] parseAddressFromStrs(final String[] addrStrs, final Charset charset) {
        final List<InternetAddress> resultList = new ArrayList<>(addrStrs.length);
        InternetAddress[] addrs;
        for (final String addrStr : addrStrs) {
            addrs = parseAddress(addrStr, charset);
            if (ArrayKit.isNotEmpty(addrs)) {
                Collections.addAll(resultList, addrs);
            }
        }
        return resultList.toArray(new InternetAddress[0]);
    }

    /**
     * 解析第一个地址
     *
     * @param address 地址字符串
     * @param charset 编码，{@code null}表示使用系统属性定义的编码或系统编码
     * @return 地址列表
     */
    public static InternetAddress parseFirstAddress(final String address, final Charset charset) {
        final InternetAddress[] internetAddresses = parseAddress(address, charset);
        if (ArrayKit.isEmpty(internetAddresses)) {
            try {
                return new InternetAddress(address);
            } catch (final AddressException e) {
                throw new InternalException(e);
            }
        }
        return internetAddresses[0];
    }

    /**
     * 将一个地址字符串解析为多个地址
     * 地址间使用" "、","、";"分隔
     *
     * @param address 地址字符串
     * @param charset 编码，{@code null}表示使用系统属性定义的编码或系统编码
     * @return 地址列表
     */
    public static InternetAddress[] parseAddress(final String address, final Charset charset) {
        final InternetAddress[] addresses;
        try {
            addresses = InternetAddress.parse(address);
        } catch (final AddressException e) {
            throw new InternalException(e);
        }
        //编码用户名
        if (ArrayKit.isNotEmpty(addresses)) {
            final String charsetStr = null == charset ? null : charset.name();
            for (final InternetAddress internetAddress : addresses) {
                try {
                    internetAddress.setPersonal(internetAddress.getPersonal(), charsetStr);
                } catch (final UnsupportedEncodingException e) {
                    throw new InternalException(e);
                }
            }
        }

        return addresses;
    }

    /**
     * 编码中文字符
     * 编码失败返回原字符串
     *
     * @param text    被编码的文本
     * @param charset 编码
     * @return 编码后的结果
     */
    public static String encodeText(final String text, final Charset charset) {
        try {
            return MimeUtility.encodeText(text, charset.name(), null);
        } catch (final UnsupportedEncodingException e) {
            // ignore
        }
        return text;
    }

}
