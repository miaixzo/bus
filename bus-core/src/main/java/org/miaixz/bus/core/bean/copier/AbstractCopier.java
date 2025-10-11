/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.bean.copier;

import org.miaixz.bus.core.bean.desc.BeanDesc;
import org.miaixz.bus.core.lang.copier.Copier;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * Abstract base class for object copying, providing a common structure for holding source and target objects.
 *
 * @param <S> The type of the source object.
 * @param <T> The type of the target object.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractCopier<S, T> implements Copier<T> {

    /**
     * The source object from which properties are copied.
     */
    protected final S source;
    /**
     * The target object to which properties are copied.
     */
    protected final T target;
    /**
     * The options governing the copying process.
     */
    protected final CopyOptions copyOptions;

    /**
     * Constructs an {@code AbstractCopier} with the specified source, target, and copy options.
     *
     * @param source      The source object.
     * @param target      The target object.
     * @param copyOptions The copy options. If {@code null}, default options will be used.
     */
    public AbstractCopier(final S source, final T target, final CopyOptions copyOptions) {
        this.source = source;
        this.target = target;
        this.copyOptions = ObjectKit.defaultIfNull(copyOptions, CopyOptions::of);
    }

    /**
     * Retrieves the {@link BeanDesc} for a given class. If a custom {@link BeanDesc} implementation is specified in
     * {@link CopyOptions}, it will be used; otherwise, the default rules will apply.
     *
     * @param actualEditable The class for which to retrieve the {@link BeanDesc}.
     * @return The {@link BeanDesc} instance for the specified class.
     */
    protected BeanDesc getBeanDesc(final Class<?> actualEditable) {
        if (null != this.copyOptions) {
            final Class<BeanDesc> beanDescClass = copyOptions.beanDescClass;
            if (null != beanDescClass) {
                return ReflectKit.newInstance(beanDescClass, actualEditable);
            }
        }
        return BeanKit.getBeanDesc(actualEditable);
    }

}
