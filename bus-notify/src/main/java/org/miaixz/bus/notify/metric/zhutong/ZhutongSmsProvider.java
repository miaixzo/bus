/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.notify.metric.zhutong;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * Zhutong SMS service provider implementation. Platform official website: https://www.ztinfo.cn/products/sms
 * Documentation address: https://doc.zthysms.com/web/#/1/236 Management backend address (requires verification code):
 * http://mix2.zthysms.com/login The interfaces used here are: custom SMS sending (sendSms) and template SMS sending
 * (sendSmsTp).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZhutongSmsProvider extends AbstractProvider<ZhutongNotice, Context> {

    /**
     * Constructor for building the SMS implementation module.
     *
     * @param context The context containing configuration information for the provider.
     */
    public ZhutongSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Zhutong SMS service. It determines whether to send a custom SMS or a template SMS
     * based on the provided notice.
     *
     * @param entity The {@link ZhutongNotice} containing SMS details.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     */
    @Override
    public Message send(ZhutongNotice entity) {
        // If template ID or template variable name is empty, use custom SMS sending without a template
        if (ArrayKit.hasBlank(entity.getSignature(), entity.getTemplate(), entity.getTemplateName())) {
            return sendForCustom(entity);
        }

        return sendForTemplate(entity);
    }

    /**
     * Sends a custom SMS message. Documentation: https://doc.zthysms.com/web/#/1/14
     *
     * @param entity The {@link ZhutongNotice} containing custom SMS details.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     * @throws ValidateException if requestUrl, username, password, mobile, or content is invalid.
     */
    protected Message sendForCustom(ZhutongNotice entity) {
        String requestUrl = this.getUrl(entity);
        String username = this.context.getAppKey();
        String password = this.context.getAppSecret();

        validator(requestUrl, username, password);
        if (StringKit.isEmpty(entity.getReceive())) {
            throw new ValidateException("Zhutong SMS: Mobile number cannot be empty!");
        }
        if (entity.getReceive().length() >= 20000) {
            throw new ValidateException("Zhutong SMS: Up to 2000 mobile numbers are supported!");
        }
        if (StringKit.isBlank(entity.getContent())) {
            throw new ValidateException("Zhutong SMS: Content cannot be empty!");
        }
        if (entity.getContent().length() >= 1000) {
            throw new ValidateException("Zhutong SMS: Content cannot exceed 1000 characters!");
        }
        if (!entity.getContent().contains("【")) {
            throw new ValidateException(
                    "Zhutong SMS: Custom SMS content must include signature information, e.g., [Zhutong Technology] Your verification code is 8888!");
        }

        String url = this.getUrl(entity) + "v2/sendSms";
        long tKey = System.currentTimeMillis() / 1000;
        Map<String, String> bodys = new HashMap<>(5);
        // Account
        bodys.put("username", username);
        // Password
        bodys.put("password", Builder.md5(Builder.md5(password) + tKey));
        // tKey
        bodys.put("tKey", tKey + "");
        // Mobile number
        bodys.put("mobile", entity.getReceive());
        // Content
        bodys.put("content", entity.getContent());

        Map<String, String> headers = MapKit.newHashMap(1, true);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(url, bodys, headers);

        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode._SUCCESS.getKey() : JsonKit.getValue(response, "code");
        String errmsg = succeed ? ErrorCode._SUCCESS.getValue() : JsonKit.getValue(response, "message");

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

    /**
     * Sends a template SMS message. Documentation: https://doc.zthysms.com/web/#/1/13
     *
     * @param entity The {@link ZhutongNotice} containing template SMS details.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     * @throws InternalException if the signature or template ID is empty.
     */
    protected Message sendForTemplate(ZhutongNotice entity) {
        validator(this.getUrl(entity), this.context.getAppKey(), this.context.getAppSecret());
        if (StringKit.isBlank(entity.getSignature())) {
            throw new InternalException("Zhutong SMS: The reported signature in template SMS cannot be empty!");
        }

        if (StringKit.isBlank(entity.getTemplate())) {
            throw new InternalException("Zhutong SMS: Template ID for template SMS cannot be empty!");
        }

        // Address
        String url = this.getUrl(entity) + "v2/sendSmsTp";
        // Request parameters
        Map<String, String> bodys = new HashMap<>();
        // Account
        bodys.put("username", this.context.getAppKey());
        // tKey
        long tKey = System.currentTimeMillis() / 1000;
        bodys.put("tKey", String.valueOf(tKey));
        // Plaintext password
        bodys.put("password", Builder.md5(Builder.md5(this.context.getAppSecret()) + tKey));
        // Template ID
        bodys.put("tpId", entity.getTemplate());
        // Signature
        bodys.put("signature", entity.getSignature());
        // Extension number
        bodys.put("ext", "");
        // Custom parameters
        bodys.put("extend", "");
        // Send record collection
        Map<String, String> records = new HashMap<>();

        for (String mobile : StringKit.split(entity.getReceive(), Symbol.COMMA)) {
            Map<String, String> record = new HashMap<>();
            // Mobile number
            record.put("mobile", mobile);
            record.put("tpContent", entity.getContent());
            records.putAll(record);
        }

        bodys.put("records", JsonKit.toJsonString(records));

        Map<String, String> headers = MapKit.newHashMap(1, true);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        String response = Httpx.post(url, bodys, headers);

        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode._SUCCESS.getKey() : JsonKit.getValue(response, "code");
        String errmsg = succeed ? ErrorCode._SUCCESS.getValue() : JsonKit.getValue(response, "message");

        return Message.builder().errcode(errcode).errmsg(errmsg).build();
    }

    /**
     * Validates the request URL, username, and password.
     *
     * @param requestUrl The request URL.
     * @param username   The username.
     * @param password   The password.
     * @throws ValidateException if any of the validation checks fail.
     */
    private void validator(String requestUrl, String username, String password) {
        if (StringKit.isBlank(requestUrl)) {
            throw new ValidateException("Zhutong SMS: requestUrl cannot be empty!");
        }
        if (!requestUrl.endsWith("/")) {
            throw new ValidateException("Zhutong SMS: requestUrl must end with '/'!");
        }
        if (ArrayKit.hasBlank(username, password)) {
            throw new ValidateException("Zhutong SMS: Account username and password cannot be empty!");
        }
    }

}
