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
 * {@link org.miaixz.bus.auth.vendor.Vendor} owns stable platform and variant identifiers plus presentation metadata.
 * {@link org.miaixz.bus.auth.vendor.VariantManifest} describes exactly one platform; its nested
 * {@link org.miaixz.bus.auth.vendor.VariantManifest.Variant} owns the protocol, default scopes, official targets,
 * implemented capabilities, and documented deviations of one supported variant. Framework-owned fixed and constrained
 * target templates remain in {@link org.miaixz.bus.auth.vendor.VendorTargets}; project-supplied deployment values and
 * external credential references remain in {@link org.miaixz.bus.auth.vendor.VendorOptions}.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.vendor.VendorDriver} binds one manifest to exact per-variant factories.
 * {@link org.miaixz.bus.auth.vendor.VendorModule} freezes built-in and externally contributed drivers into one
 * immutable inventory. {@link org.miaixz.bus.auth.vendor.VendorDirectory} provides read-only manifest lookup, while the
 * Source compiler validates project options against that inventory and creates the selected
 * {@link org.miaixz.bus.auth.vendor.VendorAdapter}. These classes do not exchange responsibilities: manifests describe,
 * options carry deployment input, drivers bind factories, the module assembles, the directory indexes, and adapters
 * execute.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.vendor.RedirectManager} orchestrates the platform redirect flow. Its package-private
 * correlation collaborator owns only atomic state, nonce, and PKCE persistence and rollback, keeping cache mechanics
 * out of adapters and flow orchestration.
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
 * options, and executable adapter together in one platform package.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor;
