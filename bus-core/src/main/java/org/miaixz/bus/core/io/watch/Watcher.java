/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.watch;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

/**
 * Observer interface for file system events. Implementations of this interface can react to file creation,
 * modification, deletion, and overflow events.
 *
 * @author Kimi Liu
 * @since Java 17+
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
