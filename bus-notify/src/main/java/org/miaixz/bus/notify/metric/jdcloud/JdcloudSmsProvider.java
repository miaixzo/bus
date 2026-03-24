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
package org.miaixz.bus.notify.metric.jdcloud;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * JD Cloud SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JdcloudSmsProvider extends AbstractProvider<JdcloudNotice, Context> {

    /**
     * Constructs a {@code JdcloudSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public JdcloudSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using JD Cloud SMS service.
     *
     * @param entity The {@link JdcloudNotice} containing SMS details like recipient, template ID, parameters, and
     *               signature.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(JdcloudNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        /**
         * The region ID where the SMS service is located.
         */
        bodys.put("regionId", this.getUrl(entity));
        /**
         * The ID of the SMS template to be used.
         */
        bodys.put("templateId", entity.getTemplate());
        /**
         * The parameters for the SMS template, typically in JSON format.
         */
        bodys.put("params", entity.getParams());
        /**
         * A comma-separated list of recipient phone numbers.
         */
        bodys.put("phoneList", entity.getReceive());
        /**
         * The ID of the SMS signature.
         */
        bodys.put("signId", entity.getSignature());

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(this.getUrl(entity), bodys, headers);
        int status = JsonKit.getValue(response, "statusCode");

        String errcode = status == HTTP.HTTP_OK ? ErrorCode._SUCCESS.getKey() : ErrorCode._FAILURE.getKey();
        String errmsg = status == HTTP.HTTP_OK ? ErrorCode._SUCCESS.getValue() : ErrorCode._FAILURE.getValue();

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

}
