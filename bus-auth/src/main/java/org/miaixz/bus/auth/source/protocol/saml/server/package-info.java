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
 * Implements the SAML 2.0 Identity Provider direction.
 * <p>
 * {@link org.miaixz.bus.auth.source.protocol.saml.server.SamlIdentityProvider} coordinates the supported server
 * operations. SingleSignOnService validates AuthnRequest and creates a standard Response containing issued assertions;
 * SingleLogoutService processes LogoutRequest and LogoutResponse; MetadataService publishes EntityDescriptor content;
 * SamlErrorMapper creates a standards-compliant Response Status when returning an error is safe. SamlServerScheme and
 * SamlServerOptions declare only executable IdP capabilities.
 * </p>
 * <p>
 * This package consumes SAML models, codecs, security validators, verified subject and session context, consent and
 * claim ports, Assertion issuance, replay storage, Policies, and Fabric transport. It does not implement user
 * authentication, project persistence, permissions, SP behavior, Vendor integration, direct Roster access, or a
 * JSON/OAuth/framework response envelope.
 * </p>
 * <p>
 * Requests are bound to issuer, destination, service endpoint, binding, request ID, issue instant, requested context,
 * exact registered ACS, and one Timeout. Responses bind InResponseTo, destination, audience, recipient, subject
 * confirmation, session, and time conditions. Signing and encryption use allowed algorithms and active keys; metadata
 * advertises only those operational bindings and keys. Unsafe or untrusted requests terminate without reflecting
 * attacker-controlled XML, identifiers, attributes, signatures, keys, or stack information.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol.saml.server;
