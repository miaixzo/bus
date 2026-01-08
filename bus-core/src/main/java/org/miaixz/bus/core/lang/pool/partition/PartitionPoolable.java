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
package org.miaixz.bus.core.lang.pool.partition;

import org.miaixz.bus.core.lang.pool.SimplePoolable;

/**
 * Represents a poolable object that belongs to a specific {@link PartitionPool}. This object wraps a raw object and
 * maintains a reference to its owning partition, allowing for direct return or freeing operations within that
 * partition.
 *
 * @param <T> the type of the raw object being pooled
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionPoolable<T> extends SimplePoolable<T> {

    /**
     * The {@link PartitionPool} to which this poolable object belongs.
     */
    private final PartitionPool<T> partition;

    /**
     * Constructs a new {@code PartitionPoolable} instance.
     *
     * @param raw       the raw object to be wrapped
     * @param partition the {@link PartitionPool} that owns this object
     */
    public PartitionPoolable(final T raw, final PartitionPool<T> partition) {
        super(raw);
        this.partition = partition;
    }

    /**
     * Returns the raw object wrapped by this instance to its owning {@link PartitionPool}.
     */
    public void returnObject() {
        this.partition.returnObject(this.getRaw());
    }

    /**
     * Frees (destroys) the raw object wrapped by this instance from its owning {@link PartitionPool}.
     */
    public void free() {
        this.partition.free(this.getRaw());
    }

}
