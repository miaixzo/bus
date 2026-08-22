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
 * Defines reusable authentication-specific validation and replay guards.
 * <p>
 * Algorithm, audience, issuer, redirect URI, scope, and time guards enforce constraints already selected by a concrete
 * registration and protocol profile. {@link org.miaixz.bus.auth.guard.ClientAuthenticator} applies registered client
 * authentication policy, {@link org.miaixz.bus.auth.guard.SecretGuard} validates and converts short-lived secret
 * material, and {@link org.miaixz.bus.auth.guard.ReplayGuard} narrows the injected replay cache to atomic one-time use.
 * </p>
 * <p>
 * Protocol services, shared security components, and Vendor adapters compose these guards after decoding a typed
 * request. Guards depend on root contracts, worker-provided leases, cache ports, and bus-core validation or crypto
 * primitives; they do not invoke Registry, load configuration, choose a Provider, or implement protocol flow. Network
 * address, proxy, TLS, and transport enforcement remains owned by Fabric AddressPolicy and its guards rather than being
 * repeated here.
 * </p>
 * <p>
 * Validation is fail-closed and uses the invocation Timeout clock, exact registered identifiers, constant-time secret
 * comparison where applicable, and atomic replay keys isolated by space and purpose. Failures reveal no secret, token,
 * code, verifier, assertion, signing input, complete URI, or stack trace and never downgrade a malformed security value
 * to an absent optional value.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.guard;
