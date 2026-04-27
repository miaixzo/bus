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
package org.miaixz.bus.cortex.setting.item;

import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Watch;
import org.miaixz.bus.logger.Logger;

/**
 * Listener that logs setting watch events.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ItemWatcher implements Listener<Watch<String>> {

    /**
     * Creates an ItemWatcher.
     */
    public ItemWatcher() {

    }

    /**
     * Logs each value carried by a setting watch event.
     *
     * @param event setting watch event to log
     */
    @Override
    public void onEvent(Watch<String> event) {
        for (String item : event.getAdded()) {
            Logger.info("Setting added: {}", item);
        }
        for (String item : event.getRemoved()) {
            Logger.info("Setting removed: {}", item);
        }
        for (String item : event.getUpdated()) {
            Logger.info("Setting updated: {}", item);
        }
    }

}
