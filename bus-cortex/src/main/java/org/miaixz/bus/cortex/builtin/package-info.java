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
 * Label-based asset annotation and metadata-driven routing for filtering registered service assets by arbitrary
 * key/value criteria.
 * <p>
 * Assets carry a list of {@code Label} objects, each holding a key, a value and an optional category field, used to
 * annotate entries for capability discovery, environment targeting and traffic routing. A {@code Selector} expression
 * matches label values using one of four operators — EQ (exact equals), NEQ (not equals), IN (value present in a set)
 * or NOTIN (value absent from a set) — and static factory methods {@code eq(key, value)} and {@code in(key, values)}
 * cover the common cases without constructing the enum directly. {@code MetadataRouter} evaluates a list of
 * {@code Selector} expressions against an asset's labels map using logical AND; its {@code filter()} method narrows a
 * candidate asset list to those satisfying every selector, enabling tag-based blue/green and canary routing decisions.
 * <p>
 * Sub-packages extend these primitives: {@code builtin.batch} for bulk register/deregister operations,
 * {@code builtin.event} for ready-to-use watch listeners and config publishers, and {@code builtin.graph} for
 * upstream/downstream dependency analysis.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.builtin;
