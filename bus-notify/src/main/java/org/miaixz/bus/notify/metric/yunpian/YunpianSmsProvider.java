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
package org.miaixz.bus.notify.metric.yunpian;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Yunpian SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class YunpianSmsProvider extends AbstractProvider<YunpianNotice, Context> {

    /**
     * Constructs a {@code YunpianSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public YunpianSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Yunpian SMS service.
     *
     * @param entity The {@link YunpianNotice} containing SMS details like API key, recipient, template ID, and template
     *               parameters.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(YunpianNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        // The API key for Yunpian.
        bodys.put("apikey", entity.getApikey());
        // The recipient's mobile number.
        bodys.put("mobile", entity.getReceive());
        // The template ID for the SMS message.
        bodys.put("tpl_id", entity.getTemplate());
        // The parameters for the SMS template.
        bodys.put("tpl_value", entity.getParams());

        String response = Httpx.post(this.getUrl(entity), bodys);
        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode._SUCCESS.getKey() : JsonKit.getValue(response, "code");
        String errmsg = succeed ? ErrorCode._SUCCESS.getValue() : JsonKit.getValue(response, "msg");

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

}
