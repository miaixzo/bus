/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
