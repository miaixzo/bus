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
/**
 * Defines structured security audit events, sanitization, and the external delivery port.
 * <p>
 * {@link org.miaixz.bus.auth.shared.audit.AuditCategory} and {@link org.miaixz.bus.auth.shared.audit.AuditOutcome}
 * provide closed classification values; {@link org.miaixz.bus.auth.shared.audit.AuditEvent} carries bounded,
 * provider-neutral event data, and {@link org.miaixz.bus.auth.shared.audit.AuditSanitizer} removes or summarizes values
 * before the event reaches an externally implemented {@link org.miaixz.bus.auth.shared.audit.AuditSink}.
 * </p>
 * <p>
 * Registry, runtime, protocol, Vendor, identity, and guard code emit events through the injected sink. This package
 * contains no database, queue, logger, network exporter, retention policy, operator authorization, or project-specific
 * schema implementation; those choices remain outside bus-auth.
 * </p>
 * <p>
 * Events must never contain plaintext credentials, tokens, authorization codes, verifiers, nonces, session keys,
 * assertions, private keys, signatures, complete request or response bodies, personal profiles, exceptions, or stack
 * traces. Identifiers and network facts are retained only when required for security correlation and must be bounded or
 * safely summarized before asynchronous delivery.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.audit;
