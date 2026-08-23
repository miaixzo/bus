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
 * {@link org.miaixz.bus.auth.Scheme}, {@link org.miaixz.bus.auth.Scheme.Options},
 * {@link org.miaixz.bus.auth.source.SourceDriver}, {@link org.miaixz.bus.auth.registry.SourceValidator}, and
 * {@link org.miaixz.bus.auth.runtime.RuntimeDescriptor} define the typed compilation and supported-implementation
 * boundary shared by client-role and server-role protocol Sources. {@link org.miaixz.bus.auth.source.SourceWorkflow}
 * supplies the redirect, device, or direct authentication capabilities; its nested Request and Stage contracts converge
 * every successfully verified platform account directly on {@link Identity}. Each protocol or Vendor implementation
 * owns its private mapping code and cannot use this package as an account-linking layer.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.source.SourceConnector} is the single sealed SPI boundary for protocol and Vendor
 * registrations. {@link org.miaixz.bus.auth.source.SourceDiscovery} is the sole discovery boundary, and
 * {@link org.miaixz.bus.auth.source.SourceSuite} uses visitor dispatch to assemble the two exact registry families
 * without runtime type inspection. Its frozen {@link org.miaixz.bus.auth.source.SourceAggregate} retains the exact
 * {@link org.miaixz.bus.auth.source.protocol.ProtocolModule} and
 * {@link org.miaixz.bus.auth.source.vendor.VendorModule}. {@link org.miaixz.bus.auth.source.protocol.ProtocolConnector}
 * groups every role-specific driver owned by one protocol, while
 * {@link org.miaixz.bus.auth.source.protocol.ProtocolRegistry} applies that group atomically before runtime assembly.
 * </p>
 * <p>
 * Protocol clients, protocol servers, and Vendor adapters depend on these contracts. RuntimeDescriptor exposes only
 * immutable SourceDescriptor values contributed by ProtocolModule and VendorModule; it never exposes a concrete
 * platform adapter, protocol implementation, token model, UserInfo model, wire codec, or factory. VendorLocator retains
 * read-only Vendor manifest lookup, while VendorModule exposes the aggregate Vendor Source driver and its exact
 * management descriptors for runtime assembly.
 * </p>
 * <p>
 * This package owns driver selection, one-time preparation, dependency declarations, and the Source-scoped service
 * contract. The runtime package owns enforcement of each scoped service view, while the worker package owns session
 * coordination. The compiled executable remains {@link org.miaixz.bus.auth.worker.SourceWorker} in {@code worker};
 * moving it here would merge the compilation contract with its execution result and invert the project-port boundary.
 * </p>
 * <p>
 * {@code Scheme} declares immutable authentication metadata, while {@code Scheme.Options} carries typed deployment
 * input and alone declares its exact implementation type. The integrating project materializes Scheme.Options before
 * loading a Source; {@code SourceDriver} only validates the matching concrete value and compiles it. Source
 * authentication represents a completed account-verification flow, not a substitute OAuth or proprietary protocol. Only
 * a stable identifier verified under the selected Source may become the external subject; access tokens, authorization
 * codes, session keys, client secrets, unverified email addresses, and display names must never be used as fallback
 * subjects or exposed in attributes and failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source;

import org.miaixz.bus.auth.Identity;
