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
package org.miaixz.bus.errors;

import java.io.Serial;
import java.util.Objects;

/**
 * Carries a classified failure across an exception boundary.
 *
 * <p>
 * The exception message is the descriptor's message key. A rendering boundary can resolve that key using the
 * descriptor's public parameters. The original cause and any suppressed failures remain diagnostic data. On the JVM,
 * Java object serialization preserves the exception and its descriptor subject to the serializability of the diagnostic
 * cause chain. Protocol adapters should render the public descriptor fields.
 * </p>
 *
 * @author Kimi Liu
 */
public final class BusException extends RuntimeException {

    /**
     * Serialization version of the exception fields.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Structured failure exposed to the receiving boundary.
     */
    private final ErrorDescriptor error;

    /**
     * Creates an exception carrying a classified error without a cause.
     *
     * @param error nonnull error descriptor
     * @throws NullPointerException if the descriptor is {@code null}
     */
    public BusException(ErrorDescriptor error) {
        this(error, null);
    }

    /**
     * Creates an exception carrying a classified error and its original cause.
     *
     * @param error nonnull error descriptor
     * @param cause original diagnostic failure, or {@code null} if absent
     * @throws NullPointerException if the descriptor is {@code null}
     */
    public BusException(ErrorDescriptor error, Throwable cause) {
        super(Objects.requireNonNull(error, "error").messageKey(), cause);
        this.error = error;
    }

    /**
     * Returns the structured description for classification or presentation.
     *
     * @return immutable error descriptor
     */
    public ErrorDescriptor error() {
        return this.error;
    }

}
