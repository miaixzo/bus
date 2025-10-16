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
package org.miaixz.bus.notify.metric.netease;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.notify.Context;

/**
 * Netease Cloud SMS message provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NeteaseSmsProvider extends NeteaseProvider<NeteaseMaterial, Context> {

    @Serial
    private static final long serialVersionUID = -202510031219L;

    /**
     * Constructs a {@code NeteaseSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public NeteaseSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Netease Cloud SMS service.
     *
     * @param entity The {@link NeteaseMaterial} containing SMS details such as template ID, parameters, and recipient.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(NeteaseMaterial entity) {
        Map<String, String> bodys = new HashMap<>();
        bodys.put("templateid", entity.getTemplate());
        bodys.put("mobiles", JsonKit.toJsonString(new String[] { entity.getReceive() }));
        bodys.put("params", JsonKit.toJsonString(entity.getParams()));
        return post(this.getUrl(entity), bodys);
    }

}
