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
package org.miaixz.bus.proxy;

import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * An interface for a dynamic proxy provider. Implementations of this interface define the mechanism for creating proxy
 * objects, such as using JDK proxies or CGLIB.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Provider {

    /**
     * Creates a proxy object for the given target, applying the specified aspect.
     *
     * @param <T>    The type of the target object.
     * @param target The object to be proxied.
     * @param aspect The aspect implementation containing the advice logic.
     * @return The proxied object.
     */
    <T> T proxy(T target, Aspect aspect);

    /**
     * Creates a proxy object for the given target, automatically instantiating the aspect class.
     *
     * @param <T>         The type of the target object.
     * @param target      The object to be proxied.
     * @param aspectClass The class of the aspect to be instantiated and applied.
     * @return The proxied object.
     */
    default <T> T proxy(final T target, final Class<? extends Aspect> aspectClass) {
        return proxy(target, ReflectKit.newInstanceIfPossible(aspectClass));
    }

}
