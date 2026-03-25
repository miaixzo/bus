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
package org.miaixz.bus.proxy.invoker;

import org.miaixz.bus.proxy.Aspect;

import java.io.Serial;
import java.io.Serializable;

/**
 * A simple base class for interceptors, holding a reference to the target object and the aspect to be applied.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Interceptor implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852259600626L;

    /** The original object being proxied. */
    protected final Object target;
    /** The aspect containing the advice logic. */
    protected final Aspect aspect;

    /**
     * Constructs a new Interceptor.
     *
     * @param target The object to be proxied.
     * @param aspect The aspect implementation.
     */
    public Interceptor(final Object target, final Aspect aspect) {
        this.target = target;
        this.aspect = aspect;
    }

    /**
     * Gets the original target object that is being proxied.
     *
     * @return The target object.
     */
    public Object getTarget() {
        return this.target;
    }

}
