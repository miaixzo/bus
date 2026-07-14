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
package org.miaixz.bus.fabric.guard.body;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.guard.GuardResult;
import org.miaixz.bus.fabric.guard.GuardRule;

/**
 * Body length guard rule.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LimitGuard implements GuardRule {

    /**
     * Rule name.
     */
    private static final String NAME = "body-limit";

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
     * Checks message body length.
     *
     * @param message message
     * @return guard result
     */
    @Override
    public GuardResult check(final Message message) {
        final Message checkedMessage = Assert.notNull(message, () -> new ValidateException("Message must not be null"));
        final long length = knownLength(checkedMessage);
        Assert.isTrue(length >= Normal.__1, () -> new ProtocolException("Body length must be -1 or greater"));
        return length > maxBytes ? GuardResult.reject("body length " + length + " exceeds max " + maxBytes)
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
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Resolves known body length from payload and headers.
     *
     * @param message message
     * @return known length or -1
     */
    private static long knownLength(final Message message) {
        final long payloadLength = message.payload().length();
        Assert.isTrue(payloadLength >= Normal.__1, () -> new ProtocolException("Body length must be -1 or greater"));
        final int headerLength = message.headers().contentLength();
        if (payloadLength >= 0 && headerLength >= 0) {
            return Math.max(payloadLength, headerLength);
        }
        return payloadLength >= 0 ? payloadLength : headerLength;
    }

    /**
     * Validates maximum bytes.
     *
     * @param maxBytes maximum bytes
     * @return maximum bytes
     */
    private static long validateMaxBytes(final long maxBytes) {
        Assert.isTrue(
                maxBytes > 0 && maxBytes <= Normal._16 * Normal.MEBI,
                () -> new ValidateException("Body limit must be between 1 and 16777216"));
        return maxBytes;
    }

}
