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
