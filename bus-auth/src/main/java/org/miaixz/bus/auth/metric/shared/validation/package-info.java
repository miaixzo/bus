/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Provides shared authentication validation for absolute URIs, HTTPS requirements, origins, redirects, transport
 * policies, and bounded protocol time claims. URI validation rejects credentials, fragments, ambiguous authorities,
 * non-normal forms, disallowed schemes, ports, hosts, origins, and redirect transitions before transport I/O. Time
 * validation uses only the injected runtime clock and explicit skew, lifetime, and TTL bounds; it never reads the
 * system clock or silently saturates arithmetic. Protocol claim meaning and wire-error mapping remain in each protocol
 * package.
 * <p>
 * <strong>Bus dependencies:</strong> bus-core {@code Protocol}, {@code Port}, and validation exceptions provide the
 * canonical network vocabulary and contract failures; {@code AuthMetric.TransportPolicy} supplies the closed transport
 * allowlist, while {@code AuthMetric.ClockSource} is the sole current-time source. JDK {@code URI} and {@code Instant}
 * retain parsed value semantics without a duplicate URL or clock abstraction.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.shared.validation;
