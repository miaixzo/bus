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
 * Implements the LDAP version 3 server and Provider direction.
 * <p>
 * Bind, Search, Modify, Add, Delete, Modify DN, Compare, Abandon, Unbind, and Extended operation services implement the
 * corresponding RFC 4511 semantics. LdapErrorMapper produces the operation-specific LDAPResult, while LdapServerScheme
 * and LdapServerOptions declare supported authentication choices, controls, extensions, limits, and StartTLS behavior.
 * DirectoryStore is the external project's directory and transaction port.
 * </p>
 * <p>
 * Services consume typed LDAP messages, authenticated connection Context, SecurityBaseline, codecs, and DirectoryStore.
 * The framework owns protocol validation and response sequencing; the external project owns directory persistence,
 * credential verification, authorization, schema data, and deployment configuration. This package has no Controller,
 * Source/client behavior, JNDI backend, Vendor integration, direct Registry lookup, or HTTP/JSON response abstraction.
 * </p>
 * <p>
 * Every PDU is bound to one trusted connection ID, current TCP or TLS transport, message ID, operation, controls, size
 * and time limits, and Budget. Bind replaces connection authentication state only after success; StartTLS rejects
 * pipelined or already protected transitions; request message IDs cannot be reused while active. Search streams
 * entries, references, intermediate responses, and one final result in order. Unbind and Abandon deliberately emit no
 * response. Credentials, directory values, controls, and diagnostic causes never enter logs or unrelated responses.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.ldap.server;
