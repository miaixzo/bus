/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 抽象的对象拷贝封装，提供来源对象、目标对象持有
 *
 * @param <S> 来源对象类型
 * @param <T> 目标对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractCopier<S, T> implements Copier<T> {

    /**
     * 源对象
     */
    protected final S source;
    /**
     * 目标对象
     */
    protected final T target;
    /**
     * 拷贝选项
     */
    protected final CopyOptions copyOptions;

    /**
     * 构造
     *
     * @param source      源对象
     * @param target      目标对象
     * @param copyOptions 拷贝选项
     */
    public AbstractCopier(final S source, final T target, final CopyOptions copyOptions) {
        this.source = source;
        this.target = target;
        this.copyOptions = ObjectKit.defaultIfNull(copyOptions, CopyOptions::of);
    }

    /**
     * 获取Bean描述信息 如果用户自定义了{@link BeanDesc}实现，则使用，否则使用默认的规则
     *
     * @param actualEditable 需要解析的类
     * @return {@link BeanDesc}
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
