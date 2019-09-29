/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.extra.mail;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ArrayUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件内部工具类
 *
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public class InternalMail {

    /**
     * 将多个字符串邮件地址转为{@link InternetAddress}列表
     * 单个字符串地址可以是多个地址合并的字符串
     *
     * @param addrStrs 地址数组
     * @param charset  编码（主要用于中文用户名的编码）
     * @return 地址数组
     */
    public static InternetAddress[] parseAddressFromStrs(String[] addrStrs, Charset charset) {
        final List<InternetAddress> resultList = new ArrayList<>(addrStrs.length);
        InternetAddress[] addrs;
        for (int i = 0; i < addrStrs.length; i++) {
            addrs = parseAddress(addrStrs[i], charset);
            if (ArrayUtils.isNotEmpty(addrs)) {
                for (int j = 0; j < addrs.length; j++) {
                    resultList.add(addrs[j]);
                }
            }
        }
        return resultList.toArray(new InternetAddress[resultList.size()]);
    }

    /**
     * 解析第一个地址
     *
     * @param address 地址字符串
     * @param charset 编码
     * @return 地址列表
     */
    public static InternetAddress parseFirstAddress(String address, Charset charset) {
        final InternetAddress[] internetAddresses = parseAddress(address, charset);
        if (ArrayUtils.isEmpty(internetAddresses)) {
            try {
                return new InternetAddress(address);
            } catch (AddressException e) {
                throw new InstrumentException(e);
            }
        }
        return internetAddresses[0];
    }

    /**
     * 将一个地址字符串解析为多个地址
     * 地址间使用" "、","、";"分隔
     *
     * @param address 地址字符串
     * @param charset 编码
     * @return 地址列表
     */
    public static InternetAddress[] parseAddress(String address, Charset charset) {
        InternetAddress[] addresses;
        try {
            addresses = InternetAddress.parse(address);
        } catch (AddressException e) {
            throw new InstrumentException(e);
        }
        //编码用户名
        if (ArrayUtils.isNotEmpty(addresses)) {
            for (InternetAddress internetAddress : addresses) {
                try {
                    internetAddress.setPersonal(internetAddress.getPersonal(), charset.name());
                } catch (UnsupportedEncodingException e) {
                    throw new InstrumentException(e);
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
    public static String encodeText(String text, Charset charset) {
        try {
            return MimeUtility.encodeText(text, charset.name(), null);
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return text;
    }
}
