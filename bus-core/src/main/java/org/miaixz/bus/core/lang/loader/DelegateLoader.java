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
package org.miaixz.bus.core.lang.loader;

/**
 * An abstract base class for resource loaders that delegate their loading operations to another {@link Loader}
 * instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class DelegateLoader extends ResourceLoader implements Loader {

    /**
     * The delegate loader to which resource loading operations are forwarded.
     */
    protected final Loader delegate;

    /**
     * Constructs a new {@code DelegateLoader} with the specified delegate.
     *
     * @param delegate The {@link Loader} instance to delegate to. Must not be {@code null}.
     * @throws IllegalArgumentException If the provided delegate is {@code null}.
     */
    protected DelegateLoader(Loader delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
    }

}
