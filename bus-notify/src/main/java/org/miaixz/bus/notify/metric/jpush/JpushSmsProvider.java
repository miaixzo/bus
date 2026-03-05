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
package org.miaixz.bus.notify.metric.jpush;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * JPush SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JpushSmsProvider extends AbstractProvider<JpushNotice, Context> {

    /**
     * Constructs a {@code JpushSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public JpushSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using JPush SMS service.
     *
     * @param entity The {@link JpushNotice} containing SMS details like signature ID, recipient, template ID, and
     *               template parameters.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(JpushNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        /**
         * The signature ID for the SMS.
         */
        bodys.put("sign_id", entity.getSignature());
        /**
         * The recipient's mobile number.
         */
        bodys.put("mobile", entity.getReceive());
        /**
         * The template ID for the SMS message.
         */
        bodys.put("temp_id", entity.getTemplate());
        /**
         * The parameters for the SMS template.
         */
        bodys.put("temp_para", entity.getParams());

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HTTP.AUTHORIZATION, "Basic " + getSign());

        String response = Httpx.post(this.getUrl(entity), bodys, headers);
        boolean succeed = Objects.equals(JsonKit.getValue(response, "success_count"), 0);
        String errcode = succeed ? ErrorCode._SUCCESS.getKey() : ErrorCode._FAILURE.getKey();
        String errmsg = succeed ? ErrorCode._SUCCESS.getValue() : JsonKit.getValue(response, "error.message");

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

    /**
     * Generates the Base64 encoded authorization string for JPush API requests.
     *
     * @return The Base64 encoded string of "appKey:appSecret".
     */
    private String getSign() {
        String origin = context.getAppKey() + Symbol.COLON + context.getAppSecret();
        return Base64.getEncoder().encodeToString(origin.getBytes(Charset.UTF_8));
    }

}
