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
 * API service definitions and direct shared-process registration.
 * <p>
 * {@code ApiAssets} supplies exact service identity, method and service version. Registration passes this existing
 * definition together with {@code org.miaixz.bus.cortex.Instance}; no second instance model or relationship wrapper is
 * required. The process retains its actual tenant, program version, application ownership and authenticated session.
 * <p>
 * {@code register(service, instance)} persists the service association using the existing host store context.
 * {@code queryInstances(tenantId, app_id, method, version)} returns direct process snapshots selected through that
 * association. {@code deregisterInstance(tenantId, app_id, method, version, checksum)} removes only the registration.
 * The host owns authoritative process persistence and session maintenance. Cache-only process projections use service
 * coordinates from ApiAssets and the global {@code reg:process:} namespace.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.cortex.registry.api;
