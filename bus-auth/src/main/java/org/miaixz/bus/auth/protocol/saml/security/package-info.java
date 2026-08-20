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
 * Enforces SAML 2.0 signature, assertion, replay, and decryption security.
 * <p>
 * SamlSignatureValidator verifies that an allowed signature covers the exact XML or Redirect-binding object consumed by
 * the caller. SamlAssertionValidator checks issuer, destination, audience, recipient, InResponseTo, subject
 * confirmation, time conditions, authentication context, and stable subject requirements. SamlReplayValidator consumes
 * response and assertion IDs atomically. SamlDecryptionService decrypts encrypted assertions or identifiers through
 * bus-crypto before the caller repeats all semantic validation over the resulting document.
 * </p>
 * <p>
 * SAML client and server services call this package after strict decoding and before identity or session effects. It
 * consumes typed SAML documents, explicit trust material, resolvers, atomic replay storage, SecurityBaseline, Context,
 * and Budget. It does not parse arbitrary XML, fetch untrusted keys, infer trust from KeyInfo, implement transport,
 * issue assertions, persist project state, invoke Registry, or contain Vendor-specific signing rules.
 * </p>
 * <p>
 * Algorithm and key-type allow-lists reject wrapping, duplicate IDs, detached or external references, transform abuse,
 * weak digest or signature algorithms, ambiguous trust anchors, and key substitution. Replay checks and time windows
 * fail closed and are scoped by issuer, recipient, and protocol purpose. Decrypted bytes and all temporary key material
 * are cleared at the operation boundary; XML, assertions, attributes, signatures, private keys, and failure causes are
 * never exposed through diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.saml.security;
