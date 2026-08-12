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
 * Implements the internal LDAP v3 protocol engine behind the exported {@link org.miaixz.bus.auth.metric.LDAP}
 * contracts. Message, filter, control, and BER packages contain bounded wire models and codecs. The client owns one
 * exclusive {@link org.miaixz.bus.auth.metric.AuthMetric.StreamSession}; the server owns one
 * {@link org.miaixz.bus.auth.metric.AuthMetric.StreamServerBinding} and its managed sessions. Both use only stream
 * ports supplied by {@link org.miaixz.bus.auth.metric.AuthMetric.Runtime}; direct sockets, JNDI, global TLS state,
 * background threads, referral chasing, SASL, and persistent directory storage are outside this package.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.ldap;
