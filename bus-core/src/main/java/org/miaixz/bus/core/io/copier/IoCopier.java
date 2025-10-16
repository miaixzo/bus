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
package org.miaixz.bus.core.io.copier;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Normal;

/**
 * Abstract IO copier, allowing customization including buffer, progress bar, and other information. This object is not
 * thread-safe.
 *
 * @param <S> The type of the copy source, such as InputStream, Reader, etc.
 * @param <T> The type of the copy target, such as OutputStream, Writer, etc.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class IoCopier<S, T> {

    /**
     * The buffer size.
     */
    protected final int bufferSize;
    /**
     * The total number of bytes to copy. Use {@link Long#MAX_VALUE} for unlimited.
     */
    protected final long count;

    /**
     * The stream progress listener.
     */
    protected StreamProgress progress;

    /**
     * Whether to flush after writing each buffer content.
     */
    protected boolean flushEveryBuffer;

    /**
     * Constructs an {@code IoCopier} with the specified buffer size, total count, and progress listener.
     *
     * @param bufferSize The buffer size. If less than or equal to 0, the default {@link Normal#_8192} is used.
     * @param count      The total number of bytes to copy. If less than or equal to 0, {@link Long#MAX_VALUE}
     *                   (unlimited) is used.
     * @param progress   The progress listener.
     */
    public IoCopier(final int bufferSize, final long count, final StreamProgress progress) {
        this.bufferSize = bufferSize > 0 ? bufferSize : Normal._8192;
        this.count = count <= 0 ? Long.MAX_VALUE : count;
        this.progress = progress;
    }

    /**
     * Executes the copy operation.
     *
     * @param source The copy source, such as InputStream, Reader, etc.
     * @param target The copy target, such as OutputStream, Writer, etc.
     * @return The actual number of bytes copied.
     */
    public abstract long copy(S source, T target);

    /**
     * Calculates the buffer size, taking the minimum of the default buffer size and the target length.
     *
     * @param count The target length.
     * @return The calculated buffer size.
     */
    protected int bufferSize(final long count) {
        return (int) Math.min(this.bufferSize, count);
    }

    /**
     * Sets whether to flush after writing each buffer content.
     *
     * @param flushEveryBuffer {@code true} to flush after each buffer write, {@code false} otherwise.
     * @return This {@link IoCopier} instance.
     */
    public IoCopier<S, T> setFlushEveryBuffer(final boolean flushEveryBuffer) {
        this.flushEveryBuffer = flushEveryBuffer;
        return this;
    }

}
