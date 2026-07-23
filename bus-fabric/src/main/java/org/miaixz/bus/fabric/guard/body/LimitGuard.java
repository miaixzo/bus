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
import org.miaixz.bus.fabric.Builder;
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
     * Inclusive upper bound for declared and known payload lengths.
     */
    private final long maxBytes;

    /**
     * Creates a limit guard.
     *
     * @param maxBytes validated inclusive body limit from 1 byte through 16 MiB
     */
    private LimitGuard(final long maxBytes) {
        this.maxBytes = validateMaxBytes(maxBytes);
    }

    /**
     * Creates a limit guard.
     *
     * @param maxBytes inclusive body limit from 1 byte through 16 MiB
     * @return new body-length guard
     */
    public static LimitGuard of(final long maxBytes) {
        return new LimitGuard(maxBytes);
    }

    /**
     * Checks message body length.
     *
     * @param message non-null message whose declared and payload lengths are compared
     * @return rejection when either known length exceeds the limit or two known lengths differ; otherwise pass
     */
    @Override
    public GuardResult check(final Message message) {
        final Message checkedMessage = Assert.notNull(message, () -> new ValidateException("Message must not be null"));
        final long declaredLength = checkedMessage.headers().contentLength();
        if (declaredLength > maxBytes) {
            return GuardResult.reject("declared body length exceeds max " + maxBytes);
        }
        final long payloadLength = checkedMessage.payload().length();
        Assert.isTrue(payloadLength >= Normal.__1, () -> new ProtocolException("Body length must be -1 or greater"));
        if (payloadLength > maxBytes) {
            return GuardResult.reject("actual body length exceeds max " + maxBytes);
        }
        if (declaredLength >= Normal._0 && payloadLength >= Normal._0 && declaredLength != payloadLength) {
            return GuardResult.reject("declared and actual body lengths differ");
        }
        return GuardResult.pass();
    }

    /**
     * Returns maximum bytes.
     *
     * @return configured inclusive body-length limit
     */
    public long maxBytes() {
        return maxBytes;
    }

    /**
     * Returns rule name.
     *
     * @return shared limit-guard name from {@link Builder}
     */
    @Override
    public String name() {
        return Builder.LIMIT_GUARD_NAME;
    }

    /**
     * Validates maximum bytes.
     *
     * @param maxBytes candidate inclusive body limit
     * @return unchanged limit from 1 byte through 16 MiB
     */
    private static long validateMaxBytes(final long maxBytes) {
        Assert.isTrue(
                maxBytes > 0 && maxBytes <= Builder.BYTES_16_MIB,
                () -> new ValidateException("Body limit must be between 1 and 16777216"));
        return maxBytes;
    }

}
