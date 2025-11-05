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
package org.miaixz.bus.notify.metric.ctyun;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * China Telecom Cloud (CTYUN) SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CtyunSmsProvider extends AbstractProvider<CtyunNotice, Context> {

    /**
     * Constructs a {@code CtyunSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public CtyunSmsProvider(Context context) {
        super(context);
    }

    /**
     * Generates the signed headers required for CTYUN API requests.
     *
     * @param body   The request body as a string.
     * @param key    The application key (AccessKeyId).
     * @param secret The application secret (AccessKeySecret).
     * @return A map of HTTP headers including authorization and date information.
     */
    private static Map<String, String> signHeader(String body, String key, String secret) {
        Map<String, String> map = new ConcurrentHashMap<>(4);
        // Construct timestamp
        Date now = new Date();
        String signatureDate = DateKit.format(now, Formatter.PURE_DATE_FORMATTER);
        String signatureTime = DateKit.format(now, "yyyyMMdd'T'HHmmss'Z'");
        // Construct request serial number
        String uuid = UUID.randomUUID().toString();

        String calculateContentHash = org.miaixz.bus.crypto.Builder.sha256(body);

        byte[] kTime = org.miaixz.bus.crypto.Builder.hmacSha256(secret.getBytes()).digest(signatureTime.getBytes());
        byte[] kAk = org.miaixz.bus.crypto.Builder.hmacSha256(key.getBytes()).digest(kTime);
        byte[] kDate = org.miaixz.bus.crypto.Builder.hmacSha256(signatureDate.getBytes()).digest(kAk);

        // SHA256 digest of the original message body
        String signatureStr = String.format("ctyun-eop-request-id:%s\neop-date:%s\n", uuid, signatureTime) + "\n\n"
                + calculateContentHash;
        // Construct signature
        String signature = Base64
                .encode(org.miaixz.bus.crypto.Builder.hmacSha256(signatureStr.getBytes(Charset.UTF_8)).digest(kDate));
        String signHeader = String.format("%s Headers=ctyun-eop-request-id;eop-date Signature=%s", key, signature);
        map.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        map.put("ctyun-eop-request-id", uuid);
        map.put("Eop-date", signatureTime);
        map.put("Eop-Authorization", signHeader);
        return map;
    }

    /**
     * Sends an SMS notification using China Telecom Cloud (CTYUN) SMS service.
     *
     * @param entity The {@link CtyunNotice} containing SMS details like recipient, signature, template, and
     *               parameters.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(CtyunNotice entity) {
        Map<String, String> bodys = new HashMap<>(5);
        // The API action to be performed.
        bodys.put("action", entity.getAction());
        // The recipient's mobile number.
        bodys.put("phoneNumber", entity.getReceive());
        // The SMS signature name.
        bodys.put("signName", entity.getSignature());
        // The parameters for the SMS template in JSON format.
        bodys.put("templateParam", entity.getParams());
        // The SMS template code.
        bodys.put("templateCode", entity.getTemplate());

        String response = Httpx.post(
                this.getUrl(entity),
                bodys,
                signHeader(JsonKit.toJsonString(bodys), this.context.getAppKey(), this.context.getAppSecret()));

        String errcode = JsonKit.getValue(response, Consts.ERRCODE);
        return Message.builder().errcode("200".equals(errcode) ? ErrorCode._SUCCESS.getKey() : errcode)
                .errmsg(JsonKit.getValue(response, Consts.ERRMSG)).build();
    }

}
