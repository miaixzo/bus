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
 * Defines the LDAP version 3 message, protocol operation, control, result, name, attribute, and filter models.
 * <p>
 * LdapMessage preserves the RFC 4511 message ID, protocolOp APPLICATION choice, and controls. The package provides the
 * complete Bind, Unbind, Search, Modify, Add, Delete, Modify DN, Compare, Abandon, Extended, and Intermediate request
 * or response types, together with DistinguishedName, LdapAttribute, authentication choices, filters, referrals, and
 * extensible result codes. Unknown non-critical extensions retain their registered binary values.
 * </p>
 * <p>
 * LDAP client, server, codec, and protocol-driver classes consume these immutable values. This package does not
 * implement a network connection, directory storage, authentication backend, Registry lookup, project permissions,
 * identity linking, Vendor behavior, a generic operation envelope, or an HTTP/JSON representation. LDAP model values
 * are never replaced with JNDI objects or framework request and response types.
 * </p>
 * <p>
 * Models preserve ASN.1 tags, OCTET STRING bytes, message IDs, result codes, control criticality, DN and filter syntax,
 * size and time limits, and the no-response semantics of Unbind and Abandon. Passwords and SASL credentials remain
 * closeable operation-scoped material. Collections, nesting, lengths, attribute values, filters, referrals, controls,
 * and diagnostic text are bounded, and credentials or directory values never enter textual diagnostics or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.ldap;
