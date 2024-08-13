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
package org.miaixz.bus.core.bean.desc;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.lang.Assert;

/**
 * Bean描述抽象类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractBeanDesc implements BeanDesc {

    private static final long serialVersionUID = -1L;

    /**
     * Bean类
     */
    protected final Class<?> beanClass;

    /**
     * 属性Map
     */
    protected final Map<String, PropDesc> propMap = new LinkedHashMap<>();

    /**
     * 构造
     *
     * @param beanClass Bean类
     */
    public AbstractBeanDesc(final Class<?> beanClass) {
        this.beanClass = Assert.notNull(beanClass);
    }

    /**
     * 获取Bean的全类名
     *
     * @return Bean的类名
     */
    public String getName() {
        return this.beanClass.getName();
    }

    /**
     * 获取Bean的简单类名
     *
     * @return Bean的类名
     */
    public String getSimpleName() {
        return this.beanClass.getSimpleName();
    }

    /**
     * 获取Bean类
     *
     * @return Bean类
     */
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Override
    public Map<String, PropDesc> getPropMap(final boolean ignoreCase) {
        return ignoreCase ? new CaseInsensitiveMap<>(1, this.propMap) : this.propMap;
    }

    /**
     * 获得字段名对应的字段对象，如果不存在返回null
     *
     * @param fieldName 字段名
     * @return 字段值
     */
    public Field getField(final String fieldName) {
        final PropDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getField();
    }

}
