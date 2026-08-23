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
 * Implements the LDAP version 3 client and generic Source direction.
 * <p>
 * {@link org.miaixz.bus.auth.source.protocol.ldap.client.LdapClient} owns one bounded LDAP connection lifecycle and
 * correlates encoded requests with response messages. LdapIdentityParser performs the configured bind and search
 * sequence and maps one verified directory entry to an external identity. LdapClientScheme and LdapClientOptions
 * declare the exact host, port, transport security, bind identity, search base, filter, attributes, subject key, and
 * Source capability.
 * </p>
 * <p>
 * This package consumes LDAP models and codecs, Fabric transport and TLS policy, resolvers, SecretLease, Policies, and
 * identity mapping contracts. It does not host an LDAP server, implement directory persistence, use JNDI, discover
 * arbitrary schemas, follow referrals outside policy, invoke Roster directly, select a Vendor, or expose directory
 * credentials and entries as framework wire values.
 * </p>
 * <p>
 * Each operation binds the configured endpoint, TLS mode, connection identity, message ID, DN, search base, filter,
 * requested attributes, size and time limits, and one Timeout. StartTLS must complete before credentials are sent and
 * the same trusted connection identifier records the TCP-to-TLS transition. Responses must match the request message ID
 * and operation; referrals, SASL mechanisms, controls, and extensions are accepted only when configured. Bind
 * credentials, assertion values, entries, and returned attributes remain operation-scoped and never enter logs or
 * failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol.ldap.client;
