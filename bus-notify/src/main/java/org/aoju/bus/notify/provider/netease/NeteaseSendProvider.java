/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 云信消息
 *
 * @author wubenhui
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class NeteaseSendProvider extends NeteaseProvider {

    private static final String API = "https://api.netease.im/nimserver/msg/sendMsg.action";

    public NeteaseSendProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(NeteaseTemplate template) {
        //构造payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", template.getContent());
        Map<String, Object> aps = new HashMap<>();
        aps.put("mutable-content", "1");
        Map<String, Object> alert = new HashMap<>();
        alert.put("title", template.getTitle());
        alert.put("body", template.getBody());
        aps.put("alert", alert);
        payload.put("apsField", aps);

        //构造请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("from", template.getSender());
        param.put("to", template.getReceive());
        param.put("ope", "0");
        param.put("type", "100");
        param.put("body", template.getContent());
        param.put("payload", JSONObject.toJSONString(payload));

        Map<String, Object> option = new HashMap<>();
        option.put("needPushNick", "false");
        option.put("sendersync", "false");
        param.put("option", JSON.toJSONString(option));
        return post(API, param);
    }

}
