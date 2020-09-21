/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.notify.provider.netease;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.magic.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * 云信消息
 *
 * @author wubenhui
 * @version 6.0.9
 * @since JDK 1.8+
 */
public class NeteaseImProvider extends NeteaseProvider<NeteaseImProperty, Context> {

    private static final String NETEASE_SENDMSG_API = "https://api.netease.im/nimserver/msg/sendMsg.action";

    private static final String NETEASE_SENDATTACHMSG_API = "https://api.netease.im/nimserver/msg/sendAttachMsg.action";

    public NeteaseImProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(NeteaseImProperty property) {
        if (Property.Type.FILE.equals(property.getType())) {
            return sendAttachMsg(property);
        }
        return sendMsg(property);
    }

    public Message sendMsg(NeteaseImProperty property) {
        //构造payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", property.getContent());
        Map<String, Object> aps = new HashMap<>();
        aps.put("mutable-content", "1");
        Map<String, Object> alert = new HashMap<>();
        alert.put("title", property.getTitle());
        alert.put("body", property.getBody());
        aps.put("alert", alert);
        payload.put("apsField", aps);

        //构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("from", property.getSender());
        params.put("to", property.getReceive());
        params.put("ope", "0");
        params.put("type", "100");
        params.put("body", property.getContent());
        params.put("payload", JSON.toJSONString(payload));

        Map<String, Object> option = new HashMap<>();
        option.put("needPushNick", "false");
        option.put("sendersync", "false");
        params.put("option", JSON.toJSONString(option));
        return post(NETEASE_SENDMSG_API, params);
    }

    public Message sendAttachMsg(NeteaseImProperty template) {
        Map<String, Object> params = new HashMap<>();
        params.put("from", template.getSender());
        params.put("msgtype", "0");
        params.put("to", template.getReceive());
        params.put("attach", template.getContent());
        return post(NETEASE_SENDATTACHMSG_API, params);
    }

}
