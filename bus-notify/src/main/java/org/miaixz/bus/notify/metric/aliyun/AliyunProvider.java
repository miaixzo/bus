/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
import org.miaixz.bus.notify.magic.Notice;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Abstract provider for Alibaba Cloud notification services, handling common logic like signing requests.
 *
 * @param <T> The type of {@link Notice} this provider handles.
 * @param <K> The type of {@link Context} this provider uses.
 * @author Justubborn
 * @since Java 17+
 */
public class AliyunProvider<T extends Notice, K extends Context> extends AbstractProvider<T, K> {

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
        // 4. Sort parameter keys
        Map<String, String> map = new TreeMap<>(params);
        // 5. Construct string to be signed
        Iterator<String> it = map.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append(Symbol.AND).append(specialUrlEncode(key)).append(Symbol.EQUAL)
                    .append(specialUrlEncode(params.get(key)));
        }
        // Remove the first excess & symbol
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
