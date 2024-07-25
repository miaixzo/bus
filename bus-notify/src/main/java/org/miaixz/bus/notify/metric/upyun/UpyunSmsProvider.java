/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.notify.metric.upyun;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 又拍云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpyunSmsProvider extends AbstractProvider<UpyunMaterial, Context> {

    public UpyunSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(UpyunMaterial entity) {
        Map<String, String> bodys = new HashMap<>();
        bodys.put("template_id", entity.getTemplate());
        bodys.put("mobile", entity.getReceive());
        bodys.put("vars", StringKit.split(entity.getParams(), "|").toString());

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HTTP.AUTHORIZATION, entity.getToken());
        String response = Httpx.post(this.getUrl(entity), bodys, headers);

        Collection<UpyunMaterial.MessageId> list = JsonKit.toList(response, UpyunMaterial.MessageId.class);
        if (CollKit.isEmpty(list)) {
            return Message.builder()
                    .errcode(ErrorCode.FAILURE.getCode())
                    .errmsg(ErrorCode.FAILURE.getDesc())
                    .build();
        }
        boolean succeed = list.stream().filter(Objects::nonNull).anyMatch(UpyunMaterial.MessageId::succeed);
        String errcode = succeed ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = succeed ? ErrorCode.SUCCESS.getDesc() : ErrorCode.FAILURE.getDesc();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

    /**
     * 判断是否成功.
     * s
     *
     * @return 是否成功
     */
    public boolean succeed(String errorCode, String msgId) {
        return StringKit.isBlank(errorCode) && StringKit.isNotBlank(msgId);
    }

}
