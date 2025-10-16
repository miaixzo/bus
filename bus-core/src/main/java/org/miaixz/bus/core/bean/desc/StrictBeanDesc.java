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
package org.miaixz.bus.core.bean.desc;

import java.io.Serial;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

/**
 * A strict Bean information descriptor, serving as an alternative to {@link java.beans.BeanInfo}. This object holds
 * relevant information about a JavaBean's setters and getters. When retrieving Bean properties, it requires fields to
 * exist and match strictly. When searching for Getter and Setter methods, it will:
 *
 * <ol>
 * <li>Ignore case for field and method names.</li>
 * <li>For Getters, search for methods like {@code getXXX}, {@code isXXX}, and {@code getIsXXX}.</li>
 * <li>For Setters, search for methods like {@code setXXX} and {@code setIsXXX}.</li>
 * <li>For Setters, it ignores cases where parameter values do not match field values. Therefore, if there are multiple
 * overloaded methods with different parameter types, the first matching one will be called.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StrictBeanDesc extends AbstractBeanDesc {

    @Serial
    private static final long serialVersionUID = 2852227769103L;

    /**
     * Whether to ignore case when matching methods and fields.
     */
    private final boolean ignoreCase;
    /**
     * Whether to match primitive types and their wrapper types when matching methods and fields. For example, if a
     * field is of type {@code int}, a setter could be {@code setXXX(int value)} or {@code setXXX(Integer value)}. If a
     * field is of type {@code Integer}, a setter could be {@code setXXX(Integer value)} or {@code setXXX(int value)}.
     */
    private final boolean isMatchPrimitive;

    /**
     * Constructs a {@code StrictBeanDesc} for the given Bean class with default settings. By default, it ignores case
     * and does not match primitive types with their wrappers.
     *
     * @param beanClass The class of the Bean. Must not be {@code null}.
     */
    public StrictBeanDesc(final Class<?> beanClass) {
        this(beanClass, true, false);
    }

    /**
     * Constructs a {@code StrictBeanDesc} for the given Bean class with specified settings.
     *
     * @param beanClass        The class of the Bean. Must not be {@code null}.
     * @param ignoreCase       Whether to ignore case when matching methods and fields.
     * @param isMatchPrimitive Whether to match primitive types with their wrapper types.
     */
    public StrictBeanDesc(final Class<?> beanClass, final boolean ignoreCase, final boolean isMatchPrimitive) {
        super(beanClass);
        this.ignoreCase = ignoreCase;
        this.isMatchPrimitive = isMatchPrimitive;
        init();
    }

    /**
     * Finds the corresponding Getter method for a Boolean or boolean type field. The rules are:
     * <ul>
     * <li>The method must have no parameters and return {@code boolean} or {@code Boolean}.</li>
     * <li>If the field name is {@code isName}, it matches {@code isName} or {@code isIsName}. If both exist, the one
     * found first in the provided method array takes precedence.</li>
     * <li>If the field name is {@code name}, it matches {@code isName}.</li>
     * </ul>
     *
     * @param gettersOrSetters An array of all potential getter or setter methods.
     * @param fieldName        The name of the field.
     * @param ignoreCase       Whether to ignore case during matching.
     * @return The found method, or {@code null} if not found.
     */
    private static Method getGetterForBoolean(
            final Method[] gettersOrSetters,
            final String fieldName,
            final boolean ignoreCase) {
        // Search for isXXX
        return MethodKit.getMethod(gettersOrSetters, m -> {
            if (0 != m.getParameterCount() || !BooleanKit.isBoolean(m.getReturnType())) {
                // Getter method requires no parameters and returns boolean or Boolean.
                return false;
            }

            if (StringKit.startWith(fieldName, Normal.IS, ignoreCase)) {
                // isName -> isName
                if (StringKit.equals(fieldName, m.getName(), ignoreCase)) {
                    return true;
                }
            }

            // name -> isName
            // isName -> isIsName
            return StringKit.equals(StringKit.upperFirstAndAddPre(fieldName, Normal.IS), m.getName(), ignoreCase);
        });
    }

    /**
     * Finds the corresponding Setter method for a Boolean or boolean type field. The rules are:
     * <ul>
     * <li>The method must have one parameter of type {@code boolean} or {@code Boolean}.</li>
     * <li>If the field name is {@code isName}, it matches {@code setName}.</li>
     * </ul>
     *
     * @param fieldName        The name of the field.
     * @param gettersOrSetters An array of all potential getter or setter methods.
     * @param ignoreCase       Whether to ignore case during matching.
     * @return The found method, or {@code null} if not found.
     */
    private static Method getSetterForBoolean(
            final Method[] gettersOrSetters,
            final String fieldName,
            final boolean ignoreCase) {
        // Search for isXXX
        return MethodKit.getMethod(gettersOrSetters, m -> {
            if (1 != m.getParameterCount() || !BooleanKit.isBoolean(m.getParameterTypes()[0])) {
                // Setter method requires one boolean or Boolean parameter.
                return false;
            }

            if (StringKit.startWith(fieldName, Normal.IS, ignoreCase)) {
                // isName -> setName
                return StringKit.equals(
                        Normal.SET + StringKit.removePrefix(fieldName, Normal.IS, ignoreCase),
                        m.getName(),
                        ignoreCase);
            }

            // Other cases do not match.
            return false;
        });
    }

    /**
     * Initializes the Bean description by finding and loading getter and setter methods. Only getter and setter methods
     * directly associated with properties are read; irrelevant {@code getXXX} and {@code setXXX} methods are ignored.
     */
    private void init() {
        final Class<?> beanClass = this.beanClass;
        final Map<String, PropDesc> propMap = this.propMap;

        final Method[] gettersAndSetters = MethodKit.getPublicMethods(beanClass, MethodKit::isGetterOrSetterIgnoreCase);
        // Exclude static fields and outer class fields.
        final Field[] fields = FieldKit
                .getFields(beanClass, field -> !ModifierKit.isStatic(field) && !FieldKit.isOuterClassField(field));
        PropDesc prop;
        for (final Field field : fields) {
            prop = createProp(field, gettersAndSetters);
            // Only add if not present, to prevent parent class properties from overriding child class properties.
            propMap.putIfAbsent(prop.getFieldName(), prop);
        }
    }

    /**
     * Creates a property descriptor for a given field. When searching for Getter and Setter methods, it will:
     *
     * <pre>
     * 1. Ignore case for field and method names.
     * 2. For Getters, search for {@code
     * getXXX
     * }, {@code
     * isXXX
     * }, {@code
     * getIsXXX
     * }.
     * 3. For Setters, search for {@code
     * setXXX
     * }, {@code
     * setIsXXX
     * }.
     * 4. For Setters, it ignores cases where parameter values do not match field values. Therefore, if there are multiple overloaded methods with different parameter types, the first matching one will be called.
     * </pre>
     *
     * @param field   The field for which to create the property descriptor.
     * @param methods All methods in the class.
     * @return A {@link PropDesc} object describing the property.
     */
    private PropDesc createProp(final Field field, final Method[] methods) {
        final PropDesc prop = findProp(field, methods, false);

        // If ignoreCase is true and either getter or setter is not found, try matching again with ignoreCase.
        if (ignoreCase && (null == prop.getter || null == prop.setter)) {
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
     * Finds the corresponding Getter and Setter methods for a given field.
     *
     * @param field            The field.
     * @param gettersOrSetters An array of all potential getter or setter methods in the class.
     * @param ignoreCase       Whether to ignore case during matching.
     * @return A {@code PropDesc} object containing the found getter and setter methods.
     */
    private PropDesc findProp(final Field field, final Method[] gettersOrSetters, final boolean ignoreCase) {
        final String fieldName = field.getName();
        final Class<?> fieldType = field.getType();
        final boolean isBooleanField = BooleanKit.isBoolean(fieldType);

        // Find getter and setter based on standard naming conventions (getXXX, setXXX).
        final Method[] getterAndSetter = findGetterAndSetter(fieldName, fieldType, gettersOrSetters, ignoreCase);

        if (isBooleanField) {
            if (null == getterAndSetter[0]) {
                // For boolean fields, try to find isName -> isName or isIsName, or name -> isName.
                getterAndSetter[0] = getGetterForBoolean(gettersOrSetters, fieldName, ignoreCase);
            }
            if (null == getterAndSetter[1]) {
                // For boolean fields, try to find isName -> setName.
                getterAndSetter[1] = getSetterForBoolean(gettersOrSetters, fieldName, ignoreCase);
            }
        }

        return new PropDesc(field, getterAndSetter[0], getterAndSetter[1]);
    }

    /**
     * Finds the corresponding Getter and Setter methods for a given field. This method does not distinguish between
     * boolean fields and other types. The search rules are:
     * <ul>
     * <li>Getter: Requires no parameters and its return type must be the field's type or a supertype of the field's
     * type.</li>
     * <li>Getter: If the field name is {@code name}, it matches {@code getName}.</li>
     * <li>Setter: Requires one parameter, and its parameter type must be the field's type or a subtype of the field's
     * type.</li>
     * <li>Setter: If the field name is {@code name}, it matches {@code setName}.</li>
     * </ul>
     *
     * @param fieldName        The name of the field.
     * @param fieldType        The type of the field.
     * @param gettersOrSetters An array of all potential getter or setter methods in the class.
     * @param ignoreCase       Whether to ignore case during matching.
     * @return An array containing the found getter method at index 0 and the setter method at index 1.
     */
    private Method[] findGetterAndSetter(
            final String fieldName,
            final Class<?> fieldType,
            final Method[] gettersOrSetters,
            final boolean ignoreCase) {
        Method getter = null;
        Method setter = null;
        String methodName;
        for (final Method method : gettersOrSetters) {
            methodName = method.getName();
            if (0 == method.getParameterCount()) {
                // No parameters, potentially a Getter method.
                if (StringKit.equals(methodName, CharsBacker.genGetter(fieldName), ignoreCase)) {
                    final Class<?> returnType = method.getReturnType();
                    if (returnType.isAssignableFrom(fieldType)
                            || (isMatchPrimitive && ClassKit.isBasicTypeMatch(returnType, fieldType))) {
                        // The return type of the getter must be the field type or a superclass of the field type.
                        getter = method;
                    }
                }
            } else if (StringKit.equals(methodName, CharsBacker.genSetter(fieldName), ignoreCase)) {
                final Class<?> parameterType = method.getParameterTypes()[0];
                if (fieldType.isAssignableFrom(parameterType)
                        || (isMatchPrimitive && ClassKit.isBasicTypeMatch(fieldType, parameterType))) {
                    // The parameter of the setter method must be the field type or a subclass of the field type.
                    setter = method;
                }
            }
            if (null != getter && null != setter) {
                // If both Getter and Setter methods are found, stop searching.
                break;
            }
        }

        return new Method[] { getter, setter };
    }

}
