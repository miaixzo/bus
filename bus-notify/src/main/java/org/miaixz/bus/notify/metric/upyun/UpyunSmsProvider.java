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
package org.miaixz.bus.notify.metric.upyun;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Upyun SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UpyunSmsProvider extends AbstractProvider<UpyunNotice, Context> {

    /**
     * Constructs a {@code UpyunSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public UpyunSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Upyun SMS service.
     *
     * @param entity The {@link UpyunNotice} containing SMS details like template ID, recipient, and parameters.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(UpyunNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        // The template ID for the SMS message.
        bodys.put("template_id", entity.getTemplate());
        // The recipient's mobile number.
        bodys.put("mobile", entity.getReceive());
        // The parameters for the SMS template, formatted as a string representation of a list.
        bodys.put("vars", StringKit.split(entity.getParams(), "|").toString());

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HTTP.AUTHORIZATION, entity.getToken());
        String response = Httpx.post(this.getUrl(entity), bodys, headers);

        Collection<UpyunNotice.MessageId> list = JsonKit.toList(response, UpyunNotice.MessageId.class);
        if (CollKit.isEmpty(list)) {
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
        boolean succeed = list.stream().filter(Objects::nonNull).anyMatch(UpyunNotice.MessageId::succeed);
        String errcode = succeed ? ErrorCode._SUCCESS.getKey() : ErrorCode._FAILURE.getKey();
        String errmsg = succeed ? ErrorCode._SUCCESS.getValue() : ErrorCode._FAILURE.getValue();

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

    /**
     * Checks if an SMS sending operation was successful based on the error code and message ID.
     *
     * @param errorCode The error code returned by the service.
     * @param msgId     The message ID returned by the service.
     * @return {@code true} if the operation was successful (error code is blank and message ID is not blank),
     *         {@code false} otherwise.
     */
    public boolean succeed(String errorCode, String msgId) {
        return StringKit.isBlank(errorCode) && StringKit.isNotBlank(msgId);
    }

}
