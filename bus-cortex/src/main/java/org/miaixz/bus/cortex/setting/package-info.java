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
 * Umbrella package for the Cortex setting domain.
 * <p>
 * The root package acts as the umbrella of the setting domain rather than the final home of every resource model.
 * {@link org.miaixz.bus.cortex.Setting} is the shared base for namespace, app, profile, current-state item, and
 * item-revision resources. {@code Type} identifies these setting-owned resource kinds without routing them through
 * registry dispatch. Namespace is the root resource of the setting domain. Applications and profiles are
 * namespace-scoped directory resources, and current-state content is modeled as {@code setting.item}. The canonical
 * relationship is {@code namespace -> app -> item} plus {@code namespace -> profile}. App/profile bindings for one item
 * are stored in relationship tables and aggregated into runtime-only fields rather than being encoded in one scalar
 * column on the item itself. The item resource therefore owns current-state setting content, lookup scope, query
 * objects, cache keys, normalization policy, request context, gray-routing helpers, revision helpers, and the
 * lightweight watcher instead of continuing as scattered root-level setting models. ItemRevision history belongs to the
 * item resource as {@code setting.item.revision} rather than standing as one more top-level sibling resource. The
 * {@code curator} subpackage handles current-state persistence, revision history, source adapters, and effective-value
 * resolution. The {@code delivery} subpackage adds export and runtime overlay services, while {@code secret} contains
 * secret codecs and masking helpers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.setting;
