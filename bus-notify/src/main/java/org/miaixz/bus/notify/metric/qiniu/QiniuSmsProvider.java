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
package org.miaixz.bus.notify.metric.qiniu;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Qiniu Cloud SMS service provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class QiniuSmsProvider extends AbstractProvider<QiniuNotice, Context> {

    @Serial
    private static final long serialVersionUID = -202510031218L;

    /**
     * Constructs a {@code QiniuSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public QiniuSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Qiniu Cloud SMS service.
     *
     * @param entity The {@link QiniuNotice} containing SMS details such as template ID, parameters, and recipient.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(QiniuNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        bodys.put("template_id", entity.getTemplate());
        bodys.put("parameters", entity.getParams());
        bodys.put("mobiles", entity.getReceive());
        String response = Httpx.post(this.getUrl(entity), bodys);
        Integer status = JsonKit.getValue(response, "status");

        String errcode = (status != null && status == 200) ? ErrorCode._SUCCESS.getKey() : ErrorCode._FAILURE.getKey();
        String errmsg = (status != null && status == 200) ? ErrorCode._SUCCESS.getValue()
                : ErrorCode._FAILURE.getValue();

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

}
