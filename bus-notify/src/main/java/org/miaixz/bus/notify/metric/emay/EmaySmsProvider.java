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
package org.miaixz.bus.notify.metric.emay;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Emay SMS Provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EmaySmsProvider extends AbstractProvider<EmayNotice, Context> {

    /**
     * Constructs an {@code EmaySmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public EmaySmsProvider(Context context) {
        super(context);
    }

    /**
     * Prepares the parameters map for the Emay SMS API request.
     *
     * @param appId     The application ID.
     * @param secretKey The secret key for signing.
     * @param phone     The recipient's phone number.
     * @param message   The content of the SMS message.
     * @return A map of parameters for the request.
     */
    private static Map<String, String> getParamsMap(String appId, String secretKey, String phone, String message) {
        Map<String, String> params = new HashMap<>();
        // Timestamp (required), format: yyyyMMddHHmmss
        String timestamp = DateKit.format(new Date(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sign = Builder.md5(appId + secretKey + timestamp);
        params.put("appId", appId);
        params.put("timestamp", timestamp);
        params.put("sign", sign);
        params.put("mobiles", phone);
        params.put("content", UrlEncoder.encodeAll(message, Charset.UTF_8));
        return params;
    }

    /**
     * Sends an SMS notification using the Emay SMS service.
     *
     * @param entity The {@link EmayNotice} containing the SMS details like recipient and content.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(EmayNotice entity) {
        Map<String, String> bodys = getParamsMap(
                context.getAppKey(),
                context.getAppSecret(),
                entity.getReceive(),
                entity.getContent());
        Map<String, String> headers = MapKit.newHashMap(1, true);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        String response = Httpx.post(this.getUrl(entity), bodys, headers);
        String errcode = JsonKit.getValue(response, Consts.ERRCODE);
        return Message.builder()
                .errcode(String.valueOf(HTTP.HTTP_OK).equals(errcode) ? ErrorCode._SUCCESS.getKey() : errcode)
                .errmsg(JsonKit.getValue(response, Consts.ERRMSG)).build();
    }

}
