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
package org.miaixz.bus.core.io;

import org.miaixz.bus.core.lang.Normal;

/**
 * This class manages the lifecycle of {@link SectionBuffer} instances to avoid GC churn and zero-filling. The pool is a
 * thread-safe static singleton.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LifeCycle {

    /**
     * The maximum size of the pool in bytes. Currently 64 KiB.
     */
    public static final long MAX_SIZE = Normal._64 * Normal._1024;

    /**
     * The head of the singly-linked list of segments in the pool.
     */
    public static SectionBuffer next;

    /**
     * The total number of bytes currently in the pool.
     */
    public static long byteCount;

    /**
     * Private constructor to prevent instantiation.
     */
    private LifeCycle() {

    }

    /**
     * Retrieves a {@link SectionBuffer} from the pool. If the pool is empty, a new {@link SectionBuffer} is created.
     *
     * @return A {@link SectionBuffer} instance.
     */
    public static SectionBuffer take() {
        synchronized (LifeCycle.class) {
            if (null != next) {
                SectionBuffer result = next;
                next = result.next;
                result.next = null;
                byteCount -= SectionBuffer.SIZE;
                return result;
            }
        }
        return new SectionBuffer(); // Pool is empty. Don't zero-fill while holding a lock.
    }

    /**
     * Recycles a {@link SectionBuffer} back into the pool. A segment can only be recycled if it is not shared and is
     * not part of a circularly-linked list.
     *
     * @param segment The {@link SectionBuffer} to recycle.
     * @throws IllegalArgumentException if the segment is still part of a list.
     */
    public static void recycle(SectionBuffer segment) {
        if (segment.next != null || segment.prev != null)
            throw new IllegalArgumentException();
        if (segment.shared)
            return; // This segment cannot be recycled.
        synchronized (LifeCycle.class) {
            if (byteCount + SectionBuffer.SIZE > MAX_SIZE)
                return; // Pool is full.
            byteCount += SectionBuffer.SIZE;
            segment.next = next;
            segment.pos = segment.limit = 0;
            next = segment;
        }
    }

}
