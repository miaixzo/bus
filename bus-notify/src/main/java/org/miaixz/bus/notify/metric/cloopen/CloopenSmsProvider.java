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
public class CloopenSmsProvider extends AbstractProvider<CloopenMaterial, Context> {

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
     * @param entity The {@link CloopenMaterial} containing SMS details like recipient, template ID, and content.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(CloopenMaterial entity) {
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
