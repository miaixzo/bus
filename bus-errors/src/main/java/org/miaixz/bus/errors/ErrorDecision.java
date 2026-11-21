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

/**
 * Side-effect-free classification and disposition for a failure.
 *
 * @param action        operation the boundary may perform
 * @param error         classified failure, absent when there is no public classification
 * @param status        verified HTTP error status, or zero when unavailable
 * @param basis         stable classification reason without diagnostic text
 * @param policyVersion listener snapshot used for this operation, or zero when absent
 *
 * @author Kimi Liu
 */
public record ErrorDecision(Action action, ErrorDescriptor error, int status, Basis basis, long policyVersion) {

    /**
     * Records the source of a classification without exposing the original exception.
     *
     * @author Kimi Liu
     */
    public enum Basis {
        /**
         * Cancellation or interruption from the execution context.
         */
        CANCELLATION,
        /**
         * Fatal JVM signal left to the runtime.
         */
        FATAL_SIGNAL,
        /**
         * Verified upstream response.
         */
        UPSTREAM,
        /**
         * Structured error descriptor supplied by the execution boundary.
         */
        VERIFIED_FACT,
        /**
         * Descriptor carried by a {@link ClassifiedException}.
         */
        STRUCTURED_EXCEPTION,
        /**
         * Result supplied by a registered classifier.
         */
        CLASSIFIER,
        /**
         * Standard projection of a verified status or unknown failure.
         */
        STANDARD
    }

    /**
     * Selects the boundary action without performing transport or recovery I/O.
     *
     * @author Kimi Liu
     */
    public enum Action {
        /**
         * Leave the original failure to the caller.
         */
        PROPAGATE,
        /**
         * Keep the upstream response in its existing pass-through path.
         */
        PASS_THROUGH,
        /**
         * Project the failure to a response while headers are still writable.
         */
        RENDER,
        /**
         * Apply the registered protocol termination contract.
         */
        TERMINATE
    }

}
