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
package org.miaixz.bus.core.io.watch;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

/**
 * Observer interface for file system events. Implementations of this interface can react to file creation,
 * modification, deletion, and overflow events.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Watcher {

    /**
     * Called when a file or directory is created.
     *
     * @param event The {@link WatchEvent} that occurred. The created file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    void onCreate(WatchEvent<?> event, WatchKey key);

    /**
     * Called when a file or directory is modified. Note that file modification events may be triggered multiple times
     * for a single change.
     *
     * @param event The {@link WatchEvent} that occurred. The modified file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    void onModify(WatchEvent<?> event, WatchKey key);

    /**
     * Called when a file or directory is deleted.
     *
     * @param event The {@link WatchEvent} that occurred. The deleted file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    void onDelete(WatchEvent<?> event, WatchKey key);

    /**
     * Called when events are lost or an error occurs (e.g., the event queue overflows).
     *
     * @param event The {@link WatchEvent} that occurred. The context of the overflow event can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    void onOverflow(WatchEvent<?> event, WatchKey key);

}
