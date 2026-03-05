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
package org.miaixz.bus.notify.metric;

import java.util.List;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.Provider;
import org.miaixz.bus.notify.magic.Notice;

import lombok.AllArgsConstructor;

/**
 * Abstract base class for notification providers, offering common functionalities and properties.
 *
 * @param <T> The type of {@link Notice} this provider handles.
 * @param <K> The type of {@link Context} this provider uses.
 * @author Justubborn
 * @since Java 17+
 */
@AllArgsConstructor
public abstract class AbstractProvider<T extends Notice, K extends Context> implements Provider<T> {

    /**
     * The context containing configuration information for the provider.
     */
    protected K context;

    /**
     * Sends a notification with the given notice. Implementations should override this method to provide specific
     * sending logic.
     *
     * @param entity The notification content or notice.
     * @return The result of the sending operation, or {@code null} if not implemented.
     */
    @Override
    public Message send(T entity) {
        return null;
    }

    /**
     * Sends a notification with the given notice to a list of mobile numbers. Implementations should override this
     * method to provide specific sending logic.
     *
     * @param entity The notification content or notice.
     * @param mobile A list of mobile numbers to send the notification to.
     * @return The result of the sending operation, or {@code null} if not implemented.
     */
    @Override
    public Message send(T entity, List<String> mobile) {
        return null;
    }

    /**
     * Retrieves the URL for the notification, prioritizing the context's endpoint if available.
     *
     * @param property The notice containing a potential URL.
     * @return The URL for the notification.
     */
    protected String getUrl(T property) {
        return getUrl(this.context, property);
    }

    /**
     * Retrieves the URL for the notification, prioritizing the context's endpoint if available.
     *
     * @param context The context containing the endpoint.
     * @param entity  The notice containing a potential URL.
     * @return The URL for the notification.
     */
    protected String getUrl(K context, T entity) {
        return ObjectKit.defaultIfNull(context.getEndpoint(), entity.getUrl());
    }

}
