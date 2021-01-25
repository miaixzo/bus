/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.notify.provider.aliyun;

import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.generic.NativeDmProperty;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云邮件
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public class AliyunDmProvider extends AliyunProvider<AliyunDmProperty, Context> {

    /**
     * 阿里云邮件产品域名
     */
    private static final String ALIYUN_DM_API = "dm.aliyuncs.com";

    public AliyunDmProvider(Context properties) {
        super(properties);
    }

    /**
     * 发送邮件逻辑处理
     *
     * @param entity 请求对象
     * @return 处理结果响应
     * @throws InstrumentException 异常信息
     */
    @Override
    public Message send(AliyunDmProperty entity) throws InstrumentException {
        if (StringKit.isEmpty(entity.getContent())) {
            throw new InstrumentException("Email content cannot be empty");
        } else if (StringKit.isEmpty(entity.getReceive())) {
            throw new InstrumentException("Email address cannot be empty");
        } else if (StringKit.isEmpty(entity.getSubject())) {
            throw new InstrumentException("Email subject cannot be empty");
        }

        SimpleDateFormat df = new SimpleDateFormat(Fields.UTC_PATTERN);
        // 这里一定要设置GMT时区
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        Map<String, String> params = new HashMap<>();
        // 1. 系统参数
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("AccessKeyId", properties.getAppKey());
        params.put("SignatureVersion", "1.0");
        params.put("Timestamp", df.format(new Date()));
        params.put("Format", "JSON");
        // 2. 业务API参数
        params.put("Action", "SingleSendMail");
        params.put("Version", "2015-11-23");
        params.put("RegionId", "cn-hangzhou");

        params.put("Subject", entity.getSubject());
        params.put("FromAlias", entity.getSender());
        params.put("ToAddress", entity.getReceive());

        if (NativeDmProperty.Type.HTML.equals(entity.getType())) {
            params.put("HtmlBody", entity.getContent());
        } else if (NativeDmProperty.Type.TEXT.equals(entity.getType())) {
            params.put("TextBody", entity.getContent());
        }

        params.put("ReplyAddress", entity.getSender());
        params.put("ReplyToAddress", entity.getSender());
        params.put("ReplyAddressAlias", entity.getSender());

        params.put("ClickTrace", getSign(params));

        params.put("Signature", getSign(params));

        Map<String, Object> map = new HashMap<>();
        for (String val : params.keySet()) {
            map.put(specialUrlEncode(val), specialUrlEncode(params.get(val)));
        }
        return checkResponse(Httpx.get(Http.HTTPS_PREFIX + ALIYUN_DM_API, map));
    }

}