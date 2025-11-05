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
