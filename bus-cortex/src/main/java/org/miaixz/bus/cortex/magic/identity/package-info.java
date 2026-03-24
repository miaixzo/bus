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
 * Distributed identity generation utilities for Cortex-managed resources.
 * <p>
 * {@code IdGenerator} is a pluggable string ID generator: the no-arg constructor uses {@code ID.objectId()} (MongoDB
 * ObjectId format) as the default strategy, and a second constructor accepts any {@code Supplier<String>} to substitute
 * an alternative strategy such as Snowflake, UUID or NanoId at deployment time. {@code Sequence} is a named monotonic
 * counter backed by CacheX: each call to {@code next(key)} atomically increments the counter stored under
 * {@code SEQUENCE_PREFIX + key} and returns the new value. {@code Fingerprint} is a stateless utility that derives a
 * stable 32-character lowercase hex identifier from a {@code host:port} string via SHA-256, used to uniquely identify a
 * runtime service instance without exposing its network coordinates directly.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.magic.identity;
