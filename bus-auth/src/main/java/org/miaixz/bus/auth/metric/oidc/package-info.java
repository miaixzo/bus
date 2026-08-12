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
 * Implements the internal OpenID Connect protocol engine behind the exported {@link org.miaixz.bus.auth.metric.OIDC}
 * provider and relying-party contracts.
 * <p>
 * This package depends in one direction on the exported OAuth 2.0 contracts, the hardened JWT verifier, shared strict
 * JSON and URI validation, the atomic state store, and the HTTP transport port supplied by
 * {@link org.miaixz.bus.auth.metric.AuthMetric.Runtime}. It does not import the internal OAuth engine package, expose
 * web-framework response types, perform direct network access, or create a second JSON, JOSE, cache, or transport
 * abstraction. Discovery, JSON Web Key Set, UserInfo, and logout endpoints are validated before the runtime transport
 * is invoked. ID Token validation delegates signature, time, issuer, audience, and replay enforcement to the shared JWT
 * pipeline before applying OpenID Connect nonce, authorized-party, and subject rules.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.oidc;
