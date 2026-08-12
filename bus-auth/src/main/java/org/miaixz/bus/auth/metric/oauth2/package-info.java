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
 * Implements the internal OAuth 2.0 protocol engine behind the public {@link org.miaixz.bus.auth.metric.OAuth2} facade.
 * <p>
 * Code in this package accepts only immutable facade contracts and uses ports supplied by
 * {@link org.miaixz.bus.auth.metric.AuthMetric.Runtime}. JWT-secured authorization requests and responses reuse the
 * public JWT contracts; compact parsing and signing remain owned by the JWT package. Bounded JSON, replay keys, atomic
 * state operations, URI validation, and security-clock validation reuse the narrowly scoped metric shared packages.
 * Dependencies point from this package to the root facade, JWT, and shared facilities and never in the opposite
 * direction.
 * </p>
 * <p>
 * This package is a protocol engine rather than a Web framework. It neither starts an HTTP server nor reads servlet,
 * framework, environment, file-system, or global configuration state. Product adapters translate inbound HTTP data into
 * facade requests, translate outcomes into standard OAuth wire responses, and own all runtime and transport lifecycles.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.oauth2;
