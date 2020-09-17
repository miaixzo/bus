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
package org.aoju.bus.notify.provider.aliyun;

import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.email.NativeDmProperty;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云邮件
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public class AliyunDmProvider extends AliyunProvider<AliyunDmProperty, Context> {

    /**
     * 阿里云邮件产品域名
     */
    private static final String ALIYUN_DM_DOMAIN = "dm.aliyuncs.com";

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
        Map<String, String> paras = new HashMap<>();
        // 1. 系统参数
        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureNonce", UUID.randomUUID().toString());
        paras.put("AccessKeyId", properties.getAppKey());
        paras.put("SignatureVersion", "1.0");
        paras.put("Timestamp", df.format(new Date()));
        paras.put("Format", "JSON");
        // 2. 业务API参数
        paras.put("Action", "SingleSendMail");
        paras.put("Version", "2015-11-23");
        paras.put("RegionId", "cn-hangzhou");

        paras.put("Subject", entity.getSubject());
        paras.put("FromAlias", entity.getSender());
        paras.put("ToAddress", entity.getReceive());

        if (NativeDmProperty.ContentType.HTML.equals(entity.getContentType())) {
            paras.put("HtmlBody", entity.getContent());
        } else if (NativeDmProperty.ContentType.TEXT.equals(entity.getContentType())) {
            paras.put("TextBody", entity.getContent());
        }

        paras.put("ReplyAddress", entity.getSender());
        paras.put("ReplyToAddress", entity.getSender());
        paras.put("ReplyAddressAlias", entity.getSender());

        paras.put("ClickTrace", getSign(paras));

        paras.put("Signature", getSign(paras));

        Map<String, Object> map = new HashMap<>();
        for (String str : paras.keySet()) {
            map.put(specialUrlEncode(str), specialUrlEncode(paras.get(str)));
        }
        return checkResponse(Httpx.get(Http.HTTPS_PREFIX + ALIYUN_DM_DOMAIN, map));
    }

}