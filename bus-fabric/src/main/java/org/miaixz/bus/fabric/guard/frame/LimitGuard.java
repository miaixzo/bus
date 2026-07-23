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
package org.miaixz.bus.fabric.guard.frame;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.guard.GuardResult;

/**
 * Frame length and write queue guard.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LimitGuard {

    /**
     * Inclusive upper bound applied independently to a frame and to queued frame bytes.
     */
    private final long maxBytes;

    /**
     * Creates a limit guard.
     *
     * @param maxBytes validated inclusive limit from 1 byte through 16 MiB
     */
    private LimitGuard(final long maxBytes) {
        this.maxBytes = validateMaxBytes(maxBytes);
    }

    /**
     * Creates a limit guard.
     *
     * @param maxBytes inclusive frame and queue limit from 1 byte through 16 MiB
     * @return new frame-length and queue-length guard
     */
    public static LimitGuard of(final long maxBytes) {
        return new LimitGuard(maxBytes);
    }

    /**
     * Checks a single frame length.
     *
     * @param frame non-null frame whose validated payload length is checked
     * @return rejection when the frame length exceeds the configured limit; otherwise pass
     */
    public GuardResult check(final Frame frame) {
        final Frame checkedFrame = Assert.notNull(frame, () -> new ValidateException("Frame must not be null"));
        final int length = checkedFrame.length();
        Assert.isTrue(
                length >= Normal._0 && length <= Builder.BYTES_16_MIB,
                () -> new ProtocolException("Frame length must be between 0 and 16777216"));
        return length > maxBytes ? GuardResult.reject("frame length " + length + " exceeds max " + maxBytes)
                : GuardResult.pass();
    }

    /**
     * Checks queued frame bytes.
     *
     * @param queuedBytes non-negative number of frame bytes currently queued for writing
     * @return rejection when the queue length exceeds the configured limit; otherwise pass
     */
    public GuardResult checkQueue(final long queuedBytes) {
        Assert.isTrue(queuedBytes >= Normal._0, () -> new ValidateException("Queued bytes must be non-negative"));
        return queuedBytes > maxBytes ? GuardResult.reject("frame queue " + queuedBytes + " exceeds max " + maxBytes)
                : GuardResult.pass();
    }

    /**
     * Returns maximum bytes.
     *
     * @return configured inclusive frame and queue limit
     */
    public long maxBytes() {
        return maxBytes;
    }

    /**
     * Returns rule name.
     *
     * @return shared frame-limit guard name from {@link Builder}
     */
    public String name() {
        return Builder.GUARD_FRAME_LIMIT_GUARD_NAME;
    }

    /**
     * Validates maximum bytes.
     *
     * @param maxBytes candidate inclusive frame and queue limit
     * @return unchanged limit from 1 byte through 16 MiB
     */
    private static long validateMaxBytes(final long maxBytes) {
        Assert.isTrue(
                maxBytes > Normal._0 && maxBytes <= Builder.BYTES_16_MIB,
                () -> new ValidateException("Frame limit must be between 1 and 16777216"));
        return maxBytes;
    }

}
