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
package org.miaixz.bus.notify.metric.aliyun;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;

/**
 * Alibaba Cloud Voice Messaging Service (VMS) provider.
 *
 * @author Justubborn
 * @since Java 21+
 */
public class AliyunVmsProvider extends AliyunProvider<AliyunNotice, Context> {

    /**
     * Constructs an {@code AliyunVmsProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public AliyunVmsProvider(Context context) {
        super(context);
    }

    /**
     * Sends a voice notification using Alibaba Cloud VMS service.
     *
     * @param entity The {@link AliyunNotice} containing VMS details like recipient, sender, play times, template, and
     *               parameters.
     * @return A {@link Message} indicating the result of the VMS sending operation.
     */
    @Override
    public Message send(AliyunNotice entity) {
        Map<String, String> bodys = new HashMap<>();
        // 1. System parameters
        // The signature method used for authentication.
        bodys.put("SignatureMethod", "HMAC-SHA1");
        // A unique random number to prevent replay attacks.
        bodys.put("SignatureNonce", UUID.randomUUID().toString());
        // The AccessKey ID of your Alibaba Cloud account.
        bodys.put("AccessKeyId", context.getAppKey());
        // The version of the signature algorithm.
        bodys.put("SignatureVersion", "1.0");
        // The timestamp of the API request in UTC format.
        bodys.put("Timestamp", DateKit.format(new Date(), Fields.UTC));
        // The format of the response, typically JSON.
        bodys.put("Format", "JSON");

        // 2. Business API parameters
        // The API action to be performed, e.g., SingleCallByTts.
        bodys.put("Action", "SingleCallByTts");
        // The API version.
        bodys.put("Version", "2017-05-25");
        // The region ID of the service, e.g., cn-hangzhou.
        bodys.put("RegionId", "cn-hangzhou");
        // The mobile number to be called.
        bodys.put("CalledNumber", entity.getReceive());
        // The number displayed to the recipient.
        bodys.put("CalledShowNumber", entity.getSender());
        // The number of times the voice message should be played.
        bodys.put("PlayTimes", entity.getPlayTimes());
        // The parameters for the Text-to-Speech (TTS) template in JSON format.
        bodys.put("TtsParam", entity.getParams());
        // The TTS template code.
        bodys.put("TtsCode", entity.getTemplate());
        bodys.put("Signature", getSign(bodys));

        Map<String, String> map = new HashMap<>();
        for (String text : bodys.keySet()) {
            map.put(specialUrlEncode(text), specialUrlEncode(bodys.get(text)));
        }
        return checkResponse(Httpx.get(this.getUrl(entity), map));
    }

}
