/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.notify.nimble.unisms;

import static org.miaixz.bus.notify.FabricX.post;

import java.util.*;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.nimble.AbstractProvider;

/**
 * Uni SMS service provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UniSmsProvider extends AbstractProvider<UniNotice, Context> {

    /**
     * Constructs a {@code UniSmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public UniSmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends an SMS notification using Uni SMS service.
     *
     * @param entity The {@link UniNotice} containing SMS details like template ID, template name, recipient, signature,
     *               and parameters.
     * @return A {@link Message} indicating the result of the SMS sending operation.
     * @throws ValidateException if both template ID and template name are empty.
     */
    @Override
    public Message send(UniNotice entity) {
        if ("".equals(entity.getTemplate()) && "".equals(entity.getTemplateName())) {
            Logger.warn(
                    false,
                    "Notify",
                    "UniSMS send rejected: reason=missingTemplate, targetCount={}",
                    entity == null || entity.getReceive() == null ? 0 : entity.getReceive().split(",").length);
            throw new ValidateException("Template ID and template variable in the configuration file cannot be empty!");
        }
        Logger.info(
                true,
                "Notify",
                "UniSMS send started: template={}, templateNamePresent={}, targetCount={}",
                entity == null ? null : entity.getTemplate(),
                entity != null && entity.getTemplateName() != null,
                entity == null || entity.getReceive() == null ? 0 : entity.getReceive().split(",").length);

        Map<String, Object> data = MapKit.newHashMap(4, true);
        // The recipient's mobile number.
        data.put("to", entity.getReceive());
        // The SMS signature.
        data.put("signature", entity.getSignature());
        // The template ID for the SMS message.
        data.put("templateId", entity.getTemplate());
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        // The template name for the SMS message.
        map.put(entity.getTemplateName(), entity.getContent());
        // The template data for the SMS message.
        data.put("templateData", map);
        Message result = request(entity, "sms.message.send", data);
        Logger.info(
                false,
                "Notify",
                "UniSMS send completed: template={}, targetCount={}, errcode={}",
                entity == null ? null : entity.getTemplate(),
                entity == null || entity.getReceive() == null ? 0 : entity.getReceive().split(",").length,
                result == null ? null : result.getErrcode());
        return result;
    }

    /**
     * Sends a request to the Uni SMS API.
     *
     * @param entity The {@link UniNotice} containing request details.
     * @param action The API action to be performed, e.g., "sms.message.send".
     * @param bodys  The request body parameters.
     * @return A {@link Message} indicating the result of the API request.
     */
    public Message request(final UniNotice entity, final String action, final Map<String, Object> bodys) {
        Logger.debug(
                true,
                "Notify",
                "UniSMS request started: action={}, bodyKeyCount={}, simpleMode={}",
                action,
                bodys == null ? 0 : bodys.size(),
                entity != null && entity.isSimple());
        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP.USER_AGENT, "uni-java-sdk" + "/" + "0.0.4");
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HTTP.ACCEPT, MediaType.APPLICATION_JSON);
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
                    sb.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append("=")
                            .append(((Map.Entry<?, ?>) stringObjectEntry).getValue());
                }
                query.put(
                        "signature",
                        Builder.hmacSha256(this.context.getAppSecret().getBytes()).digest(sb.toString()));
            }
            url = this.getUrl(entity) + "?action=" + action + "&accessKeyId=" + this.context.getAppKey() + "&algorithm="
                    + query.get("algorithm") + "&timestamp=" + query.get("timestamp") + "&nonce=" + query.get("nonce")
                    + "&signature=" + query.get("signature");
        }

        String response = post(url, JsonKit.toJsonString(bodys), headers, MediaType.APPLICATION_JSON);

        boolean succeed = Objects.equals(JsonKit.getValue(response, "code"), 0);
        String errcode = succeed ? ErrorCode._SUCCESS.getKey() : ErrorCode._FAILURE.getKey();
        String errmsg = succeed ? ErrorCode._SUCCESS.getValue() : JsonKit.getValue(response, "message");

        Message result = Message.builder().errcode(errcode).errmsg(errmsg).build();
        Logger.debug(
                false,
                "Notify",
                "UniSMS request completed: action={}, errcode={}, responseBytes={}",
                action,
                result.getErrcode(),
                response == null ? 0 : response.length());
        return result;
    }

}
