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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.lang.Chain;

/**
 * A chain of {@link Watcher} instances. This class allows multiple watchers to be grouped together and notified
 * sequentially for file system events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatcherChain implements Watcher, Chain<Watcher, WatcherChain> {

    /**
     * The list of {@link Watcher} instances in this chain.
     */
    final private List<Watcher> chain;

    /**
     * Constructs a new {@code WatcherChain} with the specified array of watchers.
     *
     * @param watchers An array of {@link Watcher} instances to be included in this chain.
     */
    public WatcherChain(final Watcher... watchers) {
        chain = Arrays.asList(watchers);
    }

    /**
     * Creates a new {@code WatcherChain} instance with the given watchers.
     *
     * @param watchers An array of {@link Watcher} instances to form the chain.
     * @return A new {@code WatcherChain} instance.
     */
    public static WatcherChain of(final Watcher... watchers) {
        return new WatcherChain(watchers);
    }

    /**
     * Notifies all watchers in the chain that a file or directory has been created.
     *
     * @param event The {@link WatchEvent} that occurred. The created file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onCreate(final WatchEvent<?> event, final WatchKey key) {
        for (final Watcher watcher : chain) {
            watcher.onCreate(event, key);
        }
    }

    /**
     * Notifies all watchers in the chain that a file or directory has been modified.
     *
     * @param event The {@link WatchEvent} that occurred. The modified file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onModify(final WatchEvent<?> event, final WatchKey key) {
        for (final Watcher watcher : chain) {
            watcher.onModify(event, key);
        }
    }

    /**
     * Notifies all watchers in the chain that a file or directory has been deleted.
     *
     * @param event The {@link WatchEvent} that occurred. The deleted file or directory name can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onDelete(final WatchEvent<?> event, final WatchKey key) {
        for (final Watcher watcher : chain) {
            watcher.onDelete(event, key);
        }
    }

    /**
     * Notifies all watchers in the chain that an overflow event has occurred.
     *
     * @param event The {@link WatchEvent} that occurred. The context of the overflow event can be obtained via
     *              {@link WatchEvent#context()}.
     * @param key   The {@link WatchKey} on which the event occurred. The monitored path can be obtained via
     *              {@link WatchKey#watchable()}.
     */
    @Override
    public void onOverflow(final WatchEvent<?> event, final WatchKey key) {
        for (final Watcher watcher : chain) {
            watcher.onOverflow(event, key);
        }
    }

    /**
     * Returns an iterator over the {@link Watcher} instances in this chain.
     *
     * @return An {@link Iterator} over the watchers.
     */
    @Override
    public Iterator<Watcher> iterator() {
        return this.chain.iterator();
    }

    /**
     * Adds a {@link Watcher} to the end of this chain.
     *
     * @param element The {@link Watcher} to add to the chain.
     * @return This {@code WatcherChain} instance, allowing for method chaining.
     */
    @Override
    public WatcherChain addChain(final Watcher element) {
        this.chain.add(element);
        return this;
    }

}
