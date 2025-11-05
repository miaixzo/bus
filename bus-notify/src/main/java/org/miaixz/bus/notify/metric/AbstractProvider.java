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
