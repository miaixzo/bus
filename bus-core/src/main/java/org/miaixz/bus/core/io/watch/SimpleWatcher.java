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
