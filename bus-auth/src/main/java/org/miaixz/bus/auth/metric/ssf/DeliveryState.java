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
package org.miaixz.bus.auth.metric.ssf;

import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable delivery attempt, deadline, and acknowledgment state.
 *
 * @param attempts        completed attempts
 * @param maximumAttempts maximum attempts
 * @param deadline        absolute deadline
 * @param acknowledged    terminal acknowledgment flag
 */
public record DeliveryState(int attempts, int maximumAttempts, Instant deadline, boolean acknowledged) {

    /**
     * Validates state.
     *
     * @param attempts        completed attempts
     * @param maximumAttempts maximum attempts
     * @param deadline        absolute deadline
     * @param acknowledged    terminal acknowledgment flag
     */
    public DeliveryState {
        Assert.isTrue(
                attempts >= Normal._0 && maximumAttempts > Normal._0 && attempts <= maximumAttempts,
                () -> new ValidateException("SSF delivery attempts are invalid"));
        deadline = Assert.notNull(deadline, () -> new ValidateException("SSF delivery deadline must not be null"));
    }

    /**
     * Advances one attempt.
     *
     * @param now current security time
     * @return advanced state
     */
    public DeliveryState attempt(final Instant now) {
        Assert.isTrue(
                !acknowledged && attempts < maximumAttempts && !now.isAfter(deadline),
                () -> new ValidateException("SSF delivery cannot be attempted"));
        return new DeliveryState(attempts + Normal._1, maximumAttempts, deadline, false);
    }

    /**
     * Marks delivery acknowledged.
     *
     * @return terminal state
     */
    public DeliveryState acknowledge() {
        Assert.isTrue(!acknowledged, () -> new ValidateException("SSF delivery is already acknowledged"));
        return new DeliveryState(attempts, maximumAttempts, deadline, true);
    }

    /**
     * Returns whether another attempt is allowed.
     *
     * @param now current security time
     * @return retry eligibility
     */
    public boolean retry(final Instant now) {
        return !acknowledged && attempts < maximumAttempts && !now.isAfter(deadline);
    }

}
