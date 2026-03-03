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
package org.miaixz.bus.shade.safety.provider;

import org.miaixz.bus.shade.safety.Complex;

/**
 * An abstract base class for decryptor providers that support filtering of entries. This class combines the
 * functionality of a {@link DecryptorProvider} with a {@link Complex} filter, allowing subclasses to decide whether to
 * decrypt an entry based on the filter's logic.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class EntryDecryptorProvider<E> extends WrappedDecryptorProvider
        implements DecryptorProvider, Complex<E> {

    /**
     * The filter used to determine whether an entry should be decrypted.
     */
    protected final Complex<E> filter;
    /**
     * A no-operation decryptor provider, used when an entry is filtered out.
     */
    protected final NopDecryptorProvider xNopDecryptor = new NopDecryptorProvider();

    /**
     * Constructs an {@code EntryDecryptorProvider} with a delegate decryptor and no filter.
     *
     * @param decryptorProvider The delegate decryptor provider.
     */
    protected EntryDecryptorProvider(DecryptorProvider decryptorProvider) {
        this(decryptorProvider, null);
    }

    /**
     * Constructs an {@code EntryDecryptorProvider} with a delegate decryptor and a specified filter.
     *
     * @param decryptorProvider The delegate decryptor provider.
     * @param filter            The filter to apply to entries. If {@code null}, all entries will be processed.
     */
    protected EntryDecryptorProvider(DecryptorProvider decryptorProvider, Complex<E> filter) {
        super(decryptorProvider);
        this.filter = filter;
    }

    /**
     * Determines whether the given entry should be processed based on the configured filter.
     *
     * @param entry The entry to check.
     * @return {@code true} if the entry should be processed (i.e., the filter is null or the filter accepts the entry);
     *         {@code false} otherwise.
     */
    @Override
    public boolean on(E entry) {
        return null == filter || filter.on(entry);
    }

}
