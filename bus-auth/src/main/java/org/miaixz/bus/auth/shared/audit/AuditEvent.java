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
package org.miaixz.bus.auth.shared.audit;

import java.time.Instant;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Carries one immutable sanitized authentication audit event for an external sink.
 * <p>
 * The event contains stable references and safe structured detail only. Raw tokens, secrets, authorization codes,
 * passwords, ID Tokens, private Vendor payloads, exceptions, stacks, and arbitrary object string representations are
 * prohibited and must be removed by AuditSanitizer before construction.
 * </p>
 *
 * @param at        event occurrence instant from the shared Clock
 * @param category  stable internal audit category
 * @param outcome   stable internal audit outcome
 * @param requestId invocation correlation identifier
 * @param subject   optional stable Subject reference
 * @param resource  optional Provider or Source Registry reference
 * @param error     optional shared Bus error definition
 * @param details   immutable sanitized structured details
 * @author Kimi Liu
 */
public record AuditEvent(Instant at, AuditCategory category, AuditOutcome outcome, Context.RequestId requestId,
        Optional<Subject.Reference> subject, Optional<Registry.Reference> resource, Optional<Errors> error,
        JsonValue.ObjectValue details) {

    /**
     * Creates a detached immutable sanitized audit event.
     *
     * @param at        occurrence instant
     * @param category  audit category
     * @param outcome   audit result classification
     * @param requestId invocation correlation identifier
     * @param subject   optional Subject reference
     * @param resource  optional Provider or Source reference
     * @param error     optional shared Bus error
     * @param details   sanitized structured detail
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     */
    public AuditEvent {
        Assert.notNull(at, "Audit event instant must not be null");
        Assert.notNull(category, "Audit event category must not be null");
        Assert.notNull(outcome, "Audit event outcome must not be null");
        Assert.notNull(requestId, "Audit event request id must not be null");
        Assert.notNull(subject, "Audit event Subject container must not be null");
        subject = Optional.ofNullable(subject.getOrNull());
        Assert.notNull(resource, "Audit event resource container must not be null");
        resource = Optional.ofNullable(resource.getOrNull());
        Assert.notNull(error, "Audit event error container must not be null");
        error = Optional.ofNullable(error.getOrNull());
        Assert.notNull(details, "Audit event details must not be null");
        details = new JsonValue.ObjectValue(details.values());
    }

}
