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
 * Encodes and decodes LDAP version 3 BER frames and protocol messages.
 * <p>
 * BerCodec frames definite-length Basic Encoding Rules values for a connection. LdapMessageEncoder maps every supported
 * protocolOp and control to its RFC 4511 universal, application, context-specific, primitive, or constructed tag;
 * LdapMessageDecoder performs the inverse mapping while preserving OCTET STRING bytes and extensible result values.
 * </p>
 * <p>
 * LDAP clients and servers call this package at the transport boundary. Codecs consume only typed LDAP models and
 * bounded byte primitives. They do not open sockets, manage connection authentication, access directory storage,
 * resolve credentials, follow referrals, apply policy, invoke Roster, create identity, interpret Vendor data, or
 * introduce an HTTP, JSON, or framework envelope.
 * </p>
 * <p>
 * Decoding rejects indefinite lengths, non-minimal or overflowing lengths and integers, truncated values, unexpected
 * tags, constructed/primitive confusion, duplicate singleton fields, invalid operation choices, trailing bytes, and
 * configured frame, depth, collection, attribute, control, filter, and text limit violations. Unbind and Abandon retain
 * their no-response wire semantics. Credential and attribute OCTET STRING arrays are defensively isolated, closed or
 * cleared at their owner boundary, and never rendered in errors or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol.ldap.codec;
