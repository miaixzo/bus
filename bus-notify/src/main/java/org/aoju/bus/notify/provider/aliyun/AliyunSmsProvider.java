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

import org.aoju.bus.core.lang.Http;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云短信
 *
 * @author Justubborn
 * @version 6.0.1
 * @since JDK1.8+
 */
public class AliyunSmsProvider extends AliyunProvider<AliyunSmsTemplate, Context> {

    /**
     * 阿里云短信产品域名
     */
    private static final String ALIYUN_PRODUCT_DOMAIN = "dysmsapi.aliyuncs.com";

    public AliyunSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(AliyunSmsTemplate template) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
        paras.put("Action", "SendSms");
        paras.put("Version", "2017-05-25");
        paras.put("RegionId", "cn-hangzhou");
        paras.put("PhoneNumbers", template.getReceive());
        paras.put("SignName", properties.getSignName());
        paras.put("TemplateParam", template.getTemplateParam());
        paras.put("TemplateCode", template.getTempCode());

        paras.put("Signature", getSign(paras));

        Map<String, Object> map = new HashMap<>();
        for (String str : paras.keySet()) {
            map.put(specialUrlEncode(str), specialUrlEncode(paras.get(str)));
        }
        return checkResponse(Httpx.get(Http.HTTPS_PREFIX + ALIYUN_PRODUCT_DOMAIN, map));
    }

}
