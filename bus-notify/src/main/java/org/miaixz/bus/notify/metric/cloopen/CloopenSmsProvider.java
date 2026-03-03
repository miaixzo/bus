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
package org.miaixz.bus.notify.metric.cloopen;

import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Cloopen Cloud SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CloopenSmsProvider extends AbstractProvider<CloopenNotice, Context> {

    /**
     * Constructs a {@code CloopenSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public CloopenSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Cloopen Cloud SMS service.
     *
     * @param entity The {@link CloopenNotice} containing SMS details like recipient, template ID, and content.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(CloopenNotice entity) {
        Map<String, String> bodys = MapKit.newHashMap(4, true);
        // The recipient's mobile number(s), comma-separated.

        bodys.put("to", String.join(Symbol.COMMA, entity.getReceive()));
        // The application ID provided by Cloopen Cloud.
        bodys.put("appId", this.context.getAppKey());
        // The template ID for the SMS message.
        bodys.put("templateId", entity.getTemplate());
        // The content of the SMS message, typically parameters for the template.
        bodys.put("datas", entity.getContent());

        String response = Httpx.post(this.getUrl(entity), bodys);
        String errcode = JsonKit.getValue(response, Consts.ERRCODE);
        return Message.builder().errcode("200".equals(errcode) ? ErrorCode._SUCCESS.getKey() : errcode)
                .errmsg(JsonKit.getValue(response, Consts.ERRMSG)).build();
    }

}
