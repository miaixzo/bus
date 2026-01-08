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
