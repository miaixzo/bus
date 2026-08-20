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
 * Assembles the non-exported OAuth 1.0 Source driver.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oauth1.internal.OAuth1SourceDriver} binds the generic OAuth 1.0 Source profile to
 * exact registration validation, settings decoding, and standard client-side runtime construction.
 * </p>
 * <p>
 * RuntimeBuilder receives the driver through the public OAuth1 facade. This package is absent from JPMS exports and
 * performs no project data loading, reflection, ServiceLoader lookup, Registry invocation, persistence, Vendor
 * selection, or protocol call during driver construction.
 * </p>
 * <p>
 * Compilation fails closed on Source type, OAuth 1.0 protocol, namespace, settings, endpoint, signature method,
 * credential reference, and manifest mismatch. No partially compiled provider, plaintext secret, or registration body
 * escapes the compiler boundary.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth1.internal;
