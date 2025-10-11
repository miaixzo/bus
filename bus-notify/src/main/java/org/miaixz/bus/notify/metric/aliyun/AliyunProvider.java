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
package org.miaixz.bus.notify.metric.aliyun;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Material;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Abstract provider for Alibaba Cloud notification services, handling common logic like signing requests.
 *
 * @param <T> The type of {@link Material} this provider handles.
 * @param <K> The type of {@link Context} this provider uses.
 * @author Justubborn
 * @since Java 17+
 */
public class AliyunProvider<T extends Material, K extends Context> extends AbstractProvider<T, K> {

    /**
     * The success result code returned by Alibaba Cloud services.
     */
    private static final String SUCCESS_RESULT = "OK";

    /**
     * Constructs an {@code AliyunProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public AliyunProvider(K context) {
        super(context);
    }

    /**
     * URL-encodes a string value according to Alibaba Cloud's specific requirements (POP encoding). This involves
     * replacing '+' with '%20', '*' with '%2A', and '~' with '%7E'.
     *
     * @param value The original string value to encode.
     * @return The URL-encoded string.
     */
    protected String specialUrlEncode(String value) {
        return URLEncoder.encode(value, Charset.UTF_8).replace(Symbol.PLUS, "%20").replace(Symbol.STAR, "%2A")
                .replace("%7E", Symbol.TILDE);
    }

    /**
     * Constructs the signature for an Alibaba Cloud API request. This involves sorting parameters, building a
     * canonicalized query string, and then signing it.
     *
     * @param params A map of parameters to be signed.
     * @return The generated signature string.
     * @throws InternalException if a security algorithm is not found or the key is invalid during signing.
     */
    protected String getSign(Map<String, String> params) {
        // 4. 参数KEY排序
        Map<String, String> map = new TreeMap<>(params);
        // 5. 构造待签名的字符串
        Iterator<String> it = map.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append(Symbol.AND).append(specialUrlEncode(key)).append(Symbol.EQUAL)
                    .append(specialUrlEncode(params.get(key)));
        }
        // 去除第一个多余的&符号
        String sortedQueryString = sortQueryStringTmp.substring(1);
        String stringToSign = HTTP.GET + Symbol.AND + specialUrlEncode(Symbol.SLASH) + Symbol.AND
                + specialUrlEncode(sortedQueryString);
        return sign(stringToSign);
    }

    /**
     * Signs a given string using HMAC-SHA1 algorithm with the application secret.
     *
     * @param stringToSign The string to be signed.
     * @return The Base64 encoded signature string.
     * @throws InternalException if a security algorithm is not found or the key is invalid.
     */
    protected String sign(String stringToSign) {
        try {
            Mac mac = Mac.getInstance(Algorithm.HMACSHA1.getValue());
            mac.init(
                    new SecretKeySpec((context.getAppSecret() + Symbol.AND).getBytes(Charset.UTF_8),
                            Algorithm.HMACSHA1.getValue()));
            byte[] signData = mac.doFinal(stringToSign.getBytes(Charset.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new InternalException("Aliyun specialUrlEncode error");
        }
    }

    /**
     * Checks the response from Alibaba Cloud and converts it into a {@link Message} object.
     *
     * @param response The raw JSON response string from Alibaba Cloud.
     * @return A {@link Message} indicating the success or failure of the operation.
     */
    protected Message checkResponse(String response) {
        String code = JsonKit.getValue(response, "Code");
        return Message.builder().errcode(SUCCESS_RESULT.equals(code) ? ErrorCode._SUCCESS.getKey() : code).errmsg(code)
                .build();
    }

}
