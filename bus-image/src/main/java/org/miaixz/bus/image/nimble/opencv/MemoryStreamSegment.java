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
package org.miaixz.bus.image.nimble.opencv;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import javax.imageio.stream.MemoryCacheImageInputStream;

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryStreamSegment extends StreamSegment {

    private final ByteBuffer cache;

    MemoryStreamSegment(ByteBuffer b, ImageDescriptor imageDescriptor) {
        super(new long[] { 0 }, new long[] { b.limit() }, imageDescriptor);
        this.cache = b;
    }

    public static ByteArrayInputStream getByteArrayInputStream(MemoryCacheImageInputStream inputStream) {
        if (inputStream != null) {
            try {
                Field fid = MemoryCacheImageInputStream.class.getDeclaredField("stream");
                if (fid != null) {
                    fid.setAccessible(true);
                    return (ByteArrayInputStream) fid.get(inputStream);
                }
            } catch (Exception e) {
                Logger.error("Cannot get inputstream", e);
            }
        }
        return null;
    }

    public ByteBuffer getCache() {
        return cache;
    }

}
