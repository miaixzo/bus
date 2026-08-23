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
 * Defines the RADIUS Access, Accounting, EAP-Message, attribute, and packet models.
 * <p>
 * RadiusPacket is the common contract for Access-Request, Access-Accept, Access-Reject, Access-Challenge,
 * Accounting-Request, and Accounting-Response. LegacyHeader preserves the one-octet Identifier and sixteen-octet
 * Authenticator of RFC 2865 and RFC 2866; Radius11Header preserves the four-octet opaque Token of RFC 9765. RadiusCode,
 * RadiusAttribute, VendorSpecificAttribute, and EapMessage retain unsigned wire ranges and raw octet values.
 * </p>
 * <p>
 * RADIUS server, codec, and protocol-driver classes consume these immutable packet values. The transport boundary
 * determines RADIUS 1.0 or RADIUS/1.1 before decoding. This package does not implement network I/O, client lookup,
 * shared-secret resolution, accounting storage, Roster access, a client/Source role, Vendor authentication, Dynamic
 * Authorization, or HTTP/JSON/framework envelopes.
 * </p>
 * <p>
 * Packet models preserve Code, Length-covered content, attribute order, unknown attributes, legacy Identifier and
 * Authenticator semantics, and RADIUS/1.1 Token correlation. Legacy security and RADIUS/1.1 are mutually exclusive: RFC
 * 9765 never carries a shared secret, MD5 authenticator, or Message-Authenticator. Attribute and packet sizes are
 * bounded, byte arrays are detached, and authenticators, tokens, EAP data, vendor values, and credentials never appear
 * in textual diagnostics or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol.radius;
