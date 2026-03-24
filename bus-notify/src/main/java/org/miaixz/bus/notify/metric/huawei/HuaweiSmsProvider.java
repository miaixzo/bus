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
package org.miaixz.bus.notify.metric.huawei;

import java.security.MessageDigest;
import java.util.*;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Huawei Cloud SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HuaweiSmsProvider extends AbstractProvider<HuaweiNotice, Context> {

    /**
     * Success code for Huawei Cloud SMS API responses.
     */
    public static final String SUCCESS_CODE = "000000";
    /**
     * Used to format the authentication header field, assigning a value to the "X-WSSE" parameter. No modification
     * required.
     */
    private static final String WSSE_HEADER_FORMAT = "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\",Created=\"%s\"";

    /**
     * Used to format the authentication header field, assigning a value to the "Authorization" parameter. No
     * modification required.
     */
    private static final String AUTH_HEADER_VALUE = "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"";

    /**
     * Constructs a {@code HuaweiSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public HuaweiSmsProvider(Context context) {
        super(context);
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal string representation.
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    /**
     * Sends an SMS notification using Huawei Cloud SMS service.
     *
     * @param entity The {@link HuaweiNotice} containing SMS details like sender, recipient, template ID, template
     *               parameters, and signature.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(HuaweiNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        // The sender's number.
        bodys.put("from", entity.getSender());
        // The recipient's mobile number.
        bodys.put("to", entity.getReceive());
        // The SMS template ID.
        bodys.put("templateId", entity.getTemplate());
        // The parameters for the SMS template in JSON format.
        bodys.put("templateParas", entity.getParams());
        // The SMS signature.
        bodys.put("signature", entity.getSignature());

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HTTP.AUTHORIZATION, AUTH_HEADER_VALUE);
        headers.put("X-WSSE", buildWsseHeader());

        String response = Httpx.post(this.getUrl(entity), bodys, headers);
        String errcode = JsonKit.getValue(response, "code");
        return Message.builder().errcode(SUCCESS_CODE.equals(errcode) ? ErrorCode._SUCCESS.getKey() : errcode)
                .errmsg(JsonKit.getValue(response, "description")).build();
    }

    /**
     * Constructs the X-WSSE header value for authentication.
     *
     * @return The X-WSSE header value.
     * @throws InternalException if a security algorithm is not found or other exceptions occur during header
     *                           construction.
     */
    private String buildWsseHeader() {
        try {
            String time = DateKit.format(new Date(), Fields.UTC);
            String nonce = UUID.randomUUID().toString().replace(Symbol.MINUS, "");
            String text = nonce + time + context.getAppSecret();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes(Charset.UTF_8));
            String hexDigest = byte2Hex(digest.digest());
            String passwordDigestBase64Str = Base64.getEncoder().encodeToString(hexDigest.getBytes());
            return String.format(WSSE_HEADER_FORMAT, context.getAppKey(), passwordDigestBase64Str, nonce, time);
        } catch (Exception e) {
            throw new InternalException(e.getLocalizedMessage(), e);
        }
    }

}
