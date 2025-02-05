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
package org.miaixz.bus.core.center.map;

import java.util.*;

import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Bean的Map接口实现 通过反射方式，将一个Bean的操作转化为Map操作
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanMap implements Map<String, Object> {

    private final Object bean;
    private final Map<String, PropDesc> propDescMap;

    /**
     * 构造
     *
     * @param bean Bean
     */
    public BeanMap(final Object bean) {
        this.bean = bean;
        this.propDescMap = BeanKit.getBeanDesc(bean.getClass()).getPropMap(false);
    }

    /**
     * 构建BeanMap
     *
     * @param bean Bean
     * @return this
     */
    public static BeanMap of(final Object bean) {
        return new BeanMap(bean);
    }

    @Override
    public int size() {
        return this.propDescMap.size();
    }

    @Override
    public boolean isEmpty() {
        return propDescMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.propDescMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        for (final PropDesc propDesc : this.propDescMap.values()) {
            if (ObjectKit.equals(propDesc.getValue(bean, false), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(final Object key) {
        final PropDesc propDesc = this.propDescMap.get(key);
        if (null != propDesc) {
            return propDesc.getValue(bean, false);
        }
        return null;
    }

    /**
     * 获取Path表达式对应的值
     *
     * @param expression Path表达式
     * @return 值
     */
    public Object getProperty(final String expression) {
        return BeanKit.getProperty(bean, expression);
    }

    @Override
    public Object put(final String key, final Object value) {
        final PropDesc propDesc = this.propDescMap.get(key);
        if (null != propDesc) {
            final Object oldValue = propDesc.getValue(bean, false);
            propDesc.setValue(bean, value);
            return oldValue;
        }
        return null;
    }

    /**
     * 设置Path表达式对应的值
     *
     * @param expression Path表达式
     * @param value      新值
     */
    public void putProperty(final String expression, final Object value) {
        BeanKit.setProperty(bean, expression, value);
    }

    @Override
    public Object remove(final Object key) {
        throw new UnsupportedOperationException("Can not remove field for Bean!");
    }

    @Override
    public void putAll(final Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Can not clear fields for Bean!");
    }

    @Override
    public Set<String> keySet() {
        return this.propDescMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        final List<Object> list = new ArrayList<>(size());
        for (final PropDesc propDesc : this.propDescMap.values()) {
            list.add(propDesc.getValue(bean, false));
        }
        return list;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        final HashSet<Entry<String, Object>> set = new HashSet<>(size(), 1);
        this.propDescMap.forEach(
                (key, propDesc) -> set.add(new AbstractMap.SimpleEntry<>(key, propDesc.getValue(bean, false))));
        return set;
    }

}
