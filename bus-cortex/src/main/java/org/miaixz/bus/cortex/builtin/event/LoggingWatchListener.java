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
package org.miaixz.bus.cortex.builtin.event;

import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Watch;
import org.miaixz.bus.logger.Logger;

/**
 * Built-in {@link Listener} that logs watch change events to the standard logger.
 * <p>
 * Suitable as a diagnostic listener or as a lightweight placeholder during development.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LoggingWatchListener implements Listener<Watch<Object>> {

    /**
     * Creates a logging watch listener.
     */
    public LoggingWatchListener() {
    }

    /**
     * Logs each value carried by a watch change event.
     *
     * @param event watch change event to log
     */
    @Override
    public void onEvent(Watch<Object> event) {
        for (Object item : event.getAdded()) {
            Logger.info("Watch added: {}", item);
        }
        for (Object item : event.getRemoved()) {
            Logger.info("Watch removed: {}", item);
        }
        for (Object item : event.getUpdated()) {
            Logger.info("Watch updated: {}", item);
        }
    }

}
