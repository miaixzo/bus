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
 * Compiles and assembles non-exported SCIM 2.0 server-role Source runtime components.
 * <p>
 * ScimProviderDriver binds the SCIM Provider profile to exact registration validation and its declared resource,
 * discovery, patch, bulk, filter, and error capabilities to formal services and the externally supplied
 * ScimResourceStore.
 * </p>
 * <p>
 * RuntimeBuilder receives this driver through the public Scim facade. This package may depend on SCIM models, codecs,
 * server services, profiles, SecurityBaseline, runtime contributions, and externally supplied stores. It does not
 * expose a public protocol operation, load project data, discover implementations through reflection or ServiceLoader,
 * retain mutable global state, create a client/Source role, call Vendor adapters, or implement persistence and
 * permissions.
 * </p>
 * <p>
 * Compilation fails closed when direction, protocol, namespace, base URI, resource type, schema, authentication scheme,
 * feature limits, store binding, conformance, settings, or manifest is inconsistent. Only a complete immutable
 * candidate is published. Runtime operations use one Context and Budget, keep mutations atomic through the store port,
 * close writeOnly SecretLease values on every completion path, and prevent resource content, credentials, filters, and
 * mutation payloads from entering diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.scim.internal;
