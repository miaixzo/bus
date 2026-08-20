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
 * Defines third-party Vendor definitions, directory metadata, fixed endpoints, settings, and registered deviations.
 * <p>
 * {@link org.miaixz.bus.auth.vendor.Vendor} owns stable platform and variant identifiers and display metadata.
 * VendorDefinition declares each variant's actual OAuth 1.0, OAuth 2.0, OpenID Connect, or VENDOR_AUTH protocol,
 * conformance, endpoints, scope, management form, capability manifest, and deviations. VendorDirectory is the immutable
 * management and Hub view. VendorModule freezes one exact built-in, external, or combined contribution set for both
 * runtime compilation and management display. VendorTargets distinguishes framework-owned fixed or templated platform
 * endpoints from deployment settings, and VendorSettings retains only registration data supplied by the external
 * project.
 * </p>
 * <p>
 * Vendor is a Source specialization, not an authentication protocol. Standards-conforming operations delegate to the
 * matching protocol client; a platform package owns only documented extensions or proprietary flows. VendorModule,
 * VendorDriver, VendorAdapter, VendorAdapter.Factory, and RedirectManager form the explicit external platform driver
 * boundary. Runtime assembly obtains every adapter through Registry compilation. This package has no mutable global
 * registration, Controller, project data loader, persistence, Provider/server role, or generic token, profile,
 * callback, request, response, and error DTO.
 * </p>
 * <p>
 * Vendor definitions advertise only implemented capabilities and preserve official fixed addresses so users do not
 * re-enter them. Every deviation identifies its affected operation, location, platform field, optional standard field,
 * HTTP method, media type, and envelope. Secrets remain credential references until an operation-scoped SecretLease is
 * opened. Platform token bodies and identity responses remain private to one adapter, and credentials, tokens,
 * callbacks, raw profiles, or private DTOs never enter directories, registrations, diagnostics, or protocol wire.
 * </p>
 * <p>
 * The direct subdirectories of this package are reserved exclusively for named third-party platforms. Cross-platform
 * contracts and package-private implementations remain in this root package; every platform keeps its executable
 * adapter in its own non-exported internal subpackage.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor;
