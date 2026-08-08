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
package org.miaixz.bus.image.nimble.opencv;

import java.nio.ByteBuffer;

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;

/**
 * Represents the MemoryStreamSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MemoryStreamSegment extends StreamSegment {

    /**
     * The cache value.
     */
    private final ByteBuffer cache;

    /**
     * Creates a new instance.
     *
     * @param b               the b.
     * @param imageDescriptor the image descriptor.
     */
    MemoryStreamSegment(ByteBuffer b, ImageDescriptor imageDescriptor) {
        super(new long[] { 0 }, new long[] { b.limit() }, imageDescriptor);
        this.cache = b;
    }

    /**
     * Gets the cache.
     *
     * @return the cache.
     */
    public ByteBuffer getCache() {
        return cache;
    }

}
