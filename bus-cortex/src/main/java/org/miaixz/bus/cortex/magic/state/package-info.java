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
 * Instance lifecycle state model for tracking and recording health transitions of registered service instances.
 * <p>
 * {@code InstanceState} is an enum with four phases: UP (running and healthy), DOWN (unreachable or failing health
 * checks), UNKNOWN (status not yet determined, typically at startup before the first probe completes) and STARTING
 * (process launched but not yet ready to serve traffic). {@code InstanceStateHistory} is an immutable value object
 * recording a single state transition: the resulting {@code InstanceState}, the Unix-epoch-millisecond timestamp when
 * the transition was observed, a human-readable reason string explaining why the state changed, and the namespace of
 * the affected instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.magic.state;
