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
package org.miaixz.bus.core.center.map;

import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.bean.path.BeanPath;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CloneException;
import org.miaixz.bus.core.lang.getter.TypeGetter;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.LambdaKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.SetKit;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 字典对象，扩充了LinkedHashMap中的方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dictionary extends CustomKeyMap<String, Object> implements TypeGetter<String> {

    private static final long serialVersionUID = -1L;
    /**
     * 是否大小写不敏感
     */
    private boolean caseInsensitive;

    /**
     * 构造
     */
    public Dictionary() {
        this(false);
    }

    /**
     * 构造
     *
     * @param caseInsensitive 是否大小写不敏感
     */
    public Dictionary(final boolean caseInsensitive) {
        this(Normal._16, caseInsensitive);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     */
    public Dictionary(final int initialCapacity) {
        this(initialCapacity, false);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param caseInsensitive 是否大小写不敏感
     */
    public Dictionary(final int initialCapacity, final boolean caseInsensitive) {
        this(initialCapacity, Normal.DEFAULT_LOAD_FACTOR, caseInsensitive);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param loadFactor      容量增长因子，0~1，即达到容量的百分之多少时扩容
     */
    public Dictionary(final int initialCapacity, final float loadFactor) {
        this(initialCapacity, loadFactor, false);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param loadFactor      容量增长因子，0~1，即达到容量的百分之多少时扩容
     * @param caseInsensitive 是否大小写不敏感
     */
    public Dictionary(final int initialCapacity, final float loadFactor, final boolean caseInsensitive) {
        super(new LinkedHashMap<>(initialCapacity, loadFactor));
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public Dictionary(final Map<String, Object> m) {
        super((null == m) ? new HashMap<>() : m);
    }

    /**
     * 创建Dict
     *
     * @return Dictionary
     */
    public static Dictionary of() {
        return new Dictionary();
    }

    /**
     * 将PO对象转为Dict
     *
     * @param <T>  Bean类型
     * @param bean Bean对象
     * @return Vo
     */
    public static <T> Dictionary parse(final T bean) {
        return of().parseBean(bean);
    }

    /**
     * 根据给定的Entry数组创建Dict对象
     *
     * @param pairs 键值对
     * @return Dictionary
     */
    @SafeVarargs
    public static Dictionary ofEntries(final Map.Entry<String, Object>... pairs) {
        final Dictionary dictionary = of();
        for (final Map.Entry<String, Object> pair : pairs) {
            dictionary.put(pair.getKey(), pair.getValue());
        }
        return dictionary;
    }

    /**
     * 根据给定的键值对数组创建Dict对象，传入参数必须为key,value,data,value...
     *
     * <p>奇数参数必须为key，key最后会转换为String类型。</p>
     * <p>偶数参数必须为value，可以为任意类型。</p>
     *
     * <pre>
     * Dictionary dict = Dictionary.of(
     * 	"RED", "#FF0000",
     * 	"GREEN", "#00FF00",
     * 	"BLUE", "#0000FF"
     * );
     * </pre>
     *
     * @param keysAndValues 键值对列表，必须奇数参数为key，偶数参数为value
     * @return Dictionary
     */
    public static Dictionary ofKvs(final Object... keysAndValues) {
        final Dictionary dictionary = of();

        String key = null;
        for (int i = 0; i < keysAndValues.length; i++) {
            // 偶数
            if ((i & 1) == 0) {
                key = Convert.toString(keysAndValues[i]);
            } else {
                dictionary.put(key, keysAndValues[i]);
            }
        }

        return dictionary;
    }

    /**
     * 转换为Bean对象
     *
     * @param <T>  Bean类型
     * @param bean Bean
     * @return Bean
     */
    public <T> T toBean(final T bean) {
        return BeanKit.fillBeanWithMap(this, bean, CopyOptions.of());
    }

    /**
     * 转换为Bean对象
     *
     * @param <T>  Bean类型
     * @param bean Bean
     * @return Bean
     */
    public <T> T toBeanIgnoreCase(final T bean) {
        return BeanKit.fillBeanWithMap(this, bean, CopyOptions.of().setIgnoreCase(true));
    }

    /**
     * 填充Value Object对象
     *
     * @param <T>   Bean类型
     * @param clazz Value Object（或者POJO）的类
     * @return vo
     */
    public <T> T toBean(final Class<T> clazz) {
        return BeanKit.toBean(this, clazz);
    }

    /**
     * 填充Value Object对象，忽略大小写
     *
     * @param <T>   Bean类型
     * @param clazz Value Object（或者POJO）的类
     * @return vo
     */
    public <T> T toBeanIgnoreCase(final Class<T> clazz) {
        return BeanKit.toBean(this, clazz, CopyOptions.of().setIgnoreCase(true));
    }

    /**
     * 将值对象转换为Dict
     * 类名会被当作表名，小写第一个字母
     *
     * @param <T>  Bean类型
     * @param bean 值对象
     * @return 自己
     */
    public <T> Dictionary parseBean(final T bean) {
        Assert.notNull(bean, "Bean must not be null");
        this.putAll(BeanKit.beanToMap(bean));
        return this;
    }

    /**
     * 将值对象转换为Dict
     * 类名会被当作表名，小写第一个字母
     *
     * @param <T>               Bean类型
     * @param bean              值对象
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return 自己
     */
    public <T> Dictionary parseBean(final T bean, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        Assert.notNull(bean, "Bean must not be null");
        this.putAll(BeanKit.beanToMap(bean, isToUnderlineCase, ignoreNullValue));
        return this;
    }

    /**
     * 与给定实体对比并去除相同的部分
     * 此方法用于在更新操作时避免所有字段被更新，跳过不需要更新的字段 version from 2.0.0
     *
     * @param <T>          字典对象类型
     * @param dict         字典对象
     * @param withoutNames 不需要去除的字段名
     */
    public <T extends Dictionary> void removeEqual(final T dict, final String... withoutNames) {
        final HashSet<String> withoutSet = SetKit.of(withoutNames);
        for (final Map.Entry<String, Object> entry : dict.entrySet()) {
            if (withoutSet.contains(entry.getKey())) {
                continue;
            }

            final Object value = this.get(entry.getKey());
            if (Objects.equals(value, entry.getValue())) {
                this.remove(entry.getKey());
            }
        }
    }

    /**
     * 过滤Map保留指定键值对，如果键不存在跳过
     *
     * @param keys 键列表
     * @return Dictionary 结果
     */
    public Dictionary filterNew(final String... keys) {
        final Dictionary result = new Dictionary(keys.length, 1);

        for (final String key : keys) {
            if (this.containsKey(key)) {
                result.put(key, this.get(key));
            }
        }
        return result;
    }

    /**
     * 过滤Map去除指定键值对，如果键不存在跳过
     *
     * @param keys 键列表
     * @return Dictionary 结果
     */
    public Dictionary removeNew(final String... keys) {
        return MapKit.removeAny(this.clone(), keys);
    }

    /**
     * 设置列
     *
     * @param attr  属性
     * @param value 值
     * @return 本身
     */
    public Dictionary set(final String attr, final Object value) {
        this.put(attr, value);
        return this;
    }

    /**
     * 设置列，当键或值为null时忽略
     *
     * @param attr  属性
     * @param value 值
     * @return 本身
     */
    public Dictionary setIgnoreNull(final String attr, final Object value) {
        if (null != attr && null != value) {
            set(attr, value);
        }
        return this;
    }

    @Override
    public Object getObject(final String key, final Object defaultValue) {
        return getOrDefault(key, defaultValue);
    }

    /**
     * 根据lambda的方法引用，获取
     *
     * @param func 方法引用
     * @param <P>  参数类型
     * @param <T>  返回值类型
     * @return 获取表达式对应属性和返回的对象
     */
    public <P, T> T get(final FunctionX<P, T> func) {
        final LambdaX lambdaX = LambdaKit.resolve(func);
        return get(lambdaX.getFieldName(), lambdaX.getReturnType());
    }

    /**
     * 获得特定类型值
     *
     * @param <T>  值类型
     * @param attr 字段名
     * @return 字段值
     */
    public <T> T getBean(final String attr) {
        return (T) get(attr);
    }

    /**
     * 通过表达式获取JSON中嵌套的对象
     * <ol>
     * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
     * <li>[]表达式，可以获取集合等对象中对应index的值</li>
     * </ol>
     *
     * @param <T>        目标类型
     * @param expression 表达式
     * @return 对象
     * @see BeanPath#getValue(Object)
     */
    public <T> T getByPath(final String expression) {
        return (T) BeanPath.of(expression).getValue(this);
    }

    /**
     * 通过表达式获取JSON中嵌套的对象
     * <ol>
     * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
     * <li>[]表达式，可以获取集合等对象中对应index的值</li>
     * </ol>
     * 获取表达式对应值后转换为对应类型的值
     *
     * @param <T>        返回值类型
     * @param expression 表达式
     * @param resultType 返回值类型
     * @return 对象
     * @see BeanPath#getValue(Object)
     */
    public <T> T getByPath(final String expression, final Type resultType) {
        return Convert.convert(resultType, getByPath(expression));
    }

    @Override
    public Dictionary clone() {
        try {
            return (Dictionary) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

    @Override
    protected String customKey(Object key) {
        if (this.caseInsensitive && null != key) {
            key = ((String) key).toLowerCase();
        }
        return (String) key;
    }

    /**
     * 通过lambda批量设置值
     * 实际使用时，可以使用getXXX的方法引用来完成键值对的赋值：
     * <pre>
     *     User user = GenericBuilder.of(User::new).with(User::setUsername, "name").build();
     *     Dictionary.create().setFields(user::getNickname, user::getUsername);
     * </pre>
     *
     * @param fields lambda,不能为空
     * @return this
     */
    public Dictionary setFields(final SupplierX<?>... fields) {
        Arrays.stream(fields).forEach(f -> set(LambdaKit.getFieldName(f), f.get()));
        return this;
    }

}
