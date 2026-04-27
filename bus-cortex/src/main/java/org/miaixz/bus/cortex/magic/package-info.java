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
 * Cross-cutting infrastructure shared by Cortex packages: structured audit logging, distributed identity generation,
 * watch dispatch, reliable change-log records, and instance lifecycle state tracking.
 * <p>
 * {@code AuditLogger} writes JSON audit events with optional detail payloads to CacheX under time-suffixed audit keys
 * and retains them for seven days. The {@code magic.identity} subpackage provides {@code CortexIdentity},
 * {@code IdGenerator}, {@code Sequence}, and {@code Fingerprint} for shared identity normalization, IDs, ordered
 * counters, and stable instance fingerprints. The {@code magic.watch} subpackage owns watch subscription lifecycle,
 * dispatch ordering, async fan-out, namespace limits, and per-watch backlog handling. The {@code magic.event}
 * subpackage owns the first-stage Cortex outbox abstractions and cache fallback store. The {@code magic.state}
 * subpackage provides {@code InstanceState} (UP, DOWN, UNKNOWN, STARTING, and MAINTENANCE) plus mutable
 * {@code InstanceStateHistory} records that capture observed state transitions with timestamps and diagnostic metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.magic;
