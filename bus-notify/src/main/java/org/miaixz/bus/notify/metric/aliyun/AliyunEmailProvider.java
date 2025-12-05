/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.notify.metric.aliyun;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.metric.generic.GenericNotice;

/**
 * Alibaba Cloud Email service provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliyunEmailProvider extends AliyunProvider<AliyunNotice, Context> {

    /**
     * Constructs an {@code AliyunEmailProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public AliyunEmailProvider(Context context) {
        super(context);
    }

    /**
     * Sends an email notification using Alibaba Cloud Direct Mail service.
     *
     * @param entity The {@link AliyunNotice} containing email details like recipient, subject, content, and sender.
     * @return A {@link Message} indicating the result of the email sending operation.
     * @throws InternalException if the email content, recipient address, or subject is empty.
     */
    @Override
    public Message send(AliyunNotice entity) throws InternalException {
        if (StringKit.isEmpty(entity.getContent())) {
            throw new InternalException("Email content cannot be empty");
        } else if (StringKit.isEmpty(entity.getReceive())) {
            throw new InternalException("Email address cannot be empty");
        } else if (StringKit.isEmpty(entity.getSubject())) {
            throw new InternalException("Email subject cannot be empty");
        }

        Map<String, String> bodys = new HashMap<>();
        // 1. System parameters
        /**
         * The signature method used for authentication.
         */
        bodys.put("SignatureMethod", "HMAC-SHA1");
        /**
         * A unique random number to prevent replay attacks.
         */
        bodys.put("SignatureNonce", UUID.randomUUID().toString());
        /**
         * The AccessKey ID of your Alibaba Cloud account.
         */
        bodys.put("AccessKeyId", context.getAppKey());
        /**
         * The version of the signature algorithm.
         */
        bodys.put("SignatureVersion", "1.0");
        /**
         * The timestamp of the API request in UTC format.
         */
        bodys.put("Timestamp", DateKit.format(new Date(), Fields.UTC));
        /**
         * The format of the response, typically JSON.
         */
        bodys.put("Format", "JSON");
        // 2. Business API parameters
        /**
         * The API action to be performed, e.g., SingleSendMail.
         */
        bodys.put("Action", "SingleSendMail");
        /**
         * The API version.
         */
        bodys.put("Version", "2015-11-23");
        /**
         * The region ID of the service, e.g., cn-hangzhou.
         */
        bodys.put("RegionId", "cn-hangzhou");

        /**
         * The subject of the email.
         */
        bodys.put("Subject", entity.getSubject());
        /**
         * The sender's alias.
         */
        bodys.put("FromAlias", entity.getSender());
        /**
         * The recipient's email address.
         */
        bodys.put("ToAddress", entity.getReceive());

        if (GenericNotice.Type.HTML.equals(entity.getType())) {
            /**
             * The HTML body of the email.
             */
            bodys.put("HtmlBody", entity.getContent());
        } else if (GenericNotice.Type.TEXT.equals(entity.getType())) {
            /**
             * The plain text body of the email.
             */
            bodys.put("TextBody", entity.getContent());
        }

        /**
         * The reply-to email address.
         */
        bodys.put("ReplyAddress", entity.getSender());
        /**
         * The reply-to email address.
         */
        bodys.put("ReplyToAddress", entity.getSender());
        /**
         * The reply-to address alias.
         */
        bodys.put("ReplyAddressAlias", entity.getSender());

        /**
         * Click tracking setting.
         */
        bodys.put("ClickTrace", getSign(bodys));
        /**
         * The signature for the request.
         */
        bodys.put("Signature", getSign(bodys));

        Map<String, String> map = new HashMap<>();
        for (String val : bodys.keySet()) {
            map.put(specialUrlEncode(val), specialUrlEncode(bodys.get(val)));
        }
        return checkResponse(Httpx.get(this.getUrl(entity), map));
    }

}
