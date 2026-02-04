/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.ref;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Enumeration of reference types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ReferenceType {

    /**
     * A strong reference, which is not garbage collected.
     */
    STRONG,
    /**
     * A soft reference, which is garbage collected when the JVM reports low memory.
     */
    SOFT,
    /**
     * A weak reference, which is garbage collected when it is discovered during a GC cycle.
     */
    WEAK,
    /**
     * A phantom reference. When a phantom reference is discovered during a GC cycle, the {@link PhantomReference}
     * object is enqueued on its {@link ReferenceQueue}. The referenced object is not yet reclaimed. It will be
     * reclaimed only after the {@link ReferenceQueue} is processed.
     */
    PHANTOM

}
