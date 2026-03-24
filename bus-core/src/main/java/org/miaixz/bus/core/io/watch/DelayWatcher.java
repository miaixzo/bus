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

import java.nio.file.*;
import java.util.Set;

import org.miaixz.bus.core.center.set.ConcurrentHashSet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * A {@link Watcher} implementation that introduces a delay to debounce multiple modify events for the same file within
 * a short period. This addresses a common issue with {@link WatchService} where a single file modification operation
 * can trigger multiple {@code ENTRY_MODIFY} events.
 * <p>
 * This class maintains a set of file paths that have recently triggered a modify event. Subsequent modify events for
 * the same file within the defined delay period are ignored, and only one event is processed after the delay. Note: The
 * delay mechanism only applies to {@code ENTRY_MODIFY} events. Other event types (create, delete, overflow) are passed
 * directly to the wrapped {@link Watcher} without delay.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DelayWatcher implements Watcher {

    /**
     * A set of {@link Path} objects. This set is used to deduplicate file paths that trigger multiple modify events
     * within the specified delay period.
     */
    private final Set<Path> eventSet = new ConcurrentHashSet<>();
    /**
     * The actual {@link Watcher} instance that will handle the events after processing. This watcher should not be
     * another {@code DelayWatcher} to avoid infinite recursion.
     */
    private final Watcher watcher;
    /**
     * The delay time in milliseconds. Modify events for the same file within this duration will be debounced.
     */
    private final long delay;

    /**
     * Constructs a new {@code DelayWatcher} with the specified actual {@link Watcher} and delay time.
     *
     * @param watcher The actual {@link Watcher} to which events will be delegated after processing. This cannot be
     *                another {@code DelayWatcher} to prevent circular dependencies.
     * @param delay   The delay time in milliseconds for debouncing modify events. If less than 1, modify events are
     *                handled immediately without delay.
     * @throws IllegalArgumentException If the provided {@code watcher} is {@code null} or is an instance of
     *                                  {@code DelayWatcher}.
     */
    public DelayWatcher(final Watcher watcher, final long delay) {
        Assert.notNull(watcher);
        if (watcher instanceof DelayWatcher) {
            throw new IllegalArgumentException("Watcher must not be a DelayWatcher");
        }
        this.watcher = watcher;
        this.delay = delay;
    }

    /**
     * Handles file modification events. If the delay is active ({@code delay >= 1}), this method debounces multiple
     * modify events for the same file within the delay period. Otherwise, it delegates the event directly to the
     * wrapped {@link Watcher}.
     *
     * @param event The {@link WatchEvent} that occurred. The modified file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onModify(final WatchEvent<?> event, final WatchKey key) {
        if (this.delay < 1) {
            this.watcher.onModify(event, key);
        } else {
            onDelayModify(event, key);
        }
    }

    /**
     * Handles file creation events. This method directly delegates the event to the wrapped {@link Watcher} without any
     * delay.
     *
     * @param event The {@link WatchEvent} that occurred. The created file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onCreate(final WatchEvent<?> event, final WatchKey key) {
        watcher.onCreate(event, key);
    }

    /**
     * Handles file deletion events. This method directly delegates the event to the wrapped {@link Watcher} without any
     * delay.
     *
     * @param event The {@link WatchEvent} that occurred. The deleted file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onDelete(final WatchEvent<?> event, final WatchKey key) {
        watcher.onDelete(event, key);
    }

    /**
     * Handles overflow events. This method directly delegates the event to the wrapped {@link Watcher} without any
     * delay.
     *
     * @param event The {@link WatchEvent} that occurred. The context of the overflow event can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onOverflow(final WatchEvent<?> event, final WatchKey key) {
        watcher.onOverflow(event, key);
    }

    /**
     * Triggers a delayed modification event. This method adds the event's path to a set to prevent immediate
     * re-processing of duplicate events and starts a new thread to handle the event after the specified delay.
     *
     * @param event The {@link WatchEvent} representing the modification.
     * @param key   The {@link WatchKey} associated with the event.
     */
    private void onDelayModify(final WatchEvent<?> event, final WatchKey key) {
        final Path eventPath = Paths.get(key.watchable().toString(), event.context().toString());
        if (eventSet.contains(eventPath)) {
            // This event has already been triggered; subsequent events are ignored and will be handled uniformly later.
            return;
        }

        // This is the first trigger for this event. Mark it and start a delayed processing thread.
        // The mark will be removed after processing.
        eventSet.add(eventPath);
        startHandleModifyThread(event, key);
    }

    /**
     * Starts a new thread to handle the delayed modification event. The thread sleeps for the specified delay, then
     * removes the event's path from the {@code eventSet} and delegates the event to the wrapped {@link Watcher}.
     *
     * @param event The {@link WatchEvent} representing the modification.
     * @param key   The {@link WatchKey} associated with the event.
     */
    private void startHandleModifyThread(final WatchEvent<?> event, final WatchKey key) {
        ThreadKit.execute(() -> {
            ThreadKit.sleep(delay);
            eventSet.remove(Paths.get(key.watchable().toString(), event.context().toString()));
            watcher.onModify(event, key);
        });
    }

}
