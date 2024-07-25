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

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 严格的Bean信息描述做为BeanInfo替代方案，此对象持有JavaBean中的setters和getters等相关信息描述，
 * 在获取Bean属性的时候，要求字段必须存在并严格匹配。查找Getter和Setter方法时会：
 *
 * <ol>
 *     <li>忽略字段和方法名的大小写</li>
 *     <li>Getter查找getXXX、isXXX、getIsXXX</li>
 *     <li>Setter查找setXXX、setIsXXX</li>
 *     <li>Setter忽略参数值与字段值不匹配的情况，因此有多个参数类型的重载时，会调用首次匹配的</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StrictBeanDesc extends AbstractBeanDesc {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param beanClass Bean类
     */
    public StrictBeanDesc(final Class<?> beanClass) {
        super(beanClass);
        init();
    }

    /**
     * 针对Boolean或boolean类型字段，查找其对应的Getter方法，规则为：
     * <ul>
     *     <li>方法必须无参数且返回boolean或Boolean</li>
     *     <li>如果字段为isName, 匹配isName、isIsName方法，两个方法均存在，则按照提供的方法数组优先匹配。</li>
     *     <li>如果字段为name, 匹配isName方法</li>
     * </ul>
     *
     * @param gettersOrSetters 所有方法
     * @param fieldName        字段名
     * @param ignoreCase       是否忽略大小写
     * @return 查找到的方法，{@code null}表示未找到
     */
    private static Method getGetterForBoolean(final Method[] gettersOrSetters, final String fieldName, final boolean ignoreCase) {
        // 查找isXXX
        return MethodKit.getMethod(gettersOrSetters, m -> {
            if (0 != m.getParameterCount() || false == BooleanKit.isBoolean(m.getReturnType())) {
                // getter方法要求无参数且返回boolean或Boolean
                return false;
            }

            if (StringKit.startWith(fieldName, "is", ignoreCase)) {
                // isName -》 isName
                if (StringKit.equals(fieldName, m.getName(), ignoreCase)) {
                    return true;
                }
            }

            // name   - isName
            // isName - isIsName
            return StringKit.equals(StringKit.upperFirstAndAddPre(fieldName, "is"), m.getName(), ignoreCase);
        });
    }

    /**
     * 针对Boolean或boolean类型字段，查找其对应的Setter方法，规则为：
     * <ul>
     *     <li>方法必须为1个boolean或Boolean参数</li>
     *     <li>如果字段为isName，匹配setName</li>
     * </ul>
     *
     * @param fieldName        字段名
     * @param gettersOrSetters 所有方法
     * @param ignoreCase       是否忽略大小写
     * @return 查找到的方法，{@code null}表示未找到
     */
    private static Method getSetterForBoolean(final Method[] gettersOrSetters, final String fieldName, final boolean ignoreCase) {
        // 查找isXXX
        return MethodKit.getMethod(gettersOrSetters, m -> {
            if (1 != m.getParameterCount() || false == BooleanKit.isBoolean(m.getParameterTypes()[0])) {
                // setter方法要求1个boolean或Boolean参数
                return false;
            }

            if (StringKit.startWith(fieldName, "is", ignoreCase)) {
                // isName -》 setName
                return StringKit.equals(
                        "set" + StringKit.removePrefix(fieldName, "is", ignoreCase),
                        m.getName(), ignoreCase);
            }

            // 其它不匹配
            return false;
        });
    }

    /**
     * 初始化
     * 只有与属性关联的相关Getter和Setter方法才会被读取，无关的getXXX和setXXX都被忽略
     */
    private void init() {
        if (RecordKit.isRecord(getBeanClass())) {
            initForRecord();
        } else {
            initForBean();
        }
    }

    /**
     * 针对Record类的反射初始化
     */
    private void initForRecord() {
        final Class<?> beanClass = this.beanClass;
        final Map<String, PropDesc> propMap = this.propMap;

        final Method[] getters = MethodKit.getPublicMethods(beanClass, method -> 0 == method.getParameterCount());
        // 排除静态属性和对象子类
        final Field[] fields = FieldKit.getFields(beanClass, field -> !ModifierKit.isStatic(field) && !FieldKit.isOuterClassField(field));
        for (final Field field : fields) {
            for (final Method getter : getters) {
                if (field.getName().equals(getter.getName())) {
                    //record对象，getter方法与字段同名
                    final PropDesc prop = new PropDesc(field, getter, null);
                    propMap.putIfAbsent(prop.getFieldName(), prop);
                }
            }
        }
    }

    /**
     * 普通Bean初始化
     */
    private void initForBean() {
        final Class<?> beanClass = this.beanClass;
        final Map<String, PropDesc> propMap = this.propMap;

        final Method[] gettersAndSetters = MethodKit.getPublicMethods(beanClass, MethodKit::isGetterOrSetterIgnoreCase);
        // 排除静态属性和对象子类
        final Field[] fields = FieldKit.getFields(beanClass, field -> !ModifierKit.isStatic(field) && !FieldKit.isOuterClassField(field));
        PropDesc prop;
        for (final Field field : fields) {
            prop = createProp(field, gettersAndSetters);
            // 只有不存在时才放入，防止父类属性覆盖子类属性
            propMap.putIfAbsent(prop.getFieldName(), prop);
        }
    }

    /**
     * 根据字段创建属性描述
     * 查找Getter和Setter方法时会：
     *
     * <pre>
     * 1. 忽略字段和方法名的大小写
     * 2. Getter查找getXXX、isXXX、getIsXXX
     * 3. Setter查找setXXX、setIsXXX
     * 4. Setter忽略参数值与字段值不匹配的情况，因此有多个参数类型的重载时，会调用首次匹配的
     * </pre>
     *
     * @param field   字段
     * @param methods 类中所有的方法
     * @return {@link PropDesc}
     */
    private PropDesc createProp(final Field field, final Method[] methods) {
        final PropDesc prop = findProp(field, methods, false);
        // 忽略大小写重新匹配一次
        if (null == prop.getter || null == prop.setter) {
            final PropDesc propIgnoreCase = findProp(field, methods, true);
            if (null == prop.getter) {
                prop.getter = propIgnoreCase.getter;
            }
            if (null == prop.setter) {
                prop.setter = propIgnoreCase.setter;
            }
        }

        return prop;
    }

    /**
     * 查找字段对应的Getter和Setter方法
     *
     * @param field            字段
     * @param gettersOrSetters 类中所有的Getter或Setter方法
     * @param ignoreCase       是否忽略大小写匹配
     * @return PropDesc
     */
    private PropDesc findProp(final Field field, final Method[] gettersOrSetters, final boolean ignoreCase) {
        final String fieldName = field.getName();
        final Class<?> fieldType = field.getType();
        final boolean isBooleanField = BooleanKit.isBoolean(fieldType);

        // Getter: name -> getName, Setter: name -> setName
        final Method[] getterAndSetter = findGetterAndSetter(fieldName, fieldType, gettersOrSetters, ignoreCase);

        if (isBooleanField) {
            if (null == getterAndSetter[0]) {
                // isName -> isName or isIsName
                // name -> isName
                getterAndSetter[0] = getGetterForBoolean(gettersOrSetters, fieldName, ignoreCase);
            }
            if (null == getterAndSetter[1]) {
                // isName -> setName
                getterAndSetter[1] = getSetterForBoolean(gettersOrSetters, fieldName, ignoreCase);
            }
        }

        return new PropDesc(field, getterAndSetter[0], getterAndSetter[1]);
    }

    /**
     * 查找字段对应的Getter和Setter方法
     * 此方法不区分是否为boolean字段，查找规则为：
     * <ul>
     *     <li>Getter要求无参数且返回值是字段类型或字段的父类</li>
     *     <li>Getter中，如果字段为name，匹配getName</li>
     *     <li>Setter要求一个参数且参数必须为字段类型或字段的子类</li>
     *     <li>Setter中，如果字段为name，匹配setName</li>
     * </ul>
     *
     * @param fieldName        字段名
     * @param fieldType        字段类型
     * @param gettersOrSetters 类中所有的Getter或Setter方法
     * @return PropDesc
     */
    private Method[] findGetterAndSetter(final String fieldName, final Class<?> fieldType,
                                         final Method[] gettersOrSetters, final boolean ignoreCase) {
        Method getter = null;
        Method setter = null;
        String methodName;
        for (final Method method : gettersOrSetters) {
            methodName = method.getName();
            if (0 == method.getParameterCount()) {
                // 无参数，可能为Getter方法
                if (StringKit.equals(methodName, CharsBacker.genGetter(fieldName), ignoreCase) &&
                        method.getReturnType().isAssignableFrom(fieldType)) {
                    // getter的返回类型必须为字段类型或字段的父类
                    getter = method;
                }
            } else if (StringKit.equals(methodName, CharsBacker.genSetter(fieldName), ignoreCase) &&
                    fieldType.isAssignableFrom(method.getParameterTypes()[0])) {
                // setter方法的参数必须为字段类型或字段的子类
                setter = method;
            }
            if (null != getter && null != setter) {
                // 如果Getter和Setter方法都找到了，不再继续寻找
                break;
            }
        }

        return new Method[]{getter, setter};
    }

}
