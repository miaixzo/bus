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

import java.util.List;

import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.logger.Logger;

/**
 * Built-in {@link Listener} that logs all change events to the standard logger.
 * <p>
 * Suitable as a diagnostic listener or as a no-op placeholder during development.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LoggingWatchListener implements Listener<String> {

    /**
     * Logs each added, removed and updated item.
     *
     * @param added   newly added values
     * @param removed removed values
     * @param updated updated values
     */
    @Override
    public void accept(List<String> added, List<String> removed, List<String> updated) {
        for (String item : added) {
            Logger.info("Watch added: {}", item);
        }
        for (String item : removed) {
            Logger.info("Watch removed: {}", item);
        }
        for (String item : updated) {
            Logger.info("Watch updated: {}", item);
        }
    }

}
