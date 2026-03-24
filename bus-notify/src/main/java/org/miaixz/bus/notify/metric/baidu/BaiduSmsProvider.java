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
package org.miaixz.bus.notify.metric.baidu;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Baidu Cloud SMS service provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BaiduSmsProvider extends AbstractProvider<BaiduNotice, Context> {

    /**
     * Constructs a {@code BaiduSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public BaiduSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Baidu Cloud SMS service.
     *
     * @param entity The {@link BaiduNotice} containing SMS details like recipient, template, signature, and parameters.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(BaiduNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        // The recipient's mobile number.
        bodys.put("mobile", entity.getReceive());
        // The SMS template ID.
        bodys.put("template", entity.getTemplate());
        // The signature ID for the SMS.
        bodys.put("signatureId", entity.getSignature());
        // The parameters for the SMS template in JSON format.
        bodys.put("contentVar", entity.getParams());
        String response = Httpx.post(this.getUrl(entity), bodys);
        String errcode = JsonKit.getValue(response, Consts.ERRCODE);
        return Message.builder().errcode("200".equals(errcode) ? ErrorCode._SUCCESS.getKey() : errcode)
                .errmsg(JsonKit.getValue(response, Consts.ERRMSG)).build();
    }

}
