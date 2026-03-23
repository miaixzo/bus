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
 * Access control and traffic protection guards for Cortex-managed services.
 * <p>
 * {@code AccessGuard} enforces RBAC-based permissions backed by CacheX: it checks whether a given role holds the
 * required permission on a resource/action pair, and allows admin-token holders to grant new permissions at runtime.
 * {@code NamespaceGuard} enforces namespace isolation by blocking callers from reaching any namespace that has not been
 * explicitly granted to their role. {@code CircuitBreaker} implements a CLOSED/OPEN/HALF_OPEN state machine that stops
 * cascading failures: the breaker opens after a configurable consecutive-failure threshold is exceeded, and probes
 * recovery after a configurable reset timeout. {@code RateLimiter} applies a token-bucket cap on request throughput per
 * key, rejecting calls that exceed the configured rate. {@code ParamValidator} checks that required parameters are
 * present and that format-constrained values satisfy their constraints, throwing on the first violation found.
 * <p>
 * Access token issuance, validation and header resolution are handled in the {@code guard.token} sub-package.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.guard;
