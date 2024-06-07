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
package org.miaixz.bus.notify.metric.unisms;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.util.*;

/**
 * 合一短信实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UniSmsProvider extends AbstractProvider<UniProperty, Context> {


    public UniSmsProvider(Context context) {
        super(context);
    }


    @Override
    public Message send(UniProperty entity) {
        if ("".equals(entity.getTemplate()) && "".equals(entity.getTemplateName())) {
            throw new ValidateException("配置文件模板id和模板变量不能为空！");
        }

        Map<String, Object> data = MapKit.newHashMap(4, true);
        data.put("to", StringKit.split(entity.getReceive(), Symbol.COMMA));
        data.put("signature", entity.getSignature());
        data.put("templateId", entity.getTemplate());
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put(entity.getTemplateName(), entity.getContent());
        data.put("templateData", map);
        return request(entity, "sms.message.send", data);
    }

    /**
     * 向 uni-sms发送请求
     *
     * @param action 接口名称
     */
    public Message request(final UniProperty entity, final String action, final Map<String, Object> bodys) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "uni-java-sdk" + "/" + "0.0.4");
        headers.put("Content-Type", MediaType.APPLICATION_JSON);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url;
        if (entity.isSimple()) {
            url = this.getUrl(entity) + "?action=" + action + "&accessKeyId=" + this.context.getAppKey();
        } else {
            Map<String, Object> query = new HashMap<>();
            if (this.context.getAppSecret() != null) {
                query.put("algorithm", Algorithm.HMACSHA256);
                query.put("timestamp", System.currentTimeMillis());
                query.put("nonce", UUID.randomUUID().toString().replaceAll("-", ""));

                Map<String, Object> sortedMap = new TreeMap<>();
                sortedMap.putAll(query);
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Object> stringObjectEntry : sortedMap.entrySet()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append("=").append(((Map.Entry<?, ?>) stringObjectEntry).getValue());
                }
                query.put("signature", Builder.hmacSha256(this.context.getAppSecret().getBytes()).digest(sb.toString()));
            }
            url = this.getUrl(entity) + "?action=" + action + "&accessKeyId=" + this.context.getAppKey() + "&algorithm=" + query.get("algorithm") +
                    "&timestamp=" + query.get("timestamp") + "&nonce=" + query.get("nonce") + "&signature=" + query.get("signature");
        }

        String response = Httpx.post(url, bodys, headers);

        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = succeed ? ErrorCode.SUCCESS.getMsg() : ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

}
