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
 * Built-in optional Cortex capabilities that can be used out of the box by applications.
 * <p>
 * {@code builtin.batch} provides bulk register/deregister operations, {@code builtin.event} provides ready-to-use watch
 * listeners and setting publishers, and {@code builtin.graph} provides upstream/downstream dependency analysis. Shared
 * runtime infrastructure belongs under {@code magic}; this package should stay focused on optional assembled features.
 * During the current structure-freeze period, the root-level metadata helpers {@code Label}, {@code LabelMapper},
 * {@code Selector}, and {@code MetadataMatcher} remain here as historical compatibility helpers. Root, setting, and
 * watch code may depend only on those helpers; optional batch/event/graph implementations stay outside the foundation
 * path.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.builtin;
