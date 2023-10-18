/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.notify.provider.upyun;

import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.Builder;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.AbstractProvider;

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
public class UpyunSmsProvider extends AbstractProvider<UpyunProperty, Context> {

    public UpyunSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(UpyunProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("template_id", entity.getTemplate());
        bodys.put("mobile", entity.getReceive());
        bodys.put("vars", StringKit.split(entity.getParams(), "|").toString());

        Map<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Header.AUTHORIZATION, entity.getToken());
        String response = Httpx.post(entity.getUrl(), bodys, headers);

        Collection<UpyunProperty.MessageId> list = JsonKit.toList(response, UpyunProperty.MessageId.class);
        if (CollKit.isEmpty(list)) {
            return Message.builder()
                    .errcode(Builder.ErrorCode.FAILURE.getCode())
                    .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                    .build();
        }
        boolean succeed = list.stream().filter(Objects::nonNull).anyMatch(UpyunProperty.MessageId::succeed);
        String errcode = succeed ? Builder.ErrorCode.SUCCESS.getCode() : Builder.ErrorCode.FAILURE.getCode();
        String errmsg = succeed ? Builder.ErrorCode.SUCCESS.getMsg() : Builder.ErrorCode.FAILURE.getMsg();

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
