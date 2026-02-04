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
package org.miaixz.bus.notify.metric.upyun;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.Notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Notice for Upyun SMS service.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpyunNotice extends Notice {

    /**
     * The authentication token for Upyun API.
     */
    private String token;

    /**
     * Retrieves the default API request address. This address is used when the {@link Context} endpoint is empty.
     *
     * @return The default API request address for Upyun SMS.
     */
    @Override
    public String getUrl() {
        return this.url = " https://sms-api.upyun.com/api/messages/";
    }

    /**
     * Represents the result of sending an SMS to a mobile number.
     */
    @Getter
    @Setter
    public static class MessageId {

        /**
         * The error code, if any, indicating a failure.
         */
        private String error_code;

        /**
         * The message ID for old versions of domestic SMS.
         */
        private Integer message_id;

        /**
         * The message ID.
         */
        private String msg_id;

        /**
         * The mobile number to which the SMS was sent.
         */
        private String mobile;

        /**
         * Checks if the SMS sending operation was successful.
         *
         * @return {@code true} if the operation was successful (no error code and mobile number is present),
         *         {@code false} otherwise.
         */
        public boolean succeed() {
            return StringKit.isBlank(error_code) && StringKit.isNotBlank(mobile);
        }

    }

}
