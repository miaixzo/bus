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
 * Defines authorization-consent values and the external decision port.
 * <p>
 * {@link org.miaixz.bus.auth.shared.consent.ConsentRequest} identifies the subject, client, requested scopes, claims,
 * and operation requiring a decision; {@link org.miaixz.bus.auth.shared.consent.ConsentDecision} records the explicit
 * result; and {@link org.miaixz.bus.auth.shared.consent.Consent} represents a bounded granted consent.
 * {@link org.miaixz.bus.auth.worker.ConsentService} is implemented by the external project that owns consent
 * presentation, persistence, and revocation.
 * </p>
 * <p>
 * Protocol authorization services call the injected service after client and subject authentication and before issuing
 * an authorization result. This package contains no controller, user interface, database implementation, operator
 * permission, role, authentication policy, or implicit approval engine.
 * </p>
 * <p>
 * A decision is bound to the exact namespace, subject, client, redirecting operation, scope and claim set, and current
 * lifetime. Missing, stale, broader, or mismatched consent fails closed. Requests and decisions must not carry
 * credentials, tokens, authorization codes, session secrets, complete personal profiles, or arbitrary executable
 * expressions.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.consent;
