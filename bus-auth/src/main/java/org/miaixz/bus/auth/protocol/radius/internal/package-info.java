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
 * Compiles and assembles non-exported RADIUS server-role Source runtime components.
 * <p>
 * RadiusProviderDriver binds the RADIUS Provider profile to exact registration validation, obtains its externally
 * supplied RadiusRequestHandler binding, and assembles Access and Accounting services with version-specific codecs,
 * RadiusAuthenticator, SecretResolver, SecurityBaseline, and the declared capability manifest.
 * </p>
 * <p>
 * RuntimeBuilder receives this driver through the public Radius facade. This package may depend on typed RADIUS models,
 * codecs, server services, profiles, resolvers, runtime contributions, and the external handler binding. It does not
 * expose public packet operations, load project data, discover implementations through reflection or ServiceLoader,
 * retain mutable global state, implement transport or persistence, create a Source/client role, or call Vendor
 * adapters.
 * </p>
 * <p>
 * Compilation fails closed for mismatched direction, protocol, namespace, version set, packet limit, EAP or Type 80
 * policy, handler binding, secret resolver, conformance, settings, or manifest. Only a complete immutable candidate is
 * published. Legacy and RADIUS/1.1 paths are assembled separately so shared-secret and MD5 operations are unreachable
 * from RFC 9765. SecretLease values are bounded to synchronous wire-security continuations, and packet octets never
 * enter runtime diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.radius.internal;
