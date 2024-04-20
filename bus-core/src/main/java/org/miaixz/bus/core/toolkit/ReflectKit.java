/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.annotation.Alias;
import org.miaixz.bus.core.beans.NullWrapper;
import org.miaixz.bus.core.collection.UniqueKeySet;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.map.WeakMap;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * 反射工具类.
 * 提供调用getter/setter方法, 访问私有变量, 调用私有方法, 获取泛型类型Class, 被AOP过的真实类等工具函数
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectKit {

    /**
     * 构造对象缓存
     */
    private static final WeakMap<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new WeakMap<>();
    /**
     * 字段缓存
     */
    private static final WeakMap<Class<?>, Field[]> FIELDS_CACHE = new WeakMap<>();
    /**
     * 方法缓存
     */
    private static final WeakMap<Class<?>, Method[]> METHODS_CACHE = new WeakMap<>();

    /**
     * 调用Getter方法.
     * 支持多级,如：对象名.对象名.方法
     *
     * @param object 对象
     * @param name   属性名
     * @return the object
     */
    public static Object invokeGetter(Object object, String name) {
        for (String method : StringKit.splitToArray(name, Symbol.DOT)) {
            String getterMethodName = Normal.GET + StringKit.capitalize(method);
            object = invokeMethod(object, getterMethodName, new Class[]{}, new Object[]{});
        }
        return object;
    }

    /**
     * 调用Setter方法, 仅匹配方法名
     * 支持多级,如：对象名.对象名.方法
     *
     * @param object 对象
     * @param name   属性名
     * @param value  值
     */
    public static void invokeSetter(Object object, String name, Object value) {
        String[] names = StringKit.splitToArray(name, Symbol.DOT);
        for (int i = 0; i < names.length; i++) {
            if (i < names.length - 1) {
                String getterMethodName = Normal.GET + StringKit.capitalize(names[i]);
                object = invokeMethod(object, getterMethodName, new Class[]{}, new Object[]{});
            } else {
                String setterMethodName = Normal.SET + StringKit.capitalize(names[i]);
                invokeMethodByName(object, setterMethodName, new Object[]{value});
            }
        }
    }

    public static Object convert(Object object, Class<?> type) {
        if (object instanceof Number) {
            Number number = (Number) object;
            if (type.equals(byte.class) || type.equals(Byte.class)) {
                return number.byteValue();
            }
            if (type.equals(short.class) || type.equals(Short.class)) {
                return number.shortValue();
            }
            if (type.equals(int.class) || type.equals(Integer.class)) {
                return number.intValue();
            }
            if (type.equals(long.class) || type.equals(Long.class)) {
                return number.longValue();
            }
            if (type.equals(float.class) || type.equals(Float.class)) {
                return number.floatValue();
            }
            if (type.equals(double.class) || type.equals(Double.class)) {
                return number.doubleValue();
            }
        }
        if (type.equals(String.class)) {
            return null == object ? Normal.EMPTY : object.toString();
        }
        return object;
    }

    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, Normal.EMPTY_OBJECT_ARRAY);
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            throw new IllegalStateException("Should never get here");
        }
    }

    /**
     * 直接调用对象方法, 无视private/protected修饰符.
     * 用于一次性调用的情况,否则应使用getAccessibleMethod()函数获得Method后反复调用.
     * 同时匹配方法名+参数类型,
     *
     * @param object 对象
     * @param name   方法名
     * @param types  参数类型
     * @param args   参数
     * @return the object
     */
    public static Object invokeMethod(final Object object, final String name, final Class<?>[] types,
                                      final Object[] args) {
        Method method = getAccessibleMethod(object, name, types);
        if (null == method) {
            throw new IllegalArgumentException("Could not find method [" + method + "] on target [" + object + "]");
        }

        try {
            return method.invoke(object, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 直接调用对象方法, 无视private/protected修饰符,
     * 用于一次性调用的情况,否则应使用getAccessibleMethodByName()函数获得Method后反复调用.
     * 只匹配函数名,如果有多个同名函数调用第一个
     *
     * @param object 对象
     * @param name   方法
     * @param args   参数
     * @return the object
     */
    public static Object invokeMethodByName(final Object object, final String name, final Object[] args) {
        Method method = getAccessibleMethodByName(object, name);
        if (null == method) {
            throw new IllegalArgumentException("Could not find method [" + name + "] on target [" + object + "]");
        }

        try {
            return method.invoke(object, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
     * <p>
     * 如向上转型到Object仍无法找到, 返回null.
     *
     * @param object 对象
     * @param name   列名
     * @return the object
     */
    public static Field getAccessibleField(final Object object, final String name) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(name);
                makeAccessible(field);
                return field;
            } catch (NoSuchFieldException e) {//NOSONAR
                // Field不在当前类定义,继续向上转型
                continue;// new add
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     * 匹配函数名+参数类型
     * <p>
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object object, Object... args)
     *
     * @param object 对象
     * @param name   方法名
     * @param types  参数类型
     * @return the object
     */
    public static Method getAccessibleMethod(final Object object,
                                             final String name,
                                             final Class<?>... types) {
        for (Class<?> searchType = object.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Method method = searchType.getDeclaredMethod(name, types);
                makeAccessible(method);
                return method;
            } catch (NoSuchMethodException e) {
                // Method不在当前类定义,继续向上转型
                continue;
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     * 只匹配函数名
     * <p>
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object object, Object... args)
     *
     * @param object 对象
     * @param name   方法名
     * @return the object
     */
    public static Method getAccessibleMethodByName(final Object object,
                                                   final String name) {
        for (Class<?> searchType = object.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            Method[] methods = searchType.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    makeAccessible(method);
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 改变private/protected的方法为public,尽量不调用实际改动的语句,避免JDK的SecurityManager抱怨
     *
     * @param method 方法
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * 改变private/protected的成员变量为public,尽量不调用实际改动的语句,避免JDK的SecurityManager抱怨
     *
     * @param field 对象
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier
                .isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处
     * 如无法找到, 返回Object.class.
     *
     * @param <T>   对象
     * @param clazz 对象
     * @return the object
     */
    public static <T> Class<T> getClassGenricType(final Class clazz) {
        return getClassGenricType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
     * 如无法找到, 返回Object.class.
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be determined
     */
    public static Class getClassGenricType(final Class clazz, final int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }

        return (Class) params[index];
    }

    public static Class<?> getUserClass(Object instance) {
        Class clazz = instance.getClass();
        if (null != clazz && clazz.getName().contains(Symbol.DOLLAR + Symbol.DOLLAR)) {
            Class<?> superClass = clazz.getSuperclass();
            if (null != superClass && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;
    }

    /**
     * 将反射时的checked exception转换为unchecked exception.
     *
     * @param e 异常
     * @return the ex
     */
    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException(e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException(((InvocationTargetException) e).getTargetException());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Unexpected Checked Exception.", e);
    }

    /**
     * 判断某个对象是否拥有某个属性
     *
     * @param object    对象
     * @param fieldName 属性名
     * @return 有属性返回true
     * 无属性返回false
     */
    public static boolean hasField(final Object object, final String fieldName) {
        Field field = getAccessibleField(object, fieldName);
        return null != field;

    }

    /**
     * 是否为父类引用字段
     * 当字段所在类是对象子类时（对象中定义的非static的class），会自动生成一个以"this$0"为名称的字段，指向父类对象
     *
     * @param field 字段
     * @return 是否为父类引用字段
     */
    public static boolean isOuterClassField(Field field) {
        return "this$0".equals(field.getName());
    }

    /**
     * 查找类中的指定参数的构造方法
     *
     * @param <T>            对象类型
     * @param clazz          类
     * @param parameterTypes 参数类型,只要任何一个参数是指定参数的父类或接口或相等即可
     * @return 构造方法, 如果未找到返回null
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        if (null == clazz) {
            return null;
        }

        final Constructor<?>[] constructors = clazz.getConstructors();
        Class<?>[] pts;
        for (Constructor<?> constructor : constructors) {
            pts = constructor.getParameterTypes();
            if (ClassKit.isAllAssignableFrom(pts, parameterTypes)) {
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    /**
     * 获得一个类中所有构造列表
     *
     * @param <T>       构造的对象类型
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static <T> Constructor<T>[] getConstructors(Class<T> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
        return (Constructor<T>[]) CONSTRUCTORS_CACHE.computeIfAbsent(beanClass, (key) -> getConstructorsDirectly(beanClass));
    }

    /**
     * 获得一个类中所有构造列表,直接反射获取,无缓存
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Constructor<?>[] getConstructorsDirectly(Class<?> beanClass) throws SecurityException {
        return beanClass.getDeclaredConstructors();
    }

    /**
     * 查找指定类中的所有字段(包括非public字段),也包括父类和Object类的字段, 字段不存在则返回null
     *
     * @param beanClass 被查找字段的类,不能为null
     * @param name      字段名
     * @return field字段
     * @throws SecurityException 安全异常
     */
    public static Field getField(Class<?> beanClass, String name) throws SecurityException {
        final Field[] fields = getFields(beanClass);
        return ArrayKit.firstNonNull((field) -> name.equals(getFieldName(field)), fields);
    }

    /**
     * 获得一个类中所有字段列表,包括其父类中的字段
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFields(Class<?> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
        return FIELDS_CACHE.computeIfAbsent(beanClass, (key) -> getFields(beanClass, true));
    }

    /**
     * 获得一个类中所有满足条件的字段列表，包括其父类中的字段
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后
     *
     * @param beanClass   类
     * @param fieldFilter field过滤器，过滤掉不需要的field
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFields(Class<?> beanClass, Predicate<Field> fieldFilter) throws SecurityException {
        return ArrayKit.filter(getFields(beanClass), fieldFilter);
    }

    /**
     * 获得一个类中所有字段列表,直接反射获取,无缓存
     *
     * @param beanClass            类
     * @param withSuperClassFields 是否包括父类的字段列表
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFields(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        Assert.notNull(beanClass);

        Field[] allFields = null;
        Class<?> searchType = beanClass;
        Field[] declaredFields;
        while (null != searchType) {
            declaredFields = searchType.getDeclaredFields();
            if (null == allFields) {
                allFields = declaredFields;
            } else {
                allFields = ArrayKit.append(allFields, declaredFields);
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }

        return allFields;
    }

    /**
     * 获取字段名，如果存在{@link Alias}注解，读取注解的值作为名称
     *
     * @param field 字段信息
     * @return 字段名
     */
    public static String getFieldName(Field field) {
        if (null == field) {
            return null;
        }

        final Alias alias = field.getAnnotation(Alias.class);
        if (null != alias) {
            return alias.value();
        }

        return field.getName();
    }

    /**
     * 获取字段值
     *
     * @param object    对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object object, String fieldName) throws InternalException {
        if (null == object || StringKit.isBlank(fieldName)) {
            return null;
        }
        return getFieldValue(object, getField(object instanceof Class ? (Class<?>) object : object.getClass(), fieldName));
    }

    /**
     * 获取字段值
     *
     * @param object 对象
     * @param field  字段
     * @return 字段值
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object object, Field field) throws InternalException {
        if (null == field) {
            return null;
        }
        if (object instanceof Class) {
            // 静态字段获取时对象为null
            object = null;
        }

        setAccessible(field);
        Object result;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            throw new InternalException("IllegalAccess for {}.{}", field.getDeclaringClass(), field.getName());
        }
        return result;
    }

    /**
     * 获取所有字段的值
     *
     * @param object bean对象，如果是static字段，此处为类class
     * @return 字段值数组
     */
    public static Object[] getFieldsValue(Object object) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass());
            if (null != fields) {
                final Object[] values = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    values[i] = getFieldValue(object, fields[i]);
                }
                return values;
            }
        }
        return null;
    }

    /**
     * 设置字段值
     * 若值类型与字段类型不一致，则会尝试通过 {@link Convert} 进行转换
     * 若字段类型是原始类型而传入的值是 null，则会将字段设置为对应原始类型的默认值（见 {@link ClassKit#getDefaultValue(Class)}）
     *
     * @param object    对象,static字段则此处传Class
     * @param fieldName 字段名
     * @param value     值，当值类型与字段类型不匹配时，会尝试转换
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static void setFieldValue(Object object, String fieldName, Object value) throws InternalException {
        Assert.notNull(object);
        Assert.notBlank(fieldName);

        final Field field = getField((object instanceof Class) ? (Class<?>) object : object.getClass(), fieldName);
        Assert.notNull(field, "Field [{}] is not exist in [{}]", fieldName, object.getClass().getName());
        setFieldValue(object, field, value);
    }

    /**
     * 设置字段值
     * 若值类型与字段类型不一致，则会尝试通过 {@link Convert} 进行转换
     * 若字段类型是原始类型而传入的值是 null，则会将字段设置为对应原始类型的默认值（见 {@link ClassKit#getDefaultValue(Class)}）
     *
     * @param object 对象，如果是static字段，此参数为null
     * @param field  字段
     * @param value  值，当值类型与字段类型不匹配时，会尝试转换
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static void setFieldValue(Object object, Field field, Object value) throws InternalException {
        Assert.notNull(field, "Field in [{}] not exist !", object);

        final Class<?> fieldType = field.getType();
        if (null != value) {
            if (false == fieldType.isAssignableFrom(value.getClass())) {
                //对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                final Object targetValue = Convert.convert(fieldType, value);
                if (null != targetValue) {
                    value = targetValue;
                }
            }
        } else {
            // 获取null对应默认值，防止原始类型造成空指针问题
            value = ClassKit.getDefaultValue(fieldType);
        }

        setAccessible(field);
        try {
            field.set(object instanceof Class ? null : object, value);
        } catch (IllegalAccessException e) {
            throw new InternalException("IllegalAccess for {}.{}", object, field.getName());
        }
    }

    /**
     * 设置静态（static）字段值
     *
     * @param field 字段
     * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
     */
    public static void setStaticFieldValue(final Field field, final Object value) {
        setFieldValue(null, field, value);
    }

    /**
     * 查找指定对象中的所有方法(包括非public方法),也包括父对象和Object类的方法
     *
     * @param object     被查找的对象,如果为{@code null}返回{@code null}
     * @param methodName 方法名,如果为空字符串返回{@code null}
     * @param args       参数
     * @return 方法
     * @throws SecurityException 无访问权限抛出异常
     */
    public static Method getMethodOfObject(Object object, String methodName, Object... args) throws SecurityException {
        if (null == object || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(object.getClass(), methodName, ClassKit.getClasses(args));
    }

    /**
     * 忽略大小写查找指定方法,如果找不到对应的方法则返回null
     *
     * @param clazz      类,如果为{@code null}返回{@code null}
     * @param methodName 方法名,如果为空字符串返回{@code null}
     * @param paramTypes 参数类型,指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
        return getMethod(clazz, true, methodName, paramTypes);
    }

    /**
     * 查找指定方法 如果找不到对应的方法则返回null
     *
     * @param clazz      类,如果为{@code null}返回{@code null}
     * @param methodName 方法名,如果为空字符串返回{@code null}
     * @param paramTypes 参数类型,指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
        return getMethod(clazz, false, methodName, paramTypes);
    }

    /**
     * 查找指定方法 如果找不到对应的方法则返回null
     *
     * @param clazz      类,如果为{@code null}返回{@code null}
     * @param ignoreCase 是否忽略大小写
     * @param methodName 方法名,如果为空字符串返回{@code null}
     * @param paramTypes 参数类型,指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethod(Class<?> clazz, boolean ignoreCase, String methodName, Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }

        Method result = null;
        final Method[] methods = getMethods(clazz);
        if (ArrayKit.isNotEmpty(methods)) {
            for (final Method method : methods) {
                if (StringKit.equals(methodName, method.getName(), ignoreCase)
                        && ClassKit.isAllAssignableFrom(method.getParameterTypes(), paramTypes)
                        && (result == null || result.getReturnType().isAssignableFrom(method.getReturnType()))) {
                    result = method;
                }
            }
        }
        return result;
    }

    /**
     * 获得指定类中的Public方法名
     * 去重重载的方法
     *
     * @param clazz 类
     * @return 方法名Set
     * @throws SecurityException 安全异常
     */
    public static Set<String> getMethodNames(Class<?> clazz) throws SecurityException {
        final HashSet<String> methodSet = new HashSet<>();
        final Method[] methods = getMethods(clazz);
        for (Method method : methods) {
            methodSet.add(method.getName());
        }
        return methodSet;
    }

    /**
     * 获得指定类过滤后的Public方法列表
     *
     * @param clazz  查找方法的类
     * @param filter 过滤器
     * @return 过滤后的方法列表
     * @throws SecurityException 安全异常
     */
    public static Method[] getMethods(Class<?> clazz, Predicate<Method> filter) throws SecurityException {
        if (null == clazz) {
            return null;
        }

        final Method[] methods = getMethods(clazz);
        if (null == filter) {
            return methods;
        }

        final List<Method> methodList = new ArrayList<>();
        for (Method method : methods) {
            if (filter.test(method)) {
                methodList.add(method);
            }
        }
        return methodList.toArray(new Method[methodList.size()]);
    }

    /**
     * 获得一个类中所有方法列表,包括其父类中的方法
     *
     * @param beanClass 类
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getMethods(Class<?> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
        return METHODS_CACHE.computeIfAbsent(beanClass,
                key -> getMethods(beanClass, false, Objects.equals(Object.class, beanClass)));
    }

    /**
     * 获得一个类中所有方法列表，直接反射获取，无缓存
     * 接口获取方法和默认方法，获取的方法包括：
     * <ul>
     *     <li>本类中的所有方法（包括static方法）</li>
     *     <li>父类中的所有方法（包括static方法）</li>
     *     <li>Object中（包括static方法）</li>
     * </ul>
     *
     * @param beanClass            类或接口
     * @param withSupers           是否包括父类或接口的方法列表
     * @param withMethodFromObject 是否包括Object中的方法
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getMethods(final Class<?> beanClass, final boolean withSupers, final boolean withMethodFromObject) throws SecurityException {
        Assert.notNull(beanClass);

        if (beanClass.isInterface()) {
            // 对于接口，直接调用Class.getMethods方法获取所有方法，因为接口都是public方法
            return withSupers ? beanClass.getMethods() : beanClass.getDeclaredMethods();
        }

        final UniqueKeySet<String, Method> result = new UniqueKeySet<>(true, ReflectKit::getUniqueKey);
        Class<?> searchType = beanClass;
        while (searchType != null) {
            if (false == withMethodFromObject && Object.class == searchType) {
                break;
            }
            result.addAllIfAbsent(Arrays.asList(searchType.getDeclaredMethods()));
            result.addAllIfAbsent(getDefaultMethodsFromInterface(searchType));


            searchType = (withSupers && false == searchType.isInterface()) ? searchType.getSuperclass() : null;
        }

        return result.toArray(new Method[0]);
    }

    /**
     * 是否为equals方法
     *
     * @param method 方法
     * @return 是否为equals方法
     */
    public static boolean isEqualsMethod(Method method) {
        if (method == null ||
                1 != method.getParameterCount() ||
                false == Normal.EQUALS.equals(method.getName())) {
            return false;
        }
        return (method.getParameterTypes()[0] == Object.class);
    }

    /**
     * 是否为hashCode方法
     *
     * @param method 方法
     * @return 是否为hashCode方法
     */
    public static boolean isHashCodeMethod(Method method) {
        return (null != method && ObjectKit.equals(method.getName(), Normal.HASHCODE) && method.getParameterTypes().length == 0);
    }

    /**
     * 是否为toString方法
     *
     * @param method 方法
     * @return 是否为toString方法
     */
    public static boolean isToStringMethod(Method method) {
        return (null != method && ObjectKit.equals(method.getName(), Normal.TOSTRING) && method.getParameterTypes().length == 0);
    }

    /**
     * 是否为无参数方法
     *
     * @param method 方法
     * @return 是否为无参数方法
     */
    public static boolean isEmptyParam(Method method) {
        return method.getParameterCount() == 0;
    }

    /**
     * 检查给定方法是否为Getter或者Setter方法，规则为：
     * <ul>
     *     <li>方法参数必须为0个或1个</li>
     *     <li>如果是无参方法，则判断是否以“get”或“is”开头</li>
     *     <li>如果方法参数1个，则判断是否以“set”开头</li>
     * </ul>
     *
     * @param method 方法
     * @return 是否为Getter或者Setter方法
     */
    public static boolean isGetterOrSetterIgnoreCase(Method method) {
        return isGetterOrSetter(method, true);
    }

    /**
     * 检查给定方法是否为Getter或者Setter方法，规则为：
     * <ul>
     *     <li>方法参数必须为0个或1个</li>
     *     <li>方法名称不能是getClass</li>
     *     <li>如果是无参方法，则判断是否以“get”或“is”开头</li>
     *     <li>如果方法参数1个，则判断是否以“set”开头</li>
     * </ul>
     *
     * @param method     方法
     * @param ignoreCase 是否忽略方法名的大小写
     * @return 是否为Getter或者Setter方法
     */
    public static boolean isGetterOrSetter(Method method, boolean ignoreCase) {
        if (null == method) {
            return false;
        }

        // 参数个数必须为0或1
        final int parameterCount = method.getParameterCount();
        if (parameterCount > 1) {
            return false;
        }

        String name = method.getName();
        // 跳过getClass这个特殊方法
        if ("getClass".equals(name)) {
            return false;
        }
        if (ignoreCase) {
            name = name.toLowerCase();
        }
        switch (parameterCount) {
            case 0:
                return name.startsWith(Normal.GET) || name.startsWith(Normal.IS);
            case 1:
                return name.startsWith(Normal.SET);
            default:
                return false;
        }
    }

    /**
     * 实例化对象
     *
     * @param <T>   对象类型
     * @param clazz 类名
     * @return 对象
     * @throws InternalException 包装各类异常
     */
    public static <T> T newInstance(String clazz) throws InternalException {
        try {
            return (T) Class.forName(clazz).getConstructor().newInstance();
        } catch (Exception e) {
            throw new InternalException(StringKit.format("Instance class [{}] error!", clazz), e);
        }
    }

    /**
     * 实例化对象
     *
     * @param <T>    对象类型
     * @param clazz  类
     * @param params 构造函数参数
     * @return 对象
     * @throws InternalException 包装各类异常
     */
    public static <T> T newInstance(Class<T> clazz, Object... params) throws InternalException {
        if (ArrayKit.isEmpty(params)) {
            final Constructor<T> constructor = getConstructor(clazz);
            if (null == constructor) {
                throw new InternalException("No constructor for [{}]", clazz);
            }
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new InternalException(e, "Instance class [{}] error!", clazz);
            }
        }

        final Class<?>[] paramTypes = ClassKit.getClasses(params);
        final Constructor<T> constructor = getConstructor(clazz, paramTypes);
        if (null == constructor) {
            throw new InternalException("No Constructor matched for parameter types: [{}]", new Object[]{paramTypes});
        }
        try {
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new InternalException(e, "Instance class [{}] error!", clazz);
        }
    }

    /**
     * 尝试遍历并调用此类的所有构造方法,直到构造成功并返回
     *
     * @param <T>  对象类型
     * @param type 被构造的类
     * @return 构造后的对象, 构造失败返回{@code null}
     */
    public static <T> T newInstanceIfPossible(Class<T> type) {
        Assert.notNull(type);

        // 原始类型
        if (type.isPrimitive()) {
            return (T) ClassKit.getPrimitiveDefaultValue(type);
        }

        // 某些特殊接口的实例化按照默认实现进行
        if (type.isAssignableFrom(AbstractMap.class)) {
            type = (Class<T>) HashMap.class;
        } else if (type.isAssignableFrom(List.class)) {
            type = (Class<T>) ArrayList.class;
        } else if (type.isAssignableFrom(Set.class)) {
            type = (Class<T>) HashSet.class;
        }

        try {
            return newInstance(type);
        } catch (Exception e) {
            // 默认构造不存在的情况下查找其它构造
        }

        // 枚举
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }

        // 数组
        if (type.isArray()) {
            return (T) Array.newInstance(type.getComponentType(), 0);
        }

        final Constructor<T>[] constructors = getConstructors(type);
        Class<?>[] parameterTypes;
        for (Constructor<T> constructor : constructors) {
            parameterTypes = constructor.getParameterTypes();
            if (0 == parameterTypes.length) {
                continue;
            }
            setAccessible(constructor);
            try {
                return constructor.newInstance(ClassKit.getDefaultValues(parameterTypes));
            } catch (Exception ignore) {
                // 构造出错时继续尝试下一种构造方式
            }
        }
        return null;
    }

    /**
     * 执行静态方法
     *
     * @param <T>    对象类型
     * @param method 方法(对象方法或static方法都可)
     * @param args   参数对象
     * @return 结果
     * @throws InternalException 多种异常包装
     */
    public static <T> T invokeStatic(Method method, Object... args) throws InternalException {
        return invoke(null, method, args);
    }

    /**
     * 执行方法
     * 执行前要检查给定参数：
     *
     * <pre>
     * 1. 参数个数是否与方法参数个数一致
     * 2. 如果某个参数为null但是方法这个位置的参数为原始类型,则赋予原始类型默认值
     * </pre>
     *
     * @param <T>    返回对象类型
     * @param object 对象,如果执行静态方法,此值为null
     * @param method 方法(对象方法或static方法都可)
     * @param args   参数对象
     * @return 结果
     * @throws InternalException 一些列异常的包装
     */
    public static <T> T invokeWithCheck(Object object, Method method, Object... args) throws InternalException {
        final Class<?>[] types = method.getParameterTypes();
        if (null != types && null != args) {
            Assert.isTrue(args.length == types.length, "Params length [{}] is not fit for param length [{}] of method !", args.length, types.length);
            Class<?> type;
            for (int i = 0; i < args.length; i++) {
                type = types[i];
                if (type.isPrimitive() && null == args[i]) {
                    // 参数是原始类型,而传入参数为null时赋予默认值
                    args[i] = ClassKit.getDefaultValue(type);
                }
            }
        }
        return invoke(object, method, args);
    }

    /**
     * 执行方法
     *
     * @param <T>    返回对象类型
     * @param object 对象,如果执行静态方法,此值为null
     * @param method 方法(对象方法或static方法都可)
     * @param args   参数对象
     * @return 结果
     */
    public static <T> T invoke(Object object, Method method, Object... args) {
        setAccessible(method);

        // 检查用户传入参数：
        // 1、忽略多余的参数
        // 2、参数不够补齐默认值
        // 3、传入参数为null，但是目标参数类型为原始类型，做转换
        // 4、传入参数类型不对应，尝试转换类型
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] actualArgs = new Object[parameterTypes.length];
        if (null != args) {
            for (int i = 0; i < actualArgs.length; i++) {
                if (i >= args.length || null == args[i]) {
                    // 越界或者空值
                    actualArgs[i] = ClassKit.getDefaultValue(parameterTypes[i]);
                } else if (false == parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    //对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                    final Object targetValue = Convert.convertQuietly(parameterTypes[i], args[i], args[i]);
                    if (null != targetValue) {
                        actualArgs[i] = targetValue;
                    }
                } else {
                    actualArgs[i] = args[i];
                }
            }
        }

        try {
            return (T) method.invoke(ClassKit.isStatic(method) ? null : object, actualArgs);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 执行对象中指定方法
     *
     * @param <T>        返回对象类型
     * @param object     方法所在对象
     * @param methodName 方法名
     * @param args       参数列表
     * @return 执行结果
     */
    public static <T> T invoke(Object object, String methodName, Object... args) {
        final Method method = getMethodOfObject(object, methodName, args);
        if (null == method) {
            throw new InternalException(StringKit.format("No such method: [{}]", methodName));
        }
        return invoke(object, method, args);
    }

    /**
     * 获取字段的get函数
     *
     * @param fieldName fieldName
     * @return 返回结果
     */
    public static String getGetMethodName(String fieldName) {
        return Normal.GET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 根据字段名获取set方法
     *
     * @param fieldName 字段名
     * @return 返回结果
     */
    public static String getSetMethodName(String fieldName) {
        return Normal.SET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 设置方法为可访问(私有方法可以被外部调用)
     *
     * @param <T>              AccessibleObject的子类,比如Class、Method、Field等
     * @param accessibleObject 可设置访问权限的对象,比如Class、Method、Field等
     * @return 被设置可访问的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T accessibleObject) {
        if (null != accessibleObject && false == accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }


    /**
     * 获取jvm定义的Field Descriptors（字段描述）
     *
     * @param executable 可执行的反射对象
     * @return 描述符
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code getDescriptor(Object.class.getMethod("hashCode"))                                                           // "()I"}</li>
     *     <li>{@code getDescriptor(Object.class.getMethod("toString"))                                                           // "()Ljava/lang/String;"}</li>
     *     <li>{@code getDescriptor(Object.class.getMethod("equals", Object.class))                                               // "(Ljava/lang/Object;)Z"}</li>
     *     <li>{@code getDescriptor(ReflectKit.class.getDeclaredMethod("appendDescriptor", Class.clas, StringBuilder.class))     // "(Ljava/lang/Class;Ljava/lang/StringBuilder;)V"}</li>
     *     <li>{@code getDescriptor(ArrayKit.class.getMethod("isEmpty", Object[].class))                                         // "([Ljava/lang/Object;)Z"}</li>
     * </ul>
     */
    public static String getDescriptor(final Executable executable) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('(');
        final Class<?>[] parameters = executable.getParameterTypes();
        for (final Class<?> parameter : parameters) {
            stringBuilder.append(getDescriptor(parameter));
        }
        if (executable instanceof Method) {
            final Method method = (Method) executable;
            return stringBuilder.append(')').append(getDescriptor(method.getReturnType())).toString();
        } else if (executable instanceof Constructor) {
            return stringBuilder.append(")V").toString();
        }
        throw new IllegalArgumentException("Unknown Executable: " + executable);
    }

    /**
     * 获取类型描述符，这是编译成class文件后的二进制名称
     *
     * @param clazz 类
     * @return 描述字符串
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code getDescriptor(boolean.class)                        "Z"}</li>
     *     <li>{@code getDescriptor(Boolean.class)                        "Ljava/lang/Boolean;"}</li>
     *     <li>{@code getDescriptor(double[][][].class)                   "[[[D"}</li>
     *     <li>{@code getDescriptor(int.class)                            "I"}</li>
     *     <li>{@code getDescriptor(Integer.class)                        "Ljava/lang/Integer;"}</li>
     * </ul>
     */
    public static String getDescriptor(final Class<?> clazz) {
        final StringBuilder stringBuilder = new StringBuilder();
        Class<?> currentClass;
        for (currentClass = clazz;
             currentClass.isArray();
             currentClass = currentClass.getComponentType()) {
            // 如果当前是数组描述符
            stringBuilder.append('[');
        }
        if (currentClass.isPrimitive()) {
            // 只有下面九种基础数据类型以及数组，才有独立的描述符
            final char descriptor;
            // see sun.invoke.util.XWrapper
            // These must be in the order defined for widening primitive conversions in JLS 5.1.2
            if (currentClass == boolean.class) {
                descriptor = 'Z';
            } else if (currentClass == byte.class) {
                descriptor = 'B';
            } else if (currentClass == short.class) {
                descriptor = 'S';
            } else if (currentClass == char.class) {
                descriptor = 'C';
            } else if (currentClass == int.class) {
                descriptor = 'I';
            } else if (currentClass == long.class) {
                descriptor = 'J';
            } else if (currentClass == float.class) {
                descriptor = 'F';
            } else if (currentClass == double.class) {
                descriptor = 'D';
            } else if (currentClass == void.class) {
                // VOID must be the last type, since it is "assignable" from any other type:
                descriptor = 'V';
            } else {
                throw new AssertionError();
            }
            stringBuilder.append(descriptor);
        } else {
            // 否则一律是 "L"+类名.replace('.', '/')+";"格式的对象类型
            stringBuilder.append('L').append(currentClass.getName().replace('.', '/')).append(';');
        }
        return stringBuilder.toString();
    }

    /**
     * 检查用户传入参数：
     * <ul>
     *     <li>1、忽略多余的参数</li>
     *     <li>2、参数不够补齐默认值</li>
     *     <li>3、通过NullWrapperBean传递的参数,会直接赋值null</li>
     *     <li>4、传入参数为null，但是目标参数类型为原始类型，做转换</li>
     *     <li>5、传入参数类型不对应，尝试转换类型</li>
     * </ul>
     *
     * @param method 方法
     * @param args   参数
     * @return 实际的参数数组
     */
    private static Object[] actualArgs(final Method method, final Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] actualArgs = new Object[parameterTypes.length];
        if (null != args) {
            for (int i = 0; i < actualArgs.length; i++) {
                if (i >= args.length || null == args[i]) {
                    // 越界或者空值
                    actualArgs[i] = ClassKit.getDefaultValue(parameterTypes[i]);
                } else if (args[i] instanceof NullWrapper) {
                    //如果是通过NullWrapperBean传递的null参数,直接赋值null
                    actualArgs[i] = null;
                } else if (false == parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    //对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                    final Object targetValue = Convert.convert(parameterTypes[i], args[i]);
                    if (null != targetValue) {
                        actualArgs[i] = targetValue;
                    }
                } else {
                    actualArgs[i] = args[i];
                }
            }
        }

        return actualArgs;
    }

    /**
     * 获取方法的唯一键，结构为:
     * <pre>
     *     返回类型#方法名:参数1类型,参数2类型...
     * </pre>
     *
     * @param method 方法
     * @return 方法唯一键
     */
    private static String getUniqueKey(Method method) {
        final StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append('#');
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(Symbol.C_COLON);
            } else {
                sb.append(Symbol.C_COMMA);
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    /**
     * 获取类对应接口中的非抽象方法（default方法）
     *
     * @param clazz 类
     * @return 方法列表
     */
    private static List<Method> getDefaultMethodsFromInterface(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method m : ifc.getMethods()) {
                if (false == ClassKit.isAbstract(m)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

}
