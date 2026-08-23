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
 * Defines formal protocol conformance metadata and contains only formal authentication protocol subpackages.
 * <p>
 * {@link org.miaixz.bus.auth.Scheme.Conformance} binds an exact bus-core protocol identifier and version to immutable
 * normative citations and a precise implementation statement. OAuth 2.x, OpenID Connect, SAML, SCIM, LDAP, and RADIUS
 * keep their standard wire models, codecs, client or server roles, security logic, and runtime assembly in dedicated
 * child packages.
 * </p>
 * <p>
 * Protocol and Vendor definitions reference Scheme.Conformance only for behavior actually implemented by the matching
 * formal protocol. This root does not dispatch operations, register modules, host Vendor behavior, generalize protocol
 * responses, or classify JWT, JOSE, DPoP, SSF events, and audit data as standalone authentication protocols.
 * </p>
 * <p>
 * A conformance declaration is a closed capability claim, not documentation decoration. It must identify the exact
 * standard sections supported by the corresponding implementation and must never advertise optional endpoints, grants,
 * bindings, algorithms, or response types that are incomplete or unverified.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol;
