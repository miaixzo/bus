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
package org.miaixz.bus.auth.worker;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.audit.AuditEvent;

/**
 * Accepts sanitized authentication audit events for external persistence or delivery.
 * <p>
 * The integrating project owns storage and delivery policy. Exceptional completion indicates only that audit writing
 * failed; it is not a protocol rejection, must not expose event-sensitive material, and is handled by the calling
 * operation's explicit audit-failure policy.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface AuditSink {

    /**
     * Accepts one already sanitized immutable audit event within the existing operation budget.
     *
     * @param event   sanitized immutable audit event
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage completed when external audit delivery succeeds
     */
    CompletionStage<Void> accept(AuditEvent event, Context context, Timeout.Budget timeout);

}
