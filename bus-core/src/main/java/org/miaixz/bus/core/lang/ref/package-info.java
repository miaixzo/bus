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
 * Provides utility classes for working with Java's reference objects, primarily encapsulating functionality related to
 * {@link java.lang.ref.Reference}. The main encapsulated references include:
 *
 * <pre>
 * 1. {@link java.lang.ref.SoftReference}: A soft reference is garbage collected when the JVM reports low memory.
 * 2. {@link java.lang.ref.WeakReference}: A weak reference is garbage collected when it is discovered during a GC cycle.
 * 3. {@link java.lang.ref.PhantomReference}: When a phantom reference is discovered during a GC cycle, the {@link java.lang.ref.PhantomReference} object is enqueued on its {@link java.lang.ref.ReferenceQueue}. The referenced object is not yet reclaimed. It will be reclaimed only after the {@link java.lang.ref.ReferenceQueue} is processed.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.core.lang.ref;
