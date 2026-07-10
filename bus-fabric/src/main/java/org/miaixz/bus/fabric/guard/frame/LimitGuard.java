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

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
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
     * Protocol maximum frame queue bytes.
     */
    private static final long MAX_BYTES = 16_777_216L;

    /**
     * Rule name.
     */
    private static final String NAME = "frame-limit";

    /**
     * Maximum allowed bytes.
     */
    private final long maxBytes;

    /**
     * Creates a limit guard.
     *
     * @param maxBytes maximum allowed bytes
     */
    private LimitGuard(final long maxBytes) {
        this.maxBytes = validateMaxBytes(maxBytes);
    }

    /**
     * Creates a limit guard.
     *
     * @param maxBytes maximum allowed bytes
     * @return limit guard
     */
    public static LimitGuard of(final long maxBytes) {
        return new LimitGuard(maxBytes);
    }

    /**
     * Checks a single frame length.
     *
     * @param frame frame
     * @return guard result
     */
    public GuardResult check(final Frame frame) {
        if (frame == null) {
            throw new ValidateException("Frame must not be null");
        }
        final int length = frame.length();
        if (length < 0 || length > MAX_BYTES) {
            throw new ProtocolException("Frame length must be between 0 and 16777216");
        }
        return length > maxBytes ? GuardResult.reject("frame length " + length + " exceeds max " + maxBytes)
                : GuardResult.pass();
    }

    /**
     * Checks queued frame bytes.
     *
     * @param queuedBytes queued bytes
     * @return guard result
     */
    public GuardResult checkQueue(final long queuedBytes) {
        if (queuedBytes < 0) {
            throw new ValidateException("Queued bytes must be non-negative");
        }
        return queuedBytes > maxBytes ? GuardResult.reject("frame queue " + queuedBytes + " exceeds max " + maxBytes)
                : GuardResult.pass();
    }

    /**
     * Returns maximum bytes.
     *
     * @return maximum bytes
     */
    public long maxBytes() {
        return maxBytes;
    }

    /**
     * Returns rule name.
     *
     * @return rule name
     */
    public String name() {
        return NAME;
    }

    /**
     * Validates maximum bytes.
     *
     * @param maxBytes maximum bytes
     * @return maximum bytes
     */
    private static long validateMaxBytes(final long maxBytes) {
        if (maxBytes <= 0 || maxBytes > MAX_BYTES) {
            throw new ValidateException("Frame limit must be between 1 and 16777216");
        }
        return maxBytes;
    }

}
