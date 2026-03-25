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
 * @since Java 21+
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
