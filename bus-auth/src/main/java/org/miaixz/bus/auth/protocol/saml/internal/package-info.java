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
 * Compiles and assembles non-exported SAML 2.0 runtime components.
 * <p>
 * SamlProviderDriver and SamlSourceDriver bind the Identity Provider and Service Provider profiles to exact
 * registration validation and bind each declared SSO, SLO, metadata, binding, signing, encryption, and
 * assertion-consumer capability to one runtime implementation.
 * {@link org.miaixz.bus.auth.protocol.saml.internal.AssertionIssuer} creates standard assertions from already verified
 * subject, consent, claim, session, and authentication context inputs.
 * </p>
 * <p>
 * RuntimeBuilder receives these drivers through the public Saml facade. This package may depend on SAML models, codecs,
 * security services, profiles, resolvers, atomic stores, shared cryptography, SecurityBaseline, and runtime
 * contributions. It does not expose public protocol operations, load project data, discover classes through reflection
 * or ServiceLoader, retain mutable global state, call Vendor adapters, or implement persistence, permissions, or user
 * authentication.
 * </p>
 * <p>
 * Compilation fails closed for mismatched direction, protocol, namespace, entity ID, endpoint, binding, key purpose,
 * algorithm, trust material, conformance, settings, or manifest. No partial Provider or Source is published. Assertion
 * issuance and request handling share one Context and Budget, create unique unpredictable IDs, use UTC instants and
 * bounded validity, and keep XML, attributes, assertions, session indexes, private keys, and decrypted material out of
 * runtime diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.saml.internal;
