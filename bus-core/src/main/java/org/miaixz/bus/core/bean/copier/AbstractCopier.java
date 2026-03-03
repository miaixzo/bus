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
