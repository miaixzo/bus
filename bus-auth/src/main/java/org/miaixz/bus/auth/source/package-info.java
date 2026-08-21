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
 * {@link org.miaixz.bus.auth.source.SourceDriver}, {@link org.miaixz.bus.auth.registry.SourceValidator}, and
 * {@link org.miaixz.bus.auth.runtime.RuntimeDescriptor} define the typed compilation and supported-implementation
 * boundary shared by client-role and server-role protocol Sources.
 * {@link org.miaixz.bus.auth.source.SourceAuthentication}
 * supplies the redirect, device, or direct authentication capabilities; its nested Request and Stage contracts
 * converge every successfully verified platform account directly on
 * {@link org.miaixz.bus.auth.source.ExternalIdentity}. Each protocol or Vendor implementation owns its private mapping
 * code and cannot use this package as an account-linking layer.
 * </p>
 * <p>
 * Protocol clients, protocol servers, and Vendor adapters depend on these contracts. RuntimeDescriptor exposes only
 * public immutable Vendor manifest contracts and never imports a concrete platform implementation, protocol
 * implementation, token model, UserInfo model, or wire codec. Vendor definitions remain in VendorDirectory while
 * VendorModule exposes their single aggregate Source driver for runtime assembly.
 * </p>
 * <p>
 * This package owns driver selection, one-time preparation, dependency declarations, the Driver-visible service
 * contract, and session coordination. The runtime package owns enforcement of each scoped service view. The compiled
 * executable remains {@link org.miaixz.bus.auth.worker.SourceWorker} in {@code worker}; moving it here would merge the
 * compilation contract with its execution result and invert the project-port boundary.
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
