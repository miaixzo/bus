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
package org.miaixz.bus.core.io.watch;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

/**
 * A no-op implementation of the {@link Watcher} interface. Users can extend this class and override only the methods
 * they need to implement specific file system event handling logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleWatcher implements Watcher, Serializable {

    @Serial
    private static final long serialVersionUID = 2852236259796L;

    /**
     * Called when a file or directory is created. This default implementation does nothing.
     *
     * @param event The {@link WatchEvent} that occurred.
     * @param key   The {@link WatchKey} on which the event occurred.
     */
    @Override
    public void onCreate(final WatchEvent<?> event, final WatchKey key) {
        // No-op
    }

    /**
     * Called when a file or directory is modified. This default implementation does nothing.
     *
     * @param event The {@link WatchEvent} that occurred.
     * @param key   The {@link WatchKey} on which the event occurred.
     */
    @Override
    public void onModify(final WatchEvent<?> event, final WatchKey key) {
        // No-op
    }

    /**
     * Called when a file or directory is deleted. This default implementation does nothing.
     *
     * @param event The {@link WatchEvent} that occurred.
     * @param key   The {@link WatchKey} on which the event occurred.
     */
    @Override
    public void onDelete(final WatchEvent<?> event, final WatchKey key) {
        // No-op
    }

    /**
     * Called when events are lost or an error occurs. This default implementation does nothing.
     *
     * @param event The {@link WatchEvent} that occurred.
     * @param key   The {@link WatchKey} on which the event occurred.
     */
    @Override
    public void onOverflow(final WatchEvent<?> event, final WatchKey key) {
        // No-op
    }

}
