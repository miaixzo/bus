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
package org.miaixz.bus.core.center.queue;

import java.io.Serial;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A custom {@link LinkedBlockingQueue} with pre-add checking. Given a check function, it checks before adding elements.
 * The principle is to check the remaining memory through Runtime#freeMemory(), and when the remaining memory is below
 * the specified threshold, no more elements are added.
 *
 * @param <E> the type of elements
 * @author Kimi Liu
 * @since Java 21+
 */
public class CheckedLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    @Serial
    private static final long serialVersionUID = 2852279169056L;

    /**
     * The checker function.
     */
    protected final Predicate<E> checker;

    /**
     * Constructs a new CheckedLinkedBlockingQueue.
     *
     * @param checker the checker function
     */
    public CheckedLinkedBlockingQueue(final Predicate<E> checker) {
        super(Integer.MAX_VALUE);
        this.checker = checker;
    }

    /**
     * Constructs a new CheckedLinkedBlockingQueue.
     *
     * @param c       the initial collection
     * @param checker the checker function
     */
    public CheckedLinkedBlockingQueue(final Collection<? extends E> c, final Predicate<E> checker) {
        super(c);
        this.checker = checker;
    }

    /**
     * Associates the specified value with the specified key in this cache.
     *
     * @param e the element to add
     */
    @Override
    public void put(final E e) throws InterruptedException {
        if (checker.test(e)) {
            super.put(e);
        }
    }

    /**
     * Offer method.
     *
     * @return the boolean value
     */
    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return checker.test(e) && super.offer(e, timeout, unit);
    }

    /**
     * Offer method.
     *
     * @return the boolean value
     */
    @Override
    public boolean offer(final E e) {
        return checker.test(e) && super.offer(e);
    }

}
