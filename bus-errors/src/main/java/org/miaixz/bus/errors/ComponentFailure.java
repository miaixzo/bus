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
 * Reports failures from a component that owns its exception, return value or fallback contract. Each call represents
 * one component operation or recovery attempt. The original cause is used for classification and is never passed to
 * standard observers.
 *
 * @author Kimi Liu
 */
public final class ComponentFailure {

    /**
     * Stable labels and the local phase reused by every reported operation.
     */
    private final ErrorContext context;

    /**
     * Creates a reusable boundary with stable component and operation labels.
     *
     * @param component Bus module name
     * @param operation stable operation label, independent of input data
     */
    public ComponentFailure(String component, String operation) {
        this.context = new ErrorContext(component, operation, ErrorContext.Phase.LOCAL);
    }

    /**
     * Reports a terminal failure while the component retains its established return or throw contract.
     *
     * @param type    structured failure type selected by the component
     * @param failure diagnostic cause, or {@code null} for a structured rejection
     */
    public void failed(ErrorType type, Throwable failure) {
        report(type, 0, failure, true, null);
    }

    /**
     * Reports a library failure with a verified remote HTTP status.
     *
     * @param type    semantic failure type selected by the component
     * @param status  remote HTTP error status, or zero if unavailable
     * @param failure diagnostic cause, or null if the status is the only signal
     */
    public void failed(ErrorType type, int status, Throwable failure) {
        failed(type, status, failure, null);
    }

    /**
     * Reports a component-owned terminal failure with server-generated correlation.
     *
     * @param type semantic failure type
     * @param status verified HTTP status, or zero when absent
     * @param failure diagnostic cause, or null for a structured rejection
     * @param requestId server-generated request identifier, or null
     */
    public void failed(ErrorType type, int status, Throwable failure, String requestId) {
        report(type, status, failure, true, requestId);
    }

    /**
     * Reports a failed attempt when the component continues through a recovery or fallback path.
     *
     * @param type    structured failure type selected by the component
     * @param failure diagnostic cause, or {@code null} for a structured rejection
     */
    public void attempt(ErrorType type, Throwable failure) {
        report(type, 0, failure, false, null);
    }

    /**
     * Reports a failure and returns the same exception for propagation, preserving its type, identity and cause.
     *
     * @param type    structured failure type selected by the component
     * @param failure original exception
     * @param <T>     exception type preserved by the caller
     * @return the same exception
     */
    public <T extends Throwable> T propagate(ErrorType type, T failure) {
        failed(type, Objects.requireNonNull(failure, "failure"));
        return failure;
    }

    /**
     * Classifies the operation locally and emits one safe event without taking transport ownership.
     *
     * @param type     semantic classification supplied by the component
     * @param status   verified remote HTTP error status, or zero
     * @param failure  diagnostic cause
     * @param terminal whether this is the final failure at this boundary
     * @param requestId server-generated request identifier, or null
     */
    private void report(ErrorType type, int status, Throwable failure, boolean terminal, String requestId) {
        var operation = ErrorBoundary.standard().open();
        var facts = new FailureFacts(FailureFacts.Source.LOCAL, ErrorEngine.descriptor(type), status);
        var decision = operation.decide(this.context, facts, failure);
        operation.observe(this.context, decision, terminal, failure, requestId);
    }

}
