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
 * Defines management and compilation contracts for protocol and Vendor Sources.
 * <p>
 * {@link org.miaixz.bus.auth.Scheme}, {@link org.miaixz.bus.auth.Options},
 * {@link org.miaixz.bus.auth.source.SourceDriver}, and {@link org.miaixz.bus.auth.source.SourceValidator} define the
 * typed complete-registration compilation boundary shared by client-role and server-role protocol Sources.
 * {@link org.miaixz.bus.auth.source.SourceAuthentication} supplies the single redirect or direct sign-in capability;
 * its request, initiation, and result types converge a successfully verified platform account on
 * {@link org.miaixz.bus.auth.source.ExternalIdentity}. An {@link org.miaixz.bus.auth.source.IdentityMapper} then maps
 * that evidence-bearing identity without changing its verified subject.
 * </p>
 * <p>
 * Protocol clients, protocol servers, and Vendor adapters depend on these contracts. This package depends only on the
 * root domain language and does not import a concrete Vendor, protocol implementation, token model, UserInfo model, or
 * wire codec. Vendor definitions remain in VendorDirectory while VendorModule exposes their single aggregate Source
 * driver for runtime assembly.
 * </p>
 * <p>
 * {@code Scheme} declares immutable authentication metadata, while {@code Options} carries typed deployment input and
 * alone declares its exact implementation type. The integrating project materializes Options before loading a Source;
 * {@code SourceDriver} only validates the matching concrete value and compiles it. Source authentication represents a
 * completed account-verification flow, not a substitute OAuth or proprietary protocol. Only a stable identifier
 * verified under the selected Source may become the external subject; access tokens, authorization codes, session keys,
 * client secrets, unverified email addresses, and display names must never be used as fallback subjects or exposed in
 * attributes and failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source;
