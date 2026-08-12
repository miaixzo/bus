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
 * Provides internal authentication-protocol capabilities shared by two or more protocol implementations.
 *
 * <p>
 * A capability enters this package only when its semantics are identical across at least two protocols and no owning
 * Bus component already provides it. Protocol wire formats, registered values, state machines, and protocol-specific
 * validation remain in their protocol packages. General-purpose networking, caching, cryptography, JSON, and utility
 * facilities remain in their owning Bus components and are consumed directly.
 * </p>
 *
 * <p>
 * Types in this package depend on {@link org.miaixz.bus.auth.metric.AuthMetric} contracts and approved Bus APIs.
 * Protocol packages may depend on this package; this package never depends on a protocol package. It does not expose a
 * parallel public facade, own threads or resources, register global state, or select product configuration.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.shared;
