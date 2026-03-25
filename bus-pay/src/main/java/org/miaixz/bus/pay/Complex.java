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
package org.miaixz.bus.pay;

import java.util.Arrays;
import java.util.Optional;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.pay.metric.AbstractProvider;

/**
 * A unified interface for the API addresses of payment platforms, providing the following methods: 1)
 * {@link Complex#sandbox()}: Get the sandbox URL. This is not a mandatory interface (some platforms do not support it).
 * 2) {@link Complex#service()}: Get the production URL. This must be implemented. Note: ① If you need to implement
 * third-party authorization through extension, please refer to {@link Registry} to create a corresponding enum class
 * and implement the {@link Complex} interface. ② If you are not using an enum class, you need to handle the assignment
 * of the source field separately.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Complex {

    /**
     * Gets the enum value based on the URL.
     *
     * @param <E>   The enum type.
     * @param clazz The enum class.
     * @param url   The URL.
     * @return The enum value.
     */
    static <E extends Enum<?> & Complex> Optional<E> of(Class<E> clazz, String url) {
        return Arrays.stream(clazz.getEnumConstants()).filter(e -> e.method().equals(url)).findFirst();
    }

    /**
     * Checks if it is a sandbox environment.
     *
     * @return The string.
     */
    default boolean isSandbox() {
        return false;
    }

    /**
     * The sandbox environment.
     *
     * @return The string.
     */
    default String sandbox() {
        return Protocol.HOST_IPV4;
    }

    /**
     * The production environment.
     *
     * @return The string.
     */
    default String service() {
        return Protocol.HOST_IPV4;
    }

    /**
     * Gets the interface/method.
     *
     * @return The string.
     */
    default String method() {
        return HTTP.NONE;
    }

    /**
     * The Provider implementation class corresponding to the platform, which must inherit from
     * {@link AbstractProvider}.
     *
     * @return The class.
     */
    default Class<? extends AbstractProvider> getTargetClass() {
        return AbstractProvider.class;
    }

}
