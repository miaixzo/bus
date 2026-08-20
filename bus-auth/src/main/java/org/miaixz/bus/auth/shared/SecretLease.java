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
package org.miaixz.bus.auth.shared;

import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Owns short-lived character-based secret material and erases it deterministically on close.
 * <p>
 * Construction transfers exclusive ownership of the supplied array to this lease without copying it. The producer must
 * discard its reference immediately, and the consumer must use try-with-resources and must not retain the array beyond
 * that lexical scope or an asynchronous stage boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SecretLease implements AutoCloseable {

    /**
     * Exclusively owned secret array, or {@code null} after close.
     */
    private char[] material;

    /**
     * Creates a lease by taking exclusive ownership of the supplied array.
     *
     * @param material non-null secret character array whose ownership is transferred to this lease
     * @throws IllegalArgumentException if {@code material} is {@code null}
     */
    public SecretLease(final char[] material) {
        this.material = Assert.notNull(material, "Secret lease material must not be null");
    }

    /**
     * Returns the owned array for immediate use while the lease remains open.
     *
     * @return exclusively owned mutable secret array
     * @throws ValidateException if the lease has already been closed
     */
    public synchronized char[] material() {
        if (material == null) {
            throw new ValidateException("Secret lease is closed");
        }
        return material;
    }

    /**
     * Idempotently erases the complete owned array and discards its reference.
     */
    @Override
    public synchronized void close() {
        if (material != null) {
            Arrays.fill(material, '\0');
            material = null;
        }
    }

}
