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
 * Defines the post-evidence identity, account-linking, claim, and session workflow.
 * <p>
 * {@link org.miaixz.bus.auth.identity.EvidenceService} evaluates verified authentication evidence before
 * {@link org.miaixz.bus.auth.identity.SubjectService} and {@link org.miaixz.bus.auth.identity.AccountLinkService}
 * resolve the local account boundary. {@link org.miaixz.bus.auth.identity.ClaimService} derives authorized claims,
 * {@link org.miaixz.bus.auth.identity.SessionService} manages framework sessions, and
 * {@link org.miaixz.bus.auth.identity.SignInService} coordinates those steps into an immutable
 * {@link org.miaixz.bus.auth.identity.SignInResult}. External identities enter through
 * {@link org.miaixz.bus.auth.identity.ExternalIdentityService}, not through a protocol-specific account path.
 * </p>
 * <p>
 * The workflow consumes verified evidence, root identity values, resolver ports, and cache-backed session ports. It
 * does not decode protocol wire, call a Source or Vendor adapter directly, perform Registry routing, or persist project
 * users. Transport and protocol services invoke identity only after their authentication checks have completed;
 * external projects implement project-specific account and attribute ports.
 * </p>
 * <p>
 * Account linking is explicit and deterministic: an unverified email, display name, access token, or mutable profile
 * attribute cannot select or merge a subject. Session issuance observes the invocation Budget and security baseline,
 * and claims contain only values authorized for the target client. This package does not define operator permissions,
 * roles, administration policy, or a second Source authentication flow.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.identity;
