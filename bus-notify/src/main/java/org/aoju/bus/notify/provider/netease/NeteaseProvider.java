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
package org.aoju.bus.notify.provider.netease;

import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.notify.Builder;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.magic.Property;
import org.aoju.bus.notify.provider.AbstractProvider;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 网易云抽象类
 *
 * @author Justubborn
 * @version 6.3.2
 * @since JDK1.8+
 */
public abstract class NeteaseProvider<T extends Property, K extends Context> extends AbstractProvider<T, K> {

    /**
     * 发送成功后返回code
     */
    private static final String SUCCESS_RESULT = "200";

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public NeteaseProvider(K properties) {
        super(properties);
    }

    private static String encode(String value) {
        if (null == value) {
            return null;
        }
        try {
            MessageDigest messageDigest
                    = MessageDigest.getInstance("sha1");
            messageDigest.update(value.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (byte aByte : bytes) {
            buf.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[aByte & 0x0f]);
        }
        return buf.toString();
    }

    protected HashMap<String, String> getPostHeader() {
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        HashMap<String, String> map = new HashMap<>();
        map.put("AppKey", properties.getAppKey());
        map.put("Nonce", properties.getAppNonce());
        map.put("CurTime", curTime);
        map.put("CheckSum", getCheckSum(curTime));
        return map;
    }

    public Message post(String routerUrl, Map<String, Object> map) {
        Map<String, String> header = getPostHeader();
        Logger.debug("netease send：{}", map);
        String response = Httpx.post(routerUrl, map, header);
        Logger.debug("netease result：{}", response);
        String code = JsonKit.getValue(response, "Code");
        return Message.builder()
                .errcode(SUCCESS_RESULT.equals(code) ? Builder.ErrorCode.SUCCESS.getCode() : code)
                .errmsg(JsonKit.getValue(response, "desc")).build();
    }

    /**
     * CheckSum
     *
     * @param curTime 时间
     * @return 结果
     */
    private String getCheckSum(String curTime) {
        return encode(properties.getAppSecret() + properties.getAppNonce() + curTime);
    }

}
