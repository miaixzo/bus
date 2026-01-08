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
