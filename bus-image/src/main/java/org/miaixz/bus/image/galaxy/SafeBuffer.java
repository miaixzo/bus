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
package org.miaixz.bus.image.galaxy;

import java.nio.Buffer;

/**
 * Utility to avoid {@link NoSuchMethodError} on builds with Java 9 running on Java 7 or Java 8 caused by overloaded
 * methods for derived classes of {@link Buffer} with covariant return types for {@link Buffer#clear()},
 * {@link Buffer#flip()}, {@link Buffer#limit(int)}, {@link Buffer#mark()}, {@link Buffer#position(int)},
 * {@link Buffer#reset()}, {@link Buffer#rewind()} added in Java 9. Usage: replace {@code buffer.clear()} by
 * {@code SafeBuffer.clear(buffer)}, ...
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SafeBuffer {

    public static Buffer clear(Buffer buf) {
        return buf.clear();
    }

    public static Buffer flip(Buffer buf) {
        return buf.flip();
    }

    public static Buffer limit(Buffer buf, int newLimit) {
        return buf.limit(newLimit);
    }

    public static Buffer mark(Buffer buf) {
        return buf.mark();
    }

    public static Buffer position(Buffer buf, int newPosition) {
        return buf.position(newPosition);
    }

    public static Buffer reset(Buffer buf) {
        return buf.reset();
    }

    public static Buffer rewind(Buffer buf) {
        return buf.rewind();
    }

}
