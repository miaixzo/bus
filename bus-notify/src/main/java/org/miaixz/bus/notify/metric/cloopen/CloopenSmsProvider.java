/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.notify.metric.cloopen;

import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.util.Map;

/**
 * 容联云短信实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CloopenSmsProvider extends AbstractProvider<CloopenProperty, Context> {

    public CloopenSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(CloopenProperty entity) {
        Map<String, Object> bodys = MapKit.newHashMap(4, true);
        bodys.put("to", String.join(",", entity.getReceive()));
        bodys.put("appId", this.context.getAppKey());
        bodys.put("templateId", entity.getTemplate());
        bodys.put("datas", entity.getContent());

        String response = Httpx.post(this.getUrl(entity), bodys);
        String errcode = JsonKit.getValue(response, "errcode");
        return Message.builder()
                .errcode("200".equals(errcode) ? ErrorCode.SUCCESS.getCode() : errcode)
                .errmsg(JsonKit.getValue(response, "errmsg"))
                .build();
    }

}
