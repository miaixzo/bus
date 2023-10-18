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
package org.aoju.bus.notify.provider.tencent;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.Builder;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TencentSmsProvider extends AbstractProvider<TencentProperty, Context> {

    public TencentSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(TencentProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("SmsSdkAppid", entity.getSmsAppId());
        bodys.put("Sign", entity.getSignature());
        bodys.put("TemplateID", entity.getTemplate());
        bodys.put("TemplateParamSet", StringKit.splitToArray(entity.getParams(), Symbol.COMMA));
        bodys.put("PhoneNumberSet", StringKit.splitToArray(entity.getReceive(), Symbol.COMMA));

        String response = Httpx.post(entity.getUrl(), bodys);
        int status = JsonKit.getValue(response, "status");

        String errcode = status == 200 ? Builder.ErrorCode.SUCCESS.getCode() : Builder.ErrorCode.FAILURE.getCode();
        String errmsg = status == 200 ? Builder.ErrorCode.SUCCESS.getMsg() : Builder.ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

}
