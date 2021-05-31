/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.annotation.Alias;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.*;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.lang.reflect.*;
import java.util.*;

/**
 * 反射工具类.
 * 提供调用getter/setter方法, 访问私有变量, 调用私有方法, 获取泛型类型Class, 被AOP过的真实类等工具函数.
 *
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class ReflectKit {

    private static final String CGLIB_CLASS_SEPARATOR = Symbol.DOLLAR + Symbol.DOLLAR;

    /**
     * 构造对象缓存
     */
    private static final SimpleCache<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new SimpleCache<>();
    /**
     * 字段缓存
     */
    private static final SimpleCache<Class<?>, Field[]> FIELDS_CACHE = new SimpleCache<>();
    /**
     * 方法缓存
     */
    private static final SimpleCache<Class<?>, Method[]> METHODS_CACHE = new SimpleCache<>();

    /**
     * 调用Getter方法.
     * 支持多级,如：对象名.对象名.方法
     *
     * @param obj  对象
     * @param name 属性名
     * @return the object
     */
    public static Object invokeGetter(Object obj, String name) {
        Object object = obj;
        for (String method : StringKit.split(name, Symbol.DOT)) {
            String getterMethodName = Normal.GET + StringKit.capitalize(method);
            object = invokeMethod(object, getterMethodName, new Class[]{}, new Object[]{});
        }
        return object;
    }

    /**
     * 调用Setter方法, 仅匹配方法名
     * 支持多级,如：对象名.对象名.方法
     *
     * @param obj   对象
     * @param name  属性名
     * @param value 值
     */
    public static void invokeSetter(Object obj, String name, Object value) {
        Object object = obj;
        String[] names = StringKit.split(name, Symbol.DOT);
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

    public static Object convert(Object obj, Class<?> type) {
        if (obj instanceof Number) {
            Number number = (Number) obj;
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
            return null == obj ? Normal.EMPTY : obj.toString();
        }
        return obj;
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
     * @param obj   对象
     * @param name  方法名
     * @param types 参数类型
     * @param args  参数
     * @return the object
     */
    public static Object invokeMethod(final Object obj, final String name, final Class<?>[] types,
                                      final Object[] args) {
        Method method = getAccessibleMethod(obj, name, types);
        if (null == method) {
            throw new IllegalArgumentException("Could not find method [" + method + "] on target [" + obj + "]");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 直接调用对象方法, 无视private/protected修饰符,
     * 用于一次性调用的情况,否则应使用getAccessibleMethodByName()函数获得Method后反复调用.
     * 只匹配函数名,如果有多个同名函数调用第一个
     *
     * @param obj  对象
     * @param name 方法
     * @param args 参数
     * @return the object
     */
    public static Object invokeMethodByName(final Object obj, final String name, final Object[] args) {
        Method method = getAccessibleMethodByName(obj, name);
        if (null == method) {
            throw new IllegalArgumentException("Could not find method [" + name + "] on target [" + obj + "]");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
     * <p>
     * 如向上转型到Object仍无法找到, 返回null.
     *
     * @param obj  对象
     * @param name 列名
     * @return the object
     */
    public static Field getAccessibleField(final Object obj, final String name) {
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
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
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     *
     * @param obj   对象
     * @param name  方法名
     * @param types 参数类型
     * @return the object
     */
    public static Method getAccessibleMethod(final Object obj, final String name,
                                             final Class<?>... types) {
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Method method = searchType.getDeclaredMethod(name, types);
                makeAccessible(method);
                return method;
            } catch (NoSuchMethodException e) {
                // Method不在当前类定义,继续向上转型
                continue;// new add
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     * 只匹配函数名
     * <p>
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     *
     * @param obj  对象
     * @param name 方法名
     * @return the object
     */
    public static Method getAccessibleMethodByName(final Object obj, final String name) {
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
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
        if (null != clazz && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
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
     * @param obj       对象
     * @param fieldName 属性名
     * @return 有属性返回true
     * 无属性返回false
     */
    public static boolean hasField(final Object obj, final String fieldName) {
        Field field = getAccessibleField(obj, fieldName);
        return null != field;

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
        Constructor<?>[] constructors = CONSTRUCTORS_CACHE.get(beanClass);
        if (null != constructors) {
            return (Constructor<T>[]) constructors;
        }

        constructors = getConstructorsDirectly(beanClass);
        return (Constructor<T>[]) CONSTRUCTORS_CACHE.put(beanClass, constructors);
    }

    /**
     * 获得一个类中所有构造列表,直接反射获取,无缓存
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Constructor<?>[] getConstructorsDirectly(Class<?> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
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
        Field[] allFields = FIELDS_CACHE.get(beanClass);
        if (null != allFields) {
            return allFields;
        }

        allFields = getFields(beanClass, true);
        return FIELDS_CACHE.put(beanClass, allFields);
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
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws InstrumentException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, String fieldName) throws InstrumentException {
        if (null == obj || StringKit.isBlank(fieldName)) {
            return null;
        }
        return getFieldValue(obj, getField(obj instanceof Class ? (Class<?>) obj : obj.getClass(), fieldName));
    }

    /**
     * 获取字段值
     *
     * @param obj   对象
     * @param field 字段
     * @return 字段值
     * @throws InstrumentException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, Field field) throws InstrumentException {
        if (null == field) {
            return null;
        }
        if (obj instanceof Class) {
            obj = null;
        }

        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new InstrumentException("IllegalAccess for " + obj.getClass() + Symbol.DOT + field.getName());
        }
    }

    /**
     * 获取所有字段的值
     *
     * @param obj bean对象，如果是static字段，此处为类class
     * @return 字段值数组
     */
    public static Object[] getFieldsValue(Object obj) {
        if (null != obj) {
            final Field[] fields = getFields(obj instanceof Class ? (Class<?>) obj : obj.getClass());
            if (null != fields) {
                final Object[] values = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    values[i] = getFieldValue(obj, fields[i]);
                }
                return values;
            }
        }
        return null;
    }

    /**
     * 设置字段值
     *
     * @param obj       对象,static字段则此处传Class
     * @param fieldName 字段名
     * @param value     值，值类型必须与字段类型匹配，不会自动转换对象类型
     * @throws InstrumentException 包装IllegalAccessException异常
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) throws InstrumentException {
        Assert.notNull(obj);
        Assert.notBlank(fieldName);

        final Field field = getField((obj instanceof Class) ? (Class<?>) obj : obj.getClass(), fieldName);
        Assert.notNull(field, "Field [{}] is not exist in [{}]", fieldName, obj.getClass().getName());
        setFieldValue(obj, field, value);
    }

    /**
     * 设置字段值
     *
     * @param obj   对象，如果是static字段，此参数为null
     * @param field 字段
     * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
     * @throws InstrumentException UtilException 包装IllegalAccessException异常
     */
    public static void setFieldValue(Object obj, Field field, Object value) throws InstrumentException {
        Assert.notNull(field, "Field in [{}] not exist !", obj);

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
            field.set(obj instanceof Class ? null : obj, value);
        } catch (IllegalAccessException e) {
            throw new InstrumentException("IllegalAccess for {}.{}", obj, field.getName());
        }
    }

    /**
     * 查找指定对象中的所有方法(包括非public方法),也包括父对象和Object类的方法
     *
     * @param obj        被查找的对象,如果为{@code null}返回{@code null}
     * @param methodName 方法名,如果为空字符串返回{@code null}
     * @param args       参数
     * @return 方法
     * @throws SecurityException 无访问权限抛出异常
     */
    public static Method getMethodOfObj(Object obj, String methodName, Object... args) throws SecurityException {
        if (null == obj || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(obj.getClass(), methodName, ClassKit.getClasses(args));
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

        final Method[] methods = getMethods(clazz);
        if (ArrayKit.isNotEmpty(methods)) {
            for (Method method : methods) {
                if (StringKit.equals(methodName, method.getName(), ignoreCase)) {
                    if (ArrayKit.isEmpty(paramTypes) || ClassKit.isAllAssignableFrom(method.getParameterTypes(), paramTypes)) {
                        return method;
                    }
                }
            }
        }
        return null;
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
    public static Method[] getMethods(Class<?> clazz, Filter<Method> filter) throws SecurityException {
        if (null == clazz) {
            return null;
        }

        final Method[] methods = getMethods(clazz);
        if (null == filter) {
            return methods;
        }

        final List<Method> methodList = new ArrayList<>();
        for (Method method : methods) {
            if (filter.accept(method)) {
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
        Method[] allMethods = METHODS_CACHE.get(beanClass);
        if (null != allMethods) {
            return allMethods;
        }

        allMethods = getMethodsDirectly(beanClass, true);
        return METHODS_CACHE.put(beanClass, allMethods);
    }

    /**
     * 获得一个类中所有方法列表,直接反射获取,无缓存
     *
     * @param beanClass             类
     * @param withSuperClassMethods 是否包括父类的方法列表
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getMethodsDirectly(Class<?> beanClass, boolean withSuperClassMethods) throws SecurityException {
        Assert.notNull(beanClass);

        Method[] allMethods = null;
        Class<?> searchType = beanClass;
        Method[] declaredMethods;
        while (null != searchType) {
            declaredMethods = searchType.getDeclaredMethods();
            if (null == allMethods) {
                allMethods = declaredMethods;
            } else {
                allMethods = ArrayKit.append(allMethods, declaredMethods);
            }
            searchType = withSuperClassMethods ? searchType.getSuperclass() : null;
        }

        return allMethods;
    }

    /**
     * 是否为equals方法
     *
     * @param method 方法
     * @return 是否为equals方法
     */
    public static boolean isEqualsMethod(Method method) {
        if (null == method || false == ObjectKit.equal(method.getName(), Normal.EQUALS)) {
            return false;
        }
        final Class<?>[] paramTypes = method.getParameterTypes();
        return (1 == paramTypes.length && paramTypes[0] == Object.class);
    }

    /**
     * 是否为hashCode方法
     *
     * @param method 方法
     * @return 是否为hashCode方法
     */
    public static boolean isHashCodeMethod(Method method) {
        return (null != method && ObjectKit.equal(method.getName(), "hashCode") && method.getParameterTypes().length == 0);
    }

    /**
     * 是否为toString方法
     *
     * @param method 方法
     * @return 是否为toString方法
     */
    public static boolean isToStringMethod(Method method) {
        return (null != method && ObjectKit.equal(method.getName(), "toString") && method.getParameterTypes().length == 0);
    }

    /**
     * 实例化对象
     *
     * @param <T>   对象类型
     * @param clazz 类名
     * @return 对象
     * @throws InstrumentException 包装各类异常
     */
    public static <T> T newInstance(String clazz) throws InstrumentException {
        try {
            return (T) Class.forName(clazz).newInstance();
        } catch (Exception e) {
            throw new InstrumentException(StringKit.format("Instance class [{}] error!", clazz), e);
        }
    }

    /**
     * 实例化对象
     *
     * @param <T>    对象类型
     * @param clazz  类
     * @param params 构造函数参数
     * @return 对象
     * @throws InstrumentException 包装各类异常
     */
    public static <T> T newInstance(Class<T> clazz, Object... params) throws InstrumentException {
        if (ArrayKit.isEmpty(params)) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InstrumentException(StringKit.format("Instance class [{}] error!", clazz), e);
            }
        }

        final Class<?>[] paramTypes = ClassKit.getClasses(params);
        final Constructor<?> constructor = getConstructor(clazz, paramTypes);
        if (null == constructor) {
            throw new InstrumentException("No Constructor matched for parameter types: [" + new Object[]{paramTypes} + "]");
        }
        try {
            return getConstructor(clazz, paramTypes).newInstance(params);
        } catch (Exception e) {
            throw new InstrumentException(StringKit.format("Instance class [{}] error!", clazz), e);
        }
    }

    /**
     * 尝试遍历并调用此类的所有构造方法,直到构造成功并返回
     *
     * @param <T>       对象类型
     * @param beanClass 被构造的类
     * @return 构造后的对象
     */
    public static <T> T newInstanceIfPossible(Class<T> beanClass) {
        Assert.notNull(beanClass);

        if (beanClass.isAssignableFrom(AbstractMap.class)) {
            beanClass = (Class<T>) HashMap.class;
        } else if (beanClass.isAssignableFrom(List.class)) {
            beanClass = (Class<T>) ArrayList.class;
        } else if (beanClass.isAssignableFrom(Set.class)) {
            beanClass = (Class<T>) HashSet.class;
        }

        try {
            return newInstance(beanClass);
        } catch (InstrumentException e) {
            // 默认构造不存在的情况下查找其它构造
        }

        final Constructor<T>[] constructors = getConstructors(beanClass);
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
     * @throws InstrumentException 多种异常包装
     */
    public static <T> T invokeStatic(Method method, Object... args) throws InstrumentException {
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
     * @param obj    对象,如果执行静态方法,此值为null
     * @param method 方法(对象方法或static方法都可)
     * @param args   参数对象
     * @return 结果
     * @throws InstrumentException 一些列异常的包装
     */
    public static <T> T invokeWithCheck(Object obj, Method method, Object... args) throws InstrumentException {
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
        return invoke(obj, method, args);
    }

    /**
     * 执行方法
     *
     * @param <T>    返回对象类型
     * @param obj    对象,如果执行静态方法,此值为null
     * @param method 方法(对象方法或static方法都可)
     * @param args   参数对象
     * @return 结果
     */
    public static <T> T invoke(Object obj, Method method, Object... args) {
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
                    final Object targetValue = Convert.convert(parameterTypes[i], args[i]);
                    if (null != targetValue) {
                        actualArgs[i] = targetValue;
                    }
                } else {
                    actualArgs[i] = args[i];
                }
            }
        }

        try {
            return (T) method.invoke(ClassKit.isStatic(method) ? null : obj, actualArgs);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 执行对象中指定方法
     *
     * @param <T>        返回对象类型
     * @param obj        方法所在对象
     * @param methodName 方法名
     * @param args       参数列表
     * @return 执行结果
     */
    public static <T> T invoke(Object obj, String methodName, Object... args) {
        final Method method = getMethodOfObj(obj, methodName, args);
        if (null == method) {
            throw new InstrumentException(StringKit.format("No such method: [{}]", methodName));
        }
        return invoke(obj, method, args);
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

}
