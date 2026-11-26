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
package org.miaixz.bus.errors;

import java.util.Objects;

/**
 * Facts verified by the execution boundary. A failure can be reported without a throwable.
 *
 * @param source     origin of the failure
 * @param descriptor explicit semantic classification, if known
 * @param status     verified protocol status, or zero when unavailable
 *
 * @author Kimi Liu
 */
public record FailureFacts(Source source, ErrorDescriptor descriptor, int status) {

    /**
     * Failure signal with no additional classification or protocol status.
     */
    public static final FailureFacts UNKNOWN = new FailureFacts(Source.LOCAL, null, 0);

    /**
     * Validates the verified status independently of the legacy application code.
     *
     * @param source     origin of the failure
     * @param descriptor explicit semantic classification, if known
     * @param status     verified protocol status, or zero when unavailable
     */
    public FailureFacts {
        Objects.requireNonNull(source, "source");
        if (status != 0 && (status < 400 || status > 599)) {
            throw new IllegalArgumentException("Failure status must be zero or an HTTP error");
        }
    }

    /**
     * Determines which boundary owns a failure response.
     *
     * @author Kimi Liu
     */
    public enum Source {
        /**
         * A failure raised within this operation.
         */
        LOCAL,
        /**
         * A response received from an upstream and eligible for the existing pass-through contract.
         */
        UPSTREAM_RESPONSE
    }

}
