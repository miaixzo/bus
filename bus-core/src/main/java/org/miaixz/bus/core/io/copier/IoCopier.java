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
