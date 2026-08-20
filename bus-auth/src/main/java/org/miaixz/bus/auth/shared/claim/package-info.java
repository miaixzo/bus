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
 * Defines immutable claim sets and explicit attribute-to-claim mapping.
 * <p>
 * {@link org.miaixz.bus.auth.shared.claim.ClaimSet} retains standard and registered extension values as a typed
 * provider-neutral JSON object. {@link org.miaixz.bus.auth.shared.claim.ClaimMapping} declares one explicit source,
 * target, conversion, and release rule, while {@link org.miaixz.bus.auth.shared.claim.ClaimMapper} applies a frozen
 * ordered mapping to verified subject attributes.
 * </p>
 * <p>
 * Identity and protocol token or assertion services compose this package after subject resolution and consent. Claim
 * mapping depends on typed JSON values and explicit project-provided declarations; it does not inspect Registry, call a
 * Source, load attributes implicitly, run scripts or expressions, or expose {@code Map<String,Object>} as a wire model.
 * </p>
 * <p>
 * Reserved claim names, value types, multiplicity, audience, requesting client, scope, consent, and protocol purpose
 * are validated before release. Missing or incompatible source values do not trigger name, email, token, or stringified
 * object fallback. Mappers must not copy secret attributes, internal identifiers, credentials, complete profiles, or
 * diagnostic objects into claims.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.claim;
