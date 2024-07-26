/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
