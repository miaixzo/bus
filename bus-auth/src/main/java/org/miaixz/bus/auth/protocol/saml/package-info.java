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
 * Defines the SAML 2.0 Core and Metadata models used by Identity Providers and Service Providers.
 * <p>
 * The package preserves AuthnRequest, Response, Assertion, LogoutRequest, LogoutResponse, Status, Subject, Conditions,
 * statements, attributes, entity descriptors, role descriptors, key descriptors, and service endpoints as typed SAML
 * values. Issuer, NameID, QName-based status codes, XML namespaces, binding identifiers, IDs, and timestamps retain
 * their OASIS-defined spelling and semantics.
 * </p>
 * <p>
 * SAML client, server, codec, security, and internal packages consume these immutable models. Shared cryptographic
 * components provide primitives without changing XML semantics. This package has no transport, persistence, Registry,
 * identity linking, project permissions, Vendor fields, generic protocol envelopes, or framework execution types.
 * HTTP-Artifact, ECP, and Name Identifier Management are outside the implemented SAML capability set.
 * </p>
 * <p>
 * XML content is represented only after namespace-aware, schema-constrained parsing with unique IDs and bounded size,
 * depth, text, and collection counts. Models never serialize Context, Timeout, Callback, Outcome, Bus errors,
 * exceptions, or internal settings. SAML failures use a standard Response Status when a response can be returned
 * safely; they are never converted to JSON or OAuth errors.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.saml;
