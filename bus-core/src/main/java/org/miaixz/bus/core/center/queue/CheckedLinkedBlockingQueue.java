/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
