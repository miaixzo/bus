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
 * Defines the framework boundary for third-party authentication platforms.
 * <p>
 * {@link org.miaixz.bus.auth.source.vendor.Vendor} owns stable platform and variant identifiers plus presentation
 * metadata. {@link org.miaixz.bus.auth.source.vendor.VendorManifest} describes exactly one platform; its nested
 * {@link org.miaixz.bus.auth.source.vendor.VendorManifest.Variant} owns the protocol, default scopes, official targets,
 * implemented capabilities, and documented deviations of one supported variant. Framework-owned fixed and constrained
 * target templates remain in {@link org.miaixz.bus.auth.source.vendor.VendorTargets}; project-supplied deployment
 * values and external credential references remain in {@link org.miaixz.bus.auth.source.vendor.VendorOptions}.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.source.vendor.VendorConnector} keeps declarations in each platform package and binds its
 * manifest, Options factory, and exact per-variant adapter factories through
 * {@link org.miaixz.bus.auth.source.vendor.VendorRegistry}. Standard and external connectors use the single root
 * {@link org.miaixz.bus.auth.source.SourceConnector} SPI and the same atomic build-time registration path.
 * {@link org.miaixz.bus.auth.source.vendor.VendorBinding} is the immutable programmatic binding form, while
 * {@link org.miaixz.bus.auth.source.vendor.VendorModule} freezes the resulting bindings into one immutable module and
 * aggregate driver. {@link org.miaixz.bus.auth.source.vendor.VendorLocator} provides read-only manifest lookup, and the
 * aggregate Vendor driver creates the selected {@link org.miaixz.bus.auth.source.vendor.VendorAdapter}. Manifests
 * describe, options carry deployment input, connectors register, bindings retain factories, the module assembles, the
 * locator indexes, the driver compiles, and adapters execute.
 * </p>
 * <p>
 * The root {@link Realm} type is the optional Source-neutral contract implemented only by declared Realm-capable
 * Variants. It models read-only description, snapshot, optional change-feed, and stable-key retrieval results; it
 * neither indexes Manifests nor implies that every Vendor supports Realm access.
 * {@link org.miaixz.bus.auth.source.vendor.VendorLocator} indexes immutable Manifest declarations and must not be used
 * as a Realm data API. Each Realm adapter continues to implement
 * {@link org.miaixz.bus.auth.source.vendor.VendorAdapter} directly. External projects select a compiled Source and
 * invoke its declared shared Realm capability through {@link org.miaixz.bus.auth.Dispatcher}; synchronization
 * scheduling, persistence, reconciliation, and business models remain outside bus-auth.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.source.vendor.RedirectManager} orchestrates the platform redirect flow. Its
 * package-private correlation collaborator owns only atomic state, nonce, and PKCE persistence and rollback, keeping
 * cache mechanics out of adapters and flow orchestration.
 * </p>
 * <p>
 * Vendor is a Source specialization, not a replacement authentication protocol. Standards-conforming operations
 * delegate to their protocol packages; a platform package owns only documented extensions or proprietary flows. This
 * package contains no mutable global registration, project data loader, persistence, controller, user or account
 * binding model, business Session, or generic replacement token/profile/callback DTO. Secrets remain external
 * references until execution-scoped resolution.
 * </p>
 * <p>
 * The direct subdirectories of this package are reserved exclusively for named third-party platforms. Cross-platform
 * contracts and package-private implementations remain in this root package; every platform keeps its manifest,
 * options, registration connector, and executable adapter together in one platform package.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor;

import org.miaixz.bus.auth.Realm;
