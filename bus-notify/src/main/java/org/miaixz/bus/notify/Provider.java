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
package org.miaixz.bus.notify;

import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.notify.magic.Notice;

/**
 * Notifier interface for sending various types of notifications, such as SMS, email, voice, WeChat, etc.
 *
 * @param <T> the type of notice used for the notification, extending {@link Notice}
 * @author Justubborn
 * @since Java 17+
 */
public interface Provider<T extends Notice> extends org.miaixz.bus.core.Provider {

    /**
     * Sends a notification using the specified template {@link Notice}. Note: The template implementation varies among
     * different service providers.
     *
     * @param entity The notification content or notice.
     * @return The result of the sending operation.
     */
    Message send(T entity);

    /**
     * Sends a notification to a list of mobile numbers.
     *
     * @param entity The notification content or notice.
     * @param mobile A list of mobile numbers to send the notification to.
     * @return The result of the sending operation.
     */
    Message send(T entity, List<String> mobile);

    /**
     * Sends a notification to a single mobile number.
     *
     * @param entity The notification content or notice.
     * @param mobile The mobile number to send the notification to.
     * @return The result of the sending operation.
     */
    default Message send(T entity, String mobile) {
        return send(entity, Collections.singletonList(mobile));
    }

    /**
     * Sends a notification to multiple mobile numbers.
     *
     * @param entity The notification content or notice.
     * @param mobile An array of mobile numbers to send the notification to.
     * @return The result of the sending operation.
     */
    default Message send(T entity, String... mobile) {
        return send(entity, ListKit.of(mobile));
    }

    /**
     * Returns the type of this provider.
     *
     * @return The provider type, which is {@link EnumValue.Povider#NOTIFY}.
     */
    @Override
    default Object type() {
        return EnumValue.Povider.NOTIFY;
    }

}
