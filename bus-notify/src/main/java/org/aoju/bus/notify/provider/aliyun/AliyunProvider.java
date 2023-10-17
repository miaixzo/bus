/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.notify.provider.aliyun;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.notify.Builder;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.magic.Property;
import org.aoju.bus.notify.provider.AbstractProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 阿里云抽象类提供者
 *
 * @author Justubborn
 * @since Java 17+
 */
public class AliyunProvider<T extends Property, K extends Context> extends AbstractProvider<T, K> {

    /**
     * 发送成功后返回code
     */
    private static final String SUCCESS_RESULT = "OK";

    public AliyunProvider(K context) {
        super(context);
    }

    /**
     * pop编码
     *
     * @param value 原值
     * @return 编码值
     */
    protected String specialUrlEncode(String value) {
        return URLEncoder.encode(value, Charset.UTF_8)
                .replace(Symbol.PLUS, "%20")
                .replace(Symbol.STAR, "%2A")
                .replace("%7E", Symbol.TILDE);
    }

    /**
     * 构造签名
     *
     * @param params 参数
     * @return 签名值
     */
    protected String getSign(Map<String, String> params) {
        // 4. 参数KEY排序
        Map<String, String> map = new TreeMap<>(params);
        // 5. 构造待签名的字符串
        Iterator<String> it = map.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp
                    .append(Symbol.AND)
                    .append(specialUrlEncode(key))
                    .append(Symbol.EQUAL)
                    .append(specialUrlEncode(params.get(key)));
        }
        // 去除第一个多余的&符号
        String sortedQueryString = sortQueryStringTmp.substring(1);
        String stringToSign = Http.GET + Symbol.AND +
                specialUrlEncode(Symbol.SLASH) + Symbol.AND +
                specialUrlEncode(sortedQueryString);
        return sign(stringToSign);
    }

    /**
     * 密钥签名
     *
     * @param stringToSign 代签名字符串
     * @return 签名后字符串
     */
    protected String sign(String stringToSign) {
        try {
            Mac mac = Mac.getInstance(Algorithm.HMACSHA1.getValue());
            mac.init(new SecretKeySpec((context.getAppSecret() + Symbol.AND).getBytes(Charset.UTF_8), Algorithm.HMACSHA1.getValue()));
            byte[] signData = mac.doFinal(stringToSign.getBytes(Charset.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new InternalException("Aliyun specialUrlEncode error");
        }
    }

    protected Message checkResponse(String response) {
        String code = JsonKit.getValue(response, "Code");
        return Message.builder()
                .errcode(SUCCESS_RESULT.equals(code) ? Builder.ErrorCode.SUCCESS.getCode() : code)
                .errmsg(code).build();
    }

}
