/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.bean.BeanDesc;
import org.aoju.bus.core.bean.copier.BeanCopier;
import org.aoju.bus.core.bean.copier.CopyOptions;
import org.aoju.bus.core.bean.copier.ValueProvider;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.convert.BasicType;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Editor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.SimpleCache;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.mutable.MutableObject;
import org.aoju.bus.core.loader.JarClassLoader;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * 类工具类
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class ClassUtils {

    /**
     * 原始类型名和其class对应表，例如：int =》 int.class
     */
    private static final Map<String, Class<?>> primitiveWrapperMap = new HashMap<>();
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();
    private static SimpleCache<String, Class<?>> classCache = new SimpleCache<>();

    static {
        List<Class<?>> primitiveTypes = new ArrayList<Class<?>>(32);
        // 加入原始类型
        primitiveTypes.addAll(BasicType.primitiveWrapperMap.keySet());
        // 加入原始类型数组类型
        primitiveTypes.add(boolean[].class);
        primitiveTypes.add(byte[].class);
        primitiveTypes.add(char[].class);
        primitiveTypes.add(double[].class);
        primitiveTypes.add(float[].class);
        primitiveTypes.add(int[].class);
        primitiveTypes.add(long[].class);
        primitiveTypes.add(short[].class);
        primitiveTypes.add(void.class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveWrapperMap.put(primitiveType.getName(), primitiveType);
        }
    }

    /**
     * {@code null}安全的获取对象类型
     *
     * @param <T> 对象类型
     * @param obj 对象，如果为{@code null} 返回{@code null}
     * @return 对象类型，提供对象如果为{@code null} 返回{@code null}
     */
    public static <T> Class<T> getClass(T obj) {
        return ((null == obj) ? null : (Class<T>) obj.getClass());
    }

    /**
     * 获取类名
     *
     * @param obj      获取类名对象
     * @param isSimple 是否简单类名，如果为true，返回不带包名的类名
     * @return 类名
     * @since 3.0.7
     */
    public static String getClassName(Object obj, boolean isSimple) {
        if (null == obj) {
            return null;
        }
        final Class<?> clazz = obj.getClass();
        return getClassName(clazz, isSimple);
    }

    /**
     * 获取类名
     * 类名并不包含“.class”这个扩展名
     * 例如：ClassUtil这个类
     *
     * <pre>
     * isSimple为false: "org.aoju.core.utils.ClassUtils"
     * isSimple为true: "ClassUtils"
     * </pre>
     *
     * @param clazz    类
     * @param isSimple 是否简单类名，如果为true，返回不带包名的类名
     * @return 类名
     * @since 3.0.7
     */
    public static String getClassName(Class<?> clazz, boolean isSimple) {
        if (null == clazz) {
            return null;
        }
        return isSimple ? clazz.getSimpleName() : clazz.getName();
    }

    /**
     * 获得对象数组的类数组
     *
     * @param objects 对象数组，如果数组中存在{@code null}元素，则此元素被认为是Object类型
     * @return 类数组
     */
    public static Class<?>[] getClasses(Object... objects) {
        Class<?>[] classes = new Class<?>[objects.length];
        Object obj;
        for (int i = 0; i < objects.length; i++) {
            obj = objects[i];
            classes[i] = (null == obj) ? Object.class : obj.getClass();
        }
        return classes;
    }

    /**
     * 指定类是否与给定的类名相同
     *
     * @param clazz      类
     * @param className  类名，可以是全类名（包含包名），也可以是简单类名（不包含包名）
     * @param ignoreCase 是否忽略大小写
     * @return 指定类是否与给定的类名相同
     * @since 3.0.7
     */
    public static boolean equals(Class<?> clazz, String className, boolean ignoreCase) {
        if (null == clazz || StringUtils.isBlank(className)) {
            return false;
        }
        if (ignoreCase) {
            return className.equalsIgnoreCase(clazz.getName()) || className.equalsIgnoreCase(clazz.getSimpleName());
        } else {
            return className.equals(clazz.getName()) || className.equals(clazz.getSimpleName());
        }
    }

    /**
     * 获得指定类中的Public方法名
     * 去重重载的方法
     *
     * @param clazz 类
     * @return 方法名Set
     */
    public static Set<String> getPublicMethodNames(Class<?> clazz) {
        HashSet<String> methodSet = new HashSet<String>();
        Method[] methodArray = getPublicMethods(clazz);
        for (Method method : methodArray) {
            String methodName = method.getName();
            methodSet.add(methodName);
        }
        return methodSet;
    }

    /**
     * 获得本类及其父类所有Public方法
     *
     * @param clazz 查找方法的类
     * @return 过滤后的方法列表
     */
    public static Method[] getPublicMethods(Class<?> clazz) {
        return clazz.getMethods();
    }

    /**
     * 获得指定类过滤后的Public方法列表
     *
     * @param clazz  查找方法的类
     * @param filter 过滤器
     * @return 过滤后的方法列表
     */
    public static List<Method> getPublicMethods(Class<?> clazz, Filter<Method> filter) {
        if (null == clazz) {
            return null;
        }

        Method[] methods = getPublicMethods(clazz);
        List<Method> methodList;
        if (null != filter) {
            methodList = new ArrayList<>();
            for (Method method : methods) {
                if (filter.accept(method)) {
                    methodList.add(method);
                }
            }
        } else {
            methodList = CollUtils.newArrayList(methods);
        }
        return methodList;
    }

    /**
     * 获得指定类过滤后的Public方法列表
     *
     * @param clazz          查找方法的类
     * @param excludeMethods 不包括的方法
     * @return 过滤后的方法列表
     */
    public static List<Method> getPublicMethods(Class<?> clazz, Method... excludeMethods) {
        final HashSet<Method> excludeMethodSet = CollUtils.newHashSet(excludeMethods);
        return getPublicMethods(clazz, new Filter<Method>() {
            @Override
            public boolean accept(Method method) {
                return false == excludeMethodSet.contains(method);
            }
        });
    }

    /**
     * 获得指定类过滤后的Public方法列表
     *
     * @param clazz              查找方法的类
     * @param excludeMethodNames 不包括的方法名列表
     * @return 过滤后的方法列表
     */
    public static List<Method> getPublicMethods(Class<?> clazz, String... excludeMethodNames) {
        final HashSet<String> excludeMethodNameSet = CollUtils.newHashSet(excludeMethodNames);
        return getPublicMethods(clazz, new Filter<Method>() {
            @Override
            public boolean accept(Method method) {
                return false == excludeMethodNameSet.contains(method.getName());
            }
        });
    }

    /**
     * 查找指定Public方法 如果找不到对应的方法或方法不为public的则返回null
     *
     * @param clazz      类
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getPublicMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * 获得指定类中的Public方法名
     * 去重重载的方法
     *
     * @param clazz 类
     * @return 方法名Set
     */
    public static Set<String> getDeclaredMethodNames(Class<?> clazz) {
        return ReflectUtils.getMethodNames(clazz);
    }

    /**
     * 获得声明的所有方法，包括本类及其父类和接口的所有方法和Object类的方法
     *
     * @param clazz 类
     * @return 方法数组
     */
    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return ReflectUtils.getMethods(clazz);
    }

    /**
     * 查找指定对象中的所有方法（包括非public方法），也包括父对象和Object类的方法
     *
     * @param obj        被查找的对象
     * @param methodName 方法名
     * @param args       参数
     * @return 方法
     * @throws SecurityException 无访问权限抛出异常
     */
    public static Method getDeclaredMethodOfObj(Object obj, String methodName, Object... args) throws SecurityException {
        return getDeclaredMethod(obj.getClass(), methodName, getClasses(args));
    }

    /**
     * 查找指定类中的所有方法（包括非public方法），也包括父类和Object类的方法 找不到方法会返回null
     *
     * @param clazz          被查找的类
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @return 方法
     * @throws SecurityException 无访问权限抛出异常
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws SecurityException {
        return ReflectUtils.getMethod(clazz, methodName, parameterTypes);
    }

    /**
     * 查找指定类中的所有字段（包括非public字段）， 字段不存在则返回null
     *
     * @param clazz     被查找字段的类
     * @param fieldName 字段名
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) throws SecurityException {
        if (null == clazz || StringUtils.isBlank(fieldName)) {
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 查找指定类中的所有字段（包括非public字段)
     *
     * @param clazz 被查找字段的类
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field[] getDeclaredFields(Class<?> clazz) throws SecurityException {
        if (null == clazz) {
            return null;
        }
        return clazz.getDeclaredFields();
    }

    /**
     * @return 获得Java ClassPath路径，不包括 jre
     */
    public static String[] getJavaClassPaths() {
        String[] classPaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        return classPaths;
    }

    /**
     * 比较判断types1和types2两组类，如果types1中所有的类都与types2对应位置的类相同，或者是其父类或接口，则返回true
     *
     * @param types1 类组1
     * @param types2 类组2
     * @return 是否相同、父类或接口
     */
    public static boolean isAllAssignableFrom(Class<?>[] types1, Class<?>[] types2) {
        if (ArrayUtils.isEmpty(types1) && ArrayUtils.isEmpty(types2)) {
            return true;
        }
        if (types1.length != types2.length) {
            return false;
        }

        Class<?> type1;
        Class<?> type2;
        for (int i = 0; i < types1.length; i++) {
            type1 = types1[i];
            type2 = types2[i];
            if (isBasicType(type1) && isBasicType(type2)) {
                //原始类型和包装类型存在不一致情况
                if (BasicType.unWrap(type1) != BasicType.unWrap(type2)) {
                    return false;
                }
            } else if (false == type1.isAssignableFrom(type2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否为包装类型
     *
     * @param clazz 类
     * @return 是否为包装类型
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return BasicType.wrapperPrimitiveMap.containsKey(clazz);
    }

    /**
     * 是否为基本类型（包括包装类和原始类）
     *
     * @param clazz 类
     * @return 是否为基本类型
     */
    public static boolean isBasicType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * 是否简单值类型或简单值类型的数组
     * 包括：原始类型,、String、other CharSequence, a Number, a Date, a URI, a URL, a Locale or a Class及其数组
     *
     * @param clazz 属性类
     * @return 是否简单值类型或简单值类型的数组
     */
    public static boolean isSimpleTypeOrArray(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    /**
     * 是否为简单值类型
     * 包括：原始类型,、String、other CharSequence, a Number, a Date, a URI, a URL, a Locale or a Class.
     *
     * @param clazz 类
     * @return 是否为简单值类型
     */
    public static boolean isSimpleValueType(Class<?> clazz) {
        return isBasicType(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || clazz
                .equals(URI.class) || clazz.equals(URL.class) || clazz.equals(Locale.class) || clazz.equals(Class.class);
    }

    /**
     * 检查目标类是否可以从原类转化
     * 转化包括：
     * 1、原类是对象，目标类型是原类型实现的接口
     * 2、目标类型是原类型的父类
     * 3、两者是原始类型或者包装类型（相互转换）
     *
     * @param classArray   目标类型
     * @param toClassArray 原类型
     * @return 是否可转化
     */
    public static boolean isAssignable(final Class<?>[] classArray, final Class<?>... toClassArray) {
        return isAssignable(classArray, toClassArray, true);
    }

    /**
     * 检查目标类是否可以从原类转化
     * 转化包括：
     * 1、原类是对象，目标类型是原类型实现的接口
     * 2、目标类型是原类型的父类
     * 3、两者是原始类型或者包装类型（相互转换）
     *
     * @param classArray   目标类型
     * @param toClassArray 原类型
     * @return 是否可转化
     */
    public static boolean isAssignable(final Class<?> classArray, final Class<?> toClassArray) {
        return isAssignable(classArray, toClassArray, true);
    }

    /**
     * 检查目标类是否可以从原类转化
     * 转化包括：
     * 1、原类是对象，目标类型是原类型实现的接口
     * 2、目标类型是原类型的父类
     * 3、两者是原始类型或者包装类型（相互转换）
     *
     * @param classArray   目标类型
     * @param toClassArray 原类型
     * @param autoboxing   自动操作
     * @return 是否可转化
     */
    public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, final boolean autoboxing) {
        if (!ArrayUtils.isSameLength(classArray, toClassArray)) {
            return false;
        }
        if (classArray == null) {
            classArray = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if (toClassArray == null) {
            toClassArray = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        for (int i = 0; i < classArray.length; i++) {
            if (!isAssignable(classArray[i], toClassArray[i], autoboxing)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAssignable(Class<?> cls, final Class<?> toClass, final boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        //autoboxing:
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass)
                        || Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    /**
     * 指定类是否为Public
     *
     * @param clazz 类
     * @return 是否为public
     */
    public static boolean isPublic(Class<?> clazz) {
        if (null == clazz) {
            throw new NullPointerException("Class to provided is null.");
        }
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * 指定方法是否为Public
     *
     * @param method 方法
     * @return 是否为public
     */
    public static boolean isPublic(Method method) {
        if (null == method) {
            throw new NullPointerException("Method to provided is null.");
        }
        return isPublic(method.getDeclaringClass());
    }

    /**
     * 指定类是否为非public
     *
     * @param clazz 类
     * @return 是否为非public
     */
    public static boolean isNotPublic(Class<?> clazz) {
        return false == isPublic(clazz);
    }

    /**
     * 指定方法是否为非public
     *
     * @param method 方法
     * @return 是否为非public
     */
    public static boolean isNotPublic(Method method) {
        return false == isPublic(method);
    }

    /**
     * 是否为静态方法
     *
     * @param method 方法
     * @return 是否为静态方法
     */
    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 设置方法为可访问
     *
     * @param method 方法
     * @return 方法
     */
    public static Method setAccessible(Method method) {
        if (null != method && false == method.isAccessible()) {
            method.setAccessible(true);
        }
        return method;
    }

    /**
     * 是否为抽象类
     *
     * @param clazz 类
     * @return 是否为抽象类
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是否为标准的类
     * 这个类必须：
     * <pre>
     * 1、非接口
     * 2、非抽象类
     * 3、非Enum枚举
     * 4、非数组
     * 5、非注解
     * 6、非原始类型（int, long等）
     * </pre>
     *
     * @param clazz 类
     * @return 是否为标准类
     */
    public static boolean isNormalClass(Class<?> clazz) {
        return null != clazz //
                && false == clazz.isInterface() //
                && false == isAbstract(clazz) //
                && false == clazz.isEnum() //
                && false == clazz.isArray() //
                && false == clazz.isAnnotation() //
                && false == clazz.isSynthetic() //
                && false == clazz.isPrimitive();//
    }

    /**
     * 判断类是否为枚举类型
     *
     * @param clazz 类
     * @return 是否为枚举类型
     * @since 3.2.0
     */
    public static boolean isEnum(Class<?> clazz) {
        return null != clazz && clazz.isEnum();
    }

    /**
     * 获得给定类的第一个泛型参数
     *
     * @param clazz 被检查的类，必须是已经确定泛型类型的类
     * @return {@link Class}
     */
    public static Class<?> getTypeArgument(Class<?> clazz) {
        return getTypeArgument(clazz, 0);
    }

    /**
     * 获得给定类的泛型参数
     *
     * @param clazz 被检查的类，必须是已经确定泛型类型的类
     * @param index 泛型类型的索引号，既第几个泛型类型
     * @return {@link Class}
     */
    public static Class<?> getTypeArgument(Class<?> clazz, int index) {
        final Type argumentType = TypeUtils.getTypeArgument(clazz, index);
        if (null != argumentType && argumentType instanceof Class) {
            return (Class<?>) argumentType;
        }
        return null;
    }

    /**
     * 获得给定类所在包的名称
     * 例如：
     * ClassUtils =》 org.aoju.bus.core.utils
     *
     * @param clazz 类
     * @return 包名
     */
    public static String getPackage(Class<?> clazz) {
        if (clazz == null) {
            return Normal.EMPTY;
        }
        final String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(Symbol.DOT);
        if (packageEndIndex == -1) {
            return Normal.EMPTY;
        }
        return className.substring(0, packageEndIndex);
    }

    /**
     * 获得给定类所在包的路径
     * 例如：
     *
     * @param clazz 类
     * @return 包名
     */
    public static String getPackagePath(Class<?> clazz) {
        return getPackage(clazz).replace(Symbol.C_DOT, Symbol.C_SLASH);
    }

    /**
     * 获取指定类型分的默认值
     * 默认值规则为：
     * <pre>
     * 1、如果为原始类型，返回0
     * 2、非原始类型返回{@code null}
     * </pre>
     *
     * @param clazz 类
     * @return 默认值
     * @since 3.0.8
     */
    public static Object getDefaultValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (long.class == clazz) {
                return 0L;
            } else if (int.class == clazz) {
                return 0;
            } else if (short.class == clazz) {
                return (short) 0;
            } else if (char.class == clazz) {
                return (char) 0;
            } else if (byte.class == clazz) {
                return (byte) 0;
            } else if (double.class == clazz) {
                return 0D;
            } else if (float.class == clazz) {
                return 0f;
            } else if (boolean.class == clazz) {
                return false;
            }
        }

        return null;
    }

    /**
     * 获得默认值列表
     *
     * @param classes 值类型
     * @return 默认值列表
     * @since 3.0.9
     */
    public static Object[] getDefaultValues(Class<?>... classes) {
        final Object[] values = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            values[i] = getDefaultValue(classes[i]);
        }
        return values;
    }

    /**
     * 判断是否为Bean对象
     * 判定方法是是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean isBean(Class<?> clazz) {
        if (isNormalClass(clazz)) {
            final Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1 && method.getName().startsWith("set")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 对象转Map，不进行驼峰转下划线，不忽略值为空的字段
     *
     * @param bean bean对象
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        return beanToMap(bean, false, false);
    }

    /**
     * 对象转Map
     *
     * @param bean              bean对象
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean, boolean isToUnderlineCase, boolean ignoreNullValue) {
        return beanToMap(bean, new HashMap<String, Object>(), isToUnderlineCase, ignoreNullValue);
    }

    /**
     * 对象转Map
     *
     * @param bean              bean对象
     * @param targetMap         目标的Map
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return Map
     * @since 3.2.3
     */
    public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, final boolean isToUnderlineCase, boolean ignoreNullValue) {
        if (bean == null) {
            return null;
        }

        return beanToMap(bean, targetMap, ignoreNullValue, new Editor<String>() {

            @Override
            public String edit(String key) {
                return isToUnderlineCase ? StringUtils.toUnderlineCase(key) : key;
            }
        });
    }

    /**
     * 对象转Map
     * 通过实现{@link Editor} 可以自定义字段值，如果这个Editor返回null则忽略这个字段，以便实现：
     *
     * <pre>
     * 1. 字段筛选，可以去除不需要的字段
     * 2. 字段变换，例如实现驼峰转下划线
     * 3. 自定义字段前缀或后缀等等
     * </pre>
     *
     * @param bean            bean对象
     * @param targetMap       目标的Map
     * @param ignoreNullValue 是否忽略值为空的字段
     * @param keyEditor       属性字段（Map的key）编辑器，用于筛选、编辑key
     * @return Map
     * @since 4.0.5
     */
    public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, boolean ignoreNullValue, Editor<String> keyEditor) {
        if (bean == null) {
            return null;
        }

        final Collection<BeanDesc.PropDesc> props = getBeanDesc(bean.getClass()).getProps();

        String key;
        Method getter;
        Object value;
        for (BeanDesc.PropDesc prop : props) {
            key = prop.getFieldName();
            // 过滤class属性
            // 得到property对应的getter方法
            getter = prop.getGetter();
            if (null != getter) {
                // 只读取有getter方法的属性
                try {
                    value = getter.invoke(bean);
                } catch (Exception ignore) {
                    continue;
                }
                if (false == ignoreNullValue || (null != value && false == value.equals(bean))) {
                    key = keyEditor.edit(key);
                    if (null != key) {
                        targetMap.put(key, value);
                    }
                }
            }
        }
        return targetMap;
    }

    /**
     * 获取{@link BeanDesc} Bean描述信息
     *
     * @param clazz Bean类
     * @return the object
     */
    public static BeanDesc getBeanDesc(Class<?> clazz) {
        return new BeanDesc(clazz);
    }

    /**
     * 获取{@link ClassLoader}
     * 获取顺序如下：
     *
     * <pre>
     * 1、获取当前线程的ContextClassLoader
     * 2、获取{@link ClassUtils}类对应的ClassLoader
     * 3、获取系统ClassLoader（{@link ClassLoader#getSystemClassLoader()}）
     * </pre>
     *
     * @return 类加载器
     */
    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassUtils.class.getClassLoader();
            if (null == classLoader) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
        }
        return classLoader;
    }

    /**
     * 获得ClassPath，将编码后的中文路径解码为原字符
     * 这个ClassPath路径会文件路径被标准化处理
     *
     * @return ClassPath
     */
    public static String getClassPath() {
        return getClassPath(false);
    }

    /**
     * 获得ClassPath，这个ClassPath路径会文件路径被标准化处理
     *
     * @param isEncoded 是否编码路径中的中文
     * @return ClassPath
     */
    public static String getClassPath(boolean isEncoded) {
        final URL classPathURL = getClassPathURL();
        String url = isEncoded ? classPathURL.getPath() : URLUtils.getDecodedPath(classPathURL);
        return FileUtils.normalize(url);
    }

    /**
     * 获得ClassPath URL
     *
     * @return ClassPath URL
     */
    public static URL getClassPathURL() {
        return getResourceURL(Normal.EMPTY);
    }

    /**
     * 获得资源的URL
     * 路径用/分隔，例如:
     *
     * <pre>
     * config/a/db.config
     * spring/xml/test.xml
     * </pre>
     *
     * @param resource 资源（相对Classpath的路径）
     * @return 资源URL
     * @see ResourceUtils#getResource(String)
     */
    public static URL getResourceURL(String resource) throws CommonException {
        return ResourceUtils.getResource(resource);
    }

    /**
     * 填充Bean的核心方法
     *
     * @param <T>           Bean类型
     * @param bean          Bean
     * @param valueProvider 值提供者
     * @param copyOptions   拷贝选项，见 {@link CopyOptions}
     * @return Bean
     */
    public static <T> T fillBean(T bean, ValueProvider<String> valueProvider, CopyOptions copyOptions) {
        if (null == valueProvider) {
            return bean;
        }
        return BeanCopier.create(valueProvider, bean, copyOptions).copy();
    }

    /**
     * 获取当前线程的{@link ClassLoader}
     *
     * @return 当前线程的class loader
     * @see Thread#getContextClassLoader()
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载类，通过传入类的字符串，返回其对应的类名，使用默认ClassLoader并初始化类（调用static模块内容和初始化static属性）
     * 扩展{@link Class#forName(String, boolean, ClassLoader)}方法，支持以下几类类名的加载：
     *
     * <pre>
     * 1、原始类型，例如：int
     * 2、数组类型，例如：int[]、Long[]、String[]
     * 3、内部类，例如：java.lang.Thread.State会被转为java.lang.Thread$State加载
     * </pre>
     *
     * @param name 类名
     * @return 类名对应的类
     * @throws CommonException 包装{@link CommonException}，没有类名对应的类时抛出此异常
     */
    public static Class<?> loadClass(String name) throws CommonException {
        return loadClass(name, true);
    }

    /**
     * 加载类，通过传入类的字符串，返回其对应的类名，使用默认ClassLoader
     * 扩展{@link Class#forName(String, boolean, ClassLoader)}方法，支持以下几类类名的加载：
     *
     * <pre>
     * 1、原始类型，例如：int
     * 2、数组类型，例如：int[]、Long[]、String[]
     * 3、内部类，例如：java.lang.Thread.State会被转为java.lang.Thread$State加载
     * </pre>
     *
     * @param name          类名
     * @param isInitialized 是否初始化类（调用static模块内容和初始化static属性）
     * @return 类名对应的类
     * @throws CommonException 包装{@link CommonException}，没有类名对应的类时抛出此异常
     */
    public static Class<?> loadClass(String name, boolean isInitialized) throws CommonException {
        return loadClass(name, null, isInitialized);
    }

    /**
     * 加载类，通过传入类的字符串，返回其对应的类名
     * 此方法支持缓存，第一次被加载的类之后会读取缓存中的类
     * 加载失败的原因可能是此类不存在或其关联引用类不存在
     * 扩展{@link Class#forName(String, boolean, ClassLoader)}方法，支持以下几类类名的加载：
     *
     * <pre>
     * 1、原始类型，例如：int
     * 2、数组类型，例如：int[]、Long[]、String[]
     * 3、内部类，例如：java.lang.Thread.State会被转为java.lang.Thread$State加载
     * </pre>
     *
     * @param name          类名
     * @param classLoader   {@link ClassLoader}，{@code null} 则使用系统默认ClassLoader
     * @param isInitialized 是否初始化类（调用static模块内容和初始化static属性）
     * @return 类名对应的类
     * @throws CommonException 包装{@link CommonException}，没有类名对应的类时抛出此异常
     */
    public static Class<?> loadClass(String name, ClassLoader classLoader, boolean isInitialized) throws CommonException {
        Assert.notNull(name, "Name must not be null");

        // 加载原始类型和缓存中的类
        Class<?> clazz = loadPrimitiveClass(name);
        if (clazz == null) {
            clazz = classCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        if (name.endsWith(Symbol.BRACKET)) {
            // 对象数组"java.lang.String[]"风格
            final String elementClassName = name.substring(0, name.length() - Symbol.BRACKET.length());
            final Class<?> elementClass = loadClass(elementClassName, classLoader, isInitialized);
            clazz = Array.newInstance(elementClass, 0).getClass();
        } else if (name.startsWith(Symbol.NON_PREFIX) && name.endsWith(";")) {
            // "[Ljava.lang.String;" 风格
            final String elementName = name.substring(Symbol.NON_PREFIX.length(), name.length() - 1);
            final Class<?> elementClass = loadClass(elementName, classLoader, isInitialized);
            clazz = Array.newInstance(elementClass, 0).getClass();
        } else if (name.startsWith(Symbol.BRACKET_LEFT)) {
            // "[[I" 或 "[[Ljava.lang.String;" 风格
            final String elementName = name.substring(Symbol.BRACKET_LEFT.length());
            final Class<?> elementClass = loadClass(elementName, classLoader, isInitialized);
            clazz = Array.newInstance(elementClass, 0).getClass();
        } else {
            // 加载普通类
            if (null == classLoader) {
                classLoader = getClassLoader();
            }
            try {
                clazz = Class.forName(name, isInitialized, classLoader);
            } catch (ClassNotFoundException ex) {
                // 尝试获取内部类，例如java.lang.Thread.State =》java.lang.Thread$State
                clazz = tryLoadInnerClass(name, classLoader, isInitialized);
                if (null == clazz) {
                    throw new CommonException(ex);
                }
            }
        }

        // 加入缓存并返回
        return classCache.put(name, clazz);
    }

    /**
     * 加载原始类型的类。包括原始类型、原始类型数组和void
     *
     * @param name 原始类型名，比如 int
     * @return 原始类型类
     */
    public static Class<?> loadPrimitiveClass(String name) {
        Class<?> result = null;
        if (StringUtils.isNotBlank(name)) {
            name = name.trim();
            if (name.length() <= 8) {
                result = primitiveWrapperMap.get(name);
            }
        }
        return result;
    }

    /**
     * 创建新的{@link JarClassLoader}，并使用此Classloader加载目录下的class文件和jar文件
     *
     * @param jarOrDir jar文件或者包含jar和class文件的目录
     * @return {@link JarClassLoader}
     * @since 4.4.2
     */
    public static JarClassLoader getJarClassLoader(File jarOrDir) {
        return JarClassLoader.load(jarOrDir);
    }

    /**
     * 加载外部类
     *
     * @param jarOrDir jar文件或者包含jar和class文件的目录
     * @param name     类名
     * @return 类
     * @since 4.4.2
     */
    public static Class<?> loadClass(File jarOrDir, String name) {
        try {
            return getJarClassLoader(jarOrDir).loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 指定类是否被提供，使用默认ClassLoader
     * 通过调用{@link #loadClass(String, ClassLoader, boolean)}方法尝试加载指定类名的类，如果加载失败返回false
     * 加载失败的原因可能是此类不存在或其关联引用类不存在
     *
     * @param className 类名
     * @return 是否被提供
     */
    public static boolean isPresent(String className) {
        return isPresent(className, null);
    }

    /**
     * 指定类是否被提供
     * 通过调用{@link #loadClass(String, ClassLoader, boolean)}方法尝试加载指定类名的类，如果加载失败返回false
     * 加载失败的原因可能是此类不存在或其关联引用类不存在
     *
     * @param className   类名
     * @param classLoader {@link ClassLoader}
     * @return 是否被提供
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            loadClass(className, classLoader, false);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * 尝试转换并加载内部类，例如java.lang.Thread.State =》java.lang.Thread$State
     *
     * @param name          类名
     * @param classLoader   {@link ClassLoader}，{@code null} 则使用系统默认ClassLoader
     * @param isInitialized 是否初始化类（调用static模块内容和初始化static属性）
     * @return 类名对应的类
     * @since 4.1.20
     */
    private static Class<?> tryLoadInnerClass(String name, ClassLoader classLoader, boolean isInitialized) {
        // 尝试获取内部类，例如java.lang.Thread.State =》java.lang.Thread$State
        final int lastDotIndex = name.lastIndexOf(Symbol.C_DOT);
        if (lastDotIndex > 0) {// 类与内部类的分隔符不能在第一位，因此>0
            final String innerClassName = name.substring(0, lastDotIndex) + Symbol.C_DOLLAR + name.substring(lastDotIndex + 1);
            try {
                return Class.forName(innerClassName, isInitialized, classLoader);
            } catch (ClassNotFoundException ex2) {
                // 尝试获取内部类失败时，忽略之。
            }
        }
        return null;
    }

    /**
     * Determine the name of the package of the given class,
     * e.g. "java.lang" for the {@code java.lang.String} class.
     *
     * @param clazz the class
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     *
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(String fqClassName) {
        Assert.notNull(fqClassName, "Class name must not be null");
        int lastDotIndex = fqClassName.lastIndexOf(Symbol.C_DOT);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }

    /**
     * Replacement for {@code Class.forName()} that also returns Class instances
     * for primitives (e.g. "int") and array class names (e.g. "String[]").
     * Furthermore, it is also capable of resolving inner class names in Java source
     * style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
     *
     * @param name        the name of the Class
     * @param classLoader the class loader to use
     *                    (may be {@code null}, which indicates the default class loader)
     * @return a class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError           if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader classLoader)
            throws ClassNotFoundException, LinkageError {

        Assert.notNull(name, "Name must not be null");

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(Symbol.BRACKET)) {
            String elementClassName = name.substring(0, name.length() - Symbol.BRACKET.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(Symbol.NON_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(Symbol.NON_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(Symbol.BRACKET_LEFT)) {
            String elementName = name.substring(Symbol.BRACKET_LEFT.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader clToUse = classLoader;
        if (clToUse == null) {
            clToUse = getDefaultClassLoader();
        }
        try {
            return Class.forName(name, false, clToUse);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(Symbol.C_DOT);
            if (lastDotIndex != -1) {
                String innerClassName =
                        name.substring(0, lastDotIndex) + Symbol.C_DOLLAR + name.substring(lastDotIndex + 1);
                try {
                    return Class.forName(innerClassName, false, clToUse);
                } catch (ClassNotFoundException ex2) {
                    // Swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = primitiveWrapperMap.get(name);
        }
        return result;
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    /**
     * <p>Gets the class name minus the package name for an {@code Object}.</p>
     *
     * @param object      the class to get the short name for, may be null
     * @param valueIfNull the value to return if null
     * @return the class name of the object without the package name, or the null value
     */
    public static String getShortClassName(final Object object, final String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return getShortClassName(object.getClass());
    }

    /**
     * <p>Gets the class name minus the package name from a {@code Class}.</p>
     *
     * <p>Consider using the Java 5 API {@link Class#getSimpleName()} instead.
     * The one known difference is that this code will return {@code "Map.Entry"} while
     * the {@code java.lang.Class} variant will simply return {@code "Entry"}. </p>
     *
     * @param cls the class to get the short name for.
     * @return the class name without the package name or an empty string
     */
    public static String getShortClassName(final Class<?> cls) {
        if (cls == null) {
            return Normal.EMPTY;
        }
        return getShortClassName(cls.getName());
    }

    /**
     * <p>Gets the class name minus the package name from a String.</p>
     *
     * <p>The string passed in is assumed to be a class name - it is not checked.</p>
     *
     * <p>Note that this method differs from Class.getSimpleName() in that this will
     * return {@code "Map.Entry"} whilst the {@code java.lang.Class} variant will simply
     * return {@code "Entry"}. </p>
     *
     * @param className the className to get the short name for
     * @return the class name of the class without the package name or an empty string
     */
    public static String getShortClassName(String className) {
        if (StringUtils.isEmpty(className)) {
            return Normal.EMPTY;
        }

        final StringBuilder arrayPrefix = new StringBuilder();

        // Handle array encoding
        if (className.startsWith("[")) {
            while (className.charAt(0) == '[') {
                className = className.substring(1);
                arrayPrefix.append("[]");
            }
            // Strip Object type encoding
            if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
                className = className.substring(1, className.length() - 1);
            }

            if (primitiveWrapperMap.containsKey(className)) {
                className = primitiveWrapperMap.get(className).toString();
            }
        }

        final int lastDotIdx = className.lastIndexOf(Symbol.C_DOT);
        final int innerIdx = className.indexOf(
                Symbol.C_DOLLAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
        String out = className.substring(lastDotIdx + 1);
        if (innerIdx != -1) {
            out = out.replace(Symbol.C_DOLLAR, Symbol.C_DOT);
        }
        return out + arrayPrefix;
    }

    /**
     * <p>Null-safe version of <code>aClass.getSimpleName()</code></p>
     *
     * @param cls the class for which to get the simple name; may be null
     * @return the simple class name.
     * @see Class#getSimpleName()
     * @since 3.0
     */
    public static String getSimpleName(final Class<?> cls) {
        return getSimpleName(cls, Normal.EMPTY);
    }

    /**
     * <p>Null-safe version of <code>aClass.getSimpleName()</code></p>
     *
     * @param cls         the class for which to get the simple name; may be null
     * @param valueIfNull the value to return if null
     * @return the simple class name or {@code valueIfNull}
     * @see Class#getSimpleName()
     * @since 3.0
     */
    public static String getSimpleName(final Class<?> cls, String valueIfNull) {
        return cls == null ? valueIfNull : cls.getSimpleName();
    }

    /**
     * <p>Null-safe version of <code>aClass.getSimpleName()</code></p>
     *
     * @param object the object for which to get the simple class name; may be null
     * @return the simple class name or the empty String
     * @see Class#getSimpleName()
     * @since 3.7
     */
    public static String getSimpleName(final Object object) {
        return getSimpleName(object, Normal.EMPTY);
    }

    /**
     * <p>Null-safe version of <code>aClass.getSimpleName()</code></p>
     *
     * @param object      the object for which to get the simple class name; may be null
     * @param valueIfNull the value to return if <code>object</code> is <code>null</code>
     * @return the simple class name or {@code valueIfNull}
     * @see Class#getSimpleName()
     * @since 3.0
     */
    public static String getSimpleName(final Object object, final String valueIfNull) {
        return object == null ? valueIfNull : object.getClass().getSimpleName();
    }

    /**
     * <p>Converts the specified primitive Class object to its corresponding
     * wrapper Class object.</p>
     *
     * <p>NOTE: From v2.2, this method handles {@code Void.TYPE},
     * returning {@code Void.TYPE}.</p>
     *
     * @param cls the class to convert, may be null
     * @return the wrapper class for {@code cls} or {@code cls} if
     * {@code cls} is not a primitive. {@code null} if null input.
     * @since 2.1
     */
    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    /**
     * <p>Converts the specified array of primitive Class objects to an array of
     * its corresponding wrapper Class objects.</p>
     *
     * @param classes the class array to convert, may be null or empty
     * @return an array which contains for each given class, the wrapper class or
     * the original class if class is not a primitive. {@code null} if null input.
     * Empty array if an empty array passed in.
     * @since 2.1
     */
    public static Class<?>[] primitivesToWrappers(final Class<?>... classes) {
        if (classes == null) {
            return null;
        }

        if (classes.length == 0) {
            return classes;
        }

        final Class<?>[] convertedClasses = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            convertedClasses[i] = primitiveToWrapper(classes[i]);
        }
        return convertedClasses;
    }

    /**
     * <p>Converts the specified wrapper class to its corresponding primitive
     * class.</p>
     *
     * <p>This method is the counter part of {@code primitiveToWrapper()}.
     * If the passed in class is a wrapper class for a primitive type, this
     * primitive type will be returned (e.g. {@code Integer.TYPE} for
     * {@code Integer.class}). For other classes, or if the parameter is
     * <b>null</b>, the return value is <b>null</b>.</p>
     *
     * @param cls the class to convert, may be <b>null</b>
     * @return the corresponding primitive type if {@code cls} is a
     * wrapper class, <b>null</b> otherwise
     * @see #primitiveToWrapper(Class)
     * @since 2.4
     */
    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        return wrapperPrimitiveMap.get(cls);
    }

    /**
     * <p>Converts the specified array of wrapper Class objects to an array of
     * its corresponding primitive Class objects.</p>
     *
     * <p>This method invokes {@code wrapperToPrimitive()} for each element
     * of the passed in array.</p>
     *
     * @param classes the class array to convert, may be null or empty
     * @return an array which contains for each given class, the primitive class or
     * <b>null</b> if the original class is not a wrapper class. {@code null} if null input.
     * Empty array if an empty array passed in.
     * @see #wrapperToPrimitive(Class)
     * @since 2.4
     */
    public static Class<?>[] wrappersToPrimitives(final Class<?>... classes) {
        if (classes == null) {
            return null;
        }

        if (classes.length == 0) {
            return classes;
        }

        final Class<?>[] convertedClasses = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            convertedClasses[i] = wrapperToPrimitive(classes[i]);
        }
        return convertedClasses;
    }

    /**
     * <p>Gets a {@code List} of all interfaces implemented by the given
     * class and its superclasses.</p>
     *
     * <p>The order is determined by looking through each interface in turn as
     * declared in the source file and following its hierarchy up. Then each
     * superclass is considered in the same way. Later duplicates are ignored,
     * so the order is maintained.</p>
     *
     * @param cls the class to look up, may be {@code null}
     * @return the {@code List} of interfaces in order,
     * {@code null} if null input
     */
    public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
        getAllInterfaces(cls, interfacesFound);

        return new ArrayList<>(interfacesFound);
    }

    /**
     * Get the interfaces for the specified class.
     *
     * @param cls             the class to look up, may be {@code null}
     * @param interfacesFound the {@code Set} of interfaces for the class
     */
    public static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
        while (cls != null) {
            final Class<?>[] interfaces = cls.getInterfaces();

            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(i, interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    /**
     * <p>Gets a {@code List} of superclasses for the given class.</p>
     *
     * @param cls the class to look up, may be {@code null}
     * @return the {@code List} of superclasses in order going up from this one
     * {@code null} if null input
     */
    public static List<Class<?>> getAllSuperclasses(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static Class<?>[] toClass(final Object... array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        final Class<?>[] classes = new Class[array.length];
        for (int i = 0; i < array.length; i++) {
            classes[i] = array[i] == null ? null : array[i].getClass();
        }
        return classes;
    }

    /**
     * Get an {@link Iterable} that can iterate over a class hierarchy in ascending (subclass to superclass) order,
     * excluding interfaces.
     *
     * @param type the type to get the class hierarchy from
     * @return Iterable an Iterable over the class hierarchy of the given class
     * @since 3.2
     */
    public static Iterable<Class<?>> hierarchy(final Class<?> type) {
        return hierarchy(type, Interfaces.EXCLUDE);
    }

    /**
     * Get an {@link Iterable} that can iterate over a class hierarchy in ascending (subclass to superclass) order.
     *
     * @param type               the type to get the class hierarchy from
     * @param interfacesBehavior switch indicating whether to include or exclude interfaces
     * @return Iterable an Iterable over the class hierarchy of the given class
     * @since 3.2
     */
    public static Iterable<Class<?>> hierarchy(final Class<?> type, final Interfaces interfacesBehavior) {
        final Iterable<Class<?>> classes = new Iterable<Class<?>>() {

            @Override
            public Iterator<Class<?>> iterator() {
                final MutableObject<Class<?>> next = new MutableObject<Class<?>>(type);
                return new Iterator<Class<?>>() {

                    @Override
                    public boolean hasNext() {
                        return next.get() != null;
                    }

                    @Override
                    public Class<?> next() {
                        final Class<?> result = next.get();
                        next.set(result.getSuperclass());
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }

        };
        if (interfacesBehavior != Interfaces.INCLUDE) {
            return classes;
        }
        return new Iterable<Class<?>>() {

            @Override
            public Iterator<Class<?>> iterator() {
                final Set<Class<?>> seenInterfaces = new HashSet<>();
                final Iterator<Class<?>> wrapped = classes.iterator();

                return new Iterator<Class<?>>() {
                    Iterator<Class<?>> interfaces = Collections.emptyIterator();

                    @Override
                    public boolean hasNext() {
                        return interfaces.hasNext() || wrapped.hasNext();
                    }

                    @Override
                    public Class<?> next() {
                        if (interfaces.hasNext()) {
                            final Class<?> nextInterface = interfaces.next();
                            seenInterfaces.add(nextInterface);
                            return nextInterface;
                        }
                        final Class<?> nextSuperclass = wrapped.next();
                        final Set<Class<?>> currentInterfaces = new LinkedHashSet<>();
                        walkInterfaces(currentInterfaces, nextSuperclass);
                        interfaces = currentInterfaces.iterator();
                        return nextSuperclass;
                    }

                    private void walkInterfaces(final Set<Class<?>> addTo, final Class<?> c) {
                        for (final Class<?> iface : c.getInterfaces()) {
                            if (!seenInterfaces.contains(iface)) {
                                addTo.add(iface);
                            }
                            walkInterfaces(addTo, iface);
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }
        };
    }

    /**
     * Returns whether the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
     * {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     *
     * @param type The class to query or null.
     * @return true if the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
     * {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     * @since 3.1
     */
    public static boolean isPrimitiveOrWrapper(final Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isPrimitive() || isPrimitiveWrapper(type);
    }

    public static boolean isUserLevelMethod(Method method) {
        Assert.notNull(method, "Method must not be null");
        return (method.isBridge() || (!method.isSynthetic() && !isGroovyObjectMethod(method)));
    }

    private static boolean isGroovyObjectMethod(Method method) {
        return method.getDeclaringClass().getName().equals("groovy.lang.GroovyObject");
    }

    /**
     * Convert a "."-based fully qualified class name to a "/"-based resource path.
     *
     * @param className the fully qualified class name
     * @return the corresponding resource path, pointing to the class
     */
    public static String convertClassNameToResourcePath(String className) {
        Assert.notNull(className, "Class name must not be null");
        return className.replace(Symbol.C_DOT, Symbol.C_SLASH);
    }

    /**
     * 获取对应类的默认变量名：
     * 1. 首字母小写
     * String=》string
     *
     * @param className 类名称
     * @return 类的默认变量名
     */
    public static String getClassVar(final String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * 获取类所有的字段信息
     * ps: 这个方法有个问题 如果子类和父类有相同的字段 会不会重复
     * 1. 还会获取到 serialVersionUID 这个字段。
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getAllFieldList(final Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = clazz;
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }

        for (Field field : fieldList) {
            field.setAccessible(true);
        }
        return fieldList;
    }

    /**
     * 获取对象的实例化
     *
     * @param clazz 类
     * @param <T>   泛型
     * @return 实例化对象
     */
    public static <T> T newInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有字段的 read 方法列表
     *
     * @param clazz 类信息
     * @return 方法列表
     * @throws IntrospectionException if any
     * @since 0.0.7
     */
    public static List<Method> getAllFieldsReadMethods(final Class clazz) throws IntrospectionException {
        List<Field> fieldList = getAllFieldList(clazz);
        if (CollUtils.isEmpty(fieldList)) {
            return Collections.emptyList();
        }

        List<Method> methods = new ArrayList<>();
        for (Field field : fieldList) {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
            //获得get方法
            Method getMethod = pd.getReadMethod();
            methods.add(getMethod);
        }
        return methods;
    }

    public enum Interfaces {
        INCLUDE, EXCLUDE
    }

}
