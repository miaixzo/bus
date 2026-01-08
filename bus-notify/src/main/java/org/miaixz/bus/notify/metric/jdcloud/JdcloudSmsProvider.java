/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
