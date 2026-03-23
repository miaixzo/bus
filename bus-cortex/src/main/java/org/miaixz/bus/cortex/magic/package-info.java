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
 * Cross-cutting infrastructure shared by all Cortex sub-packages: structured audit logging, distributed identity
 * generation and instance lifecycle state tracking.
 * <p>
 * {@code AuditLogger} records structured audit events backed by CacheX. Each call to
 * {@code log(namespace, operation, id, operator)} serialises the event fields — operation, entity ID, operator and
 * timestamp — as a JSON string and writes it under a namespaced key ({@code audit:namespace:operation:id}) with a 7-day
 * TTL retention window.
 * <p>
 * The {@code magic.identity} sub-package provides {@code IdGenerator} (defaults to {@code ID.objectId()}, extensible
 * via any {@code Supplier<String>}), {@code Sequence} (named CacheX-backed atomic counters) and {@code Fingerprint}
 * (SHA-256 hex identifier derived from host and port). The {@code magic.state} sub-package provides
 * {@code InstanceState} (the UP/DOWN/UNKNOWN/STARTING enum) and {@code InstanceStateHistory} (immutable
 * state-transition snapshot with timestamp and reason).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.magic;
