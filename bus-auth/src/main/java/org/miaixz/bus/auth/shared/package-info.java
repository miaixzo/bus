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
 * Hosts security capabilities shared by multiple authentication protocols without defining another protocol.
 * <p>
 * The root exports {@link org.miaixz.bus.auth.shared.SecurityBaseline}, an immutable selection of algorithm, time,
 * replay, address, and protocol-specific limits supplied through {@link org.miaixz.bus.auth.runtime.RuntimeServices}.
 * Subpackages implement reusable audit, JOSE, JWT, PKCE, DPoP, claim, and consent building blocks that formal protocol
 * services compose under their own standard contracts.
 * </p>
 * <p>
 * Dependencies flow from protocol and Vendor implementations into the narrow shared capability they need, then into
 * bus-core, bus-crypto, bus-cache ports, and Fabric security primitives. Shared code does not dispatch protocols,
 * publish Provider or Source schemes, access Registry, select a Vendor, or introduce SSF, JWT, JOSE, or a security
 * event as a top-level authentication protocol.
 * </p>
 * <p>
 * Every operation receives explicit Context, Budget, keys or leases, and a frozen baseline. Shared services reject
 * algorithm confusion, unbounded input, replay, unsafe addresses, and ambiguous claims; they do not use global crypto
 * state or log tokens, keys, assertions, signatures, personal data, or complete payloads.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared;
