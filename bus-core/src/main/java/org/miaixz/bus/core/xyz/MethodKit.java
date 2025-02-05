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
package org.miaixz.bus.core.xyz;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.bean.NullWrapper;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.lang.reflect.method.MethodReflect;

/**
 * 反射中{@link Method}相关工具类，包括方法获取和方法执行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodKit {

    /**
     * 方法缓存
     */
    private static final WeakConcurrentMap<Class<?>, MethodReflect> METHODS_CACHE = new WeakConcurrentMap<>();

    /**
     * 清除方法缓存
     */
    synchronized static void clearCache() {
        METHODS_CACHE.clear();
    }

    /**
     * 通过给定的条件（Predicate）从一个Method数组中查找第一个匹配的方法。
     *
     * @param methods   Method数组，是被搜索的目标对象。
     * @param predicate 一个Predicate接口实例，用于定义查找方法的条件。
     * @return 返回第一个满足predicate条件的Method对象，如果没有找到匹配的方法则返回null。
     */
    public static Method getMethod(final Method[] methods, final Predicate<Method> predicate) {
        // 使用ArrayKit的get方法，通过predicate对methods数组进行搜索
        return ArrayKit.get(methods, predicate);
    }

    /**
     * 获得指定类本类及其父类中的Public方法名 去重重载的方法
     *
     * @param clazz 类
     * @return 方法名Set
     */
    public static Set<String> getPublicMethodNames(final Class<?> clazz) {
        return StreamKit.of(getPublicMethods(clazz)).map(Method::getName).collect(Collectors.toSet());
    }

    /**
     * 查找指定Public方法 如果找不到对应的方法或方法不为public的则返回{@code null}
     *
     * @param clazz      类
     * @param ignoreCase 是否忽略大小写
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getPublicMethod(final Class<?> clazz, final boolean ignoreCase, final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(getPublicMethods(clazz), ignoreCase, methodName, paramTypes);
    }

    /**
     * 查找指定对象中的所有方法（包括非public方法），也包括父对象和Object类的方法
     *
     * <p>
     * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回{@code null}。
     * </p>
     *
     * @param object     被查找的对象，如果为{@code null}返回{@code null}
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @param args       参数
     * @return 方法
     * @throws SecurityException 无访问权限抛出异常
     */
    public static Method getMethodOfObject(final Object object, final String methodName, final Object... args)
            throws SecurityException {
        if (null == object || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(object.getClass(), methodName, ClassKit.getClasses(args));
    }

    /**
     * 忽略大小写查找指定方法，如果找不到对应的方法则返回{@code null}
     *
     * <p>
     * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回{@code null}。
     * </p>
     *
     * @param clazz      类，如果为{@code null}返回{@code null}
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethodIgnoreCase(final Class<?> clazz, final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        return getMethod(clazz, true, methodName, paramTypes);
    }

    /**
     * 查找指定方法 如果找不到对应的方法则返回{@code null}
     *
     * <p>
     * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回{@code null}。
     * </p>
     *
     * @param clazz      类，如果为{@code null}返回{@code null}
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... paramTypes)
            throws SecurityException {
        return getMethod(clazz, false, methodName, paramTypes);
    }

    /**
     * 查找指定方法 如果找不到对应的方法则返回{@code null} 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回{@code null}。 如果查找的方法有多个同参数类型重载，查找第一个找到的方法
     *
     * @param clazz      类，如果为{@code null}返回{@code null}
     * @param ignoreCase 是否忽略大小写
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethod(final Class<?> clazz, final boolean ignoreCase, final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(getMethods(clazz), ignoreCase, methodName, paramTypes);
    }

    /**
     * 查找指定方法 如果找不到对应的方法则返回{@code null} 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回{@code null}。 如果查找的方法有多个同参数类型重载，查找最后一个非协变桥接方法
     *
     * @param methods    方法列表
     * @param ignoreCase 是否忽略大小写
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethod(final Method[] methods, final boolean ignoreCase, final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        if (ArrayKit.isEmpty(methods) || StringKit.isBlank(methodName)) {
            return null;
        }

        Method res = null;
        if (ArrayKit.isNotEmpty(methods)) {
            for (final Method method : methods) {
                if (StringKit.equals(methodName, method.getName(), ignoreCase)
                        && ClassKit.isAllAssignableFrom(method.getParameterTypes(), paramTypes)
                        // 排除协变桥接方法
                        && (res == null || res.getReturnType().isAssignableFrom(method.getReturnType()))) {
                    res = method;
                }
            }
        }
        return res;
    }

    /**
     * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回{@code null}
     *
     * <p>
     * 此方法只检查方法名是否一致，并不检查参数的一致性。
     * </p>
     *
     * @param clazz      类，如果为{@code null}返回{@code null}
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethodByName(final Class<?> clazz, final String methodName) throws SecurityException {
        return getMethodByName(clazz, false, methodName);
    }

    /**
     * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回{@code null}
     *
     * <p>
     * 此方法只检查方法名是否一致（忽略大小写），并不检查参数的一致性。
     * </p>
     *
     * @param clazz      类，如果为{@code null}返回{@code null}
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethodByNameIgnoreCase(final Class<?> clazz, final String methodName)
            throws SecurityException {
        return getMethodByName(clazz, true, methodName);
    }

    /**
     * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回{@code null}
     *
     * <p>
     * 此方法只检查方法名是否一致，并不检查参数的一致性。
     * </p>
     *
     * @param clazz      类，如果为{@code null}返回{@code null}
     * @param ignoreCase 是否忽略大小写
     * @param methodName 方法名，如果为空字符串返回{@code null}
     * @return 方法
     * @throws SecurityException 无权访问抛出异常
     */
    public static Method getMethodByName(final Class<?> clazz, final boolean ignoreCase, final String methodName)
            throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }

        final Method[] methods = getMethods(clazz, (method -> StringKit.equals(methodName, method.getName(), ignoreCase)
                && (method.getReturnType().isAssignableFrom(method.getReturnType()))));

        return ArrayKit.isEmpty(methods) ? null : methods[0];
    }

    /**
     * 获得指定类中的Public方法名 去重重载的方法
     *
     * @param clazz 类
     * @return 方法名Set
     * @throws SecurityException 安全异常
     */
    public static Set<String> getMethodNames(final Class<?> clazz) throws SecurityException {
        return StreamKit.of(getMethods(clazz, null)).map(Method::getName).collect(Collectors.toSet());
    }

    /**
     * 获得一个类中所有方法列表，包括其父类中的方法
     *
     * @param clazz 类，非{@code null}
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getMethods(final Class<?> clazz) throws SecurityException {
        return getMethods(clazz, null);
    }

    /**
     * 获得一个类中所有方法列表，包括其父类中的方法
     *
     * @param clazz     类，非{@code null}
     * @param predicate 方法过滤器，{@code null}表示无过滤
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getMethods(final Class<?> clazz, final Predicate<Method> predicate)
            throws SecurityException {
        return METHODS_CACHE.computeIfAbsent(Assert.notNull(clazz), MethodReflect::of).getAllMethods(predicate);
    }

    /**
     * 获得本类及其父类所有Public方法
     *
     * @param clazz 查找方法的类
     * @return 过滤后的方法列表
     */
    public static Method[] getPublicMethods(final Class<?> clazz) {
        return getPublicMethods(clazz, null);
    }

    /**
     * 获得本类及其父类所有Public方法
     *
     * @param clazz     查找方法的类
     * @param predicate 方法过滤器，{@code null}表示无过滤
     * @return 过滤后的方法列表
     */
    public static Method[] getPublicMethods(final Class<?> clazz, final Predicate<Method> predicate) {
        return METHODS_CACHE.computeIfAbsent(Assert.notNull(clazz), MethodReflect::of).getPublicMethods(predicate);
    }

    /**
     * 获得类中所有直接声明方法，不包括其父类中的方法
     *
     * @param clazz 类，非{@code null}
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz) throws SecurityException {
        return getDeclaredMethods(clazz, null);
    }

    /**
     * 获得类中所有直接声明方法，不包括其父类中的方法
     *
     * @param clazz     类，非{@code null}
     * @param predicate 方法过滤器，{@code null}表示无过滤
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz, final Predicate<Method> predicate)
            throws SecurityException {
        return METHODS_CACHE.computeIfAbsent(Assert.notNull(clazz), MethodReflect::of).getDeclaredMethods(predicate);
    }

    /**
     * 获得一个类中所有方法列表，直接反射获取，无缓存 接口获取方法和默认方法，获取的方法包括：
     * <ul>
     * <li>本类中的所有方法（包括static方法）</li>
     * <li>父类中的所有方法（包括static方法）</li>
     * <li>Object中（包括static方法）</li>
     * </ul>
     *
     * @param beanClass            类或接口
     * @param withSupers           是否包括父类或接口的方法列表
     * @param withMethodFromObject 是否包括Object中的方法
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static Method[] getMethodsDirectly(final Class<?> beanClass, final boolean withSupers,
            final boolean withMethodFromObject) throws SecurityException {
        return MethodReflect.of(Assert.notNull(beanClass)).getMethodsDirectly(withSupers, withMethodFromObject);
    }

    /**
     * 是否为Object方法
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isObjectMethod(Method method) {
        return method != null && (method.getDeclaringClass() == Object.class || isEqualsMethod(method)
                || isHashCodeMethod(method) || isToStringMethod(method));
    }

    /**
     * 是否为Attribute方法
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isAttributeMethod(Method method) {
        return method != null && method.getParameterTypes().length == 0 && method.getReturnType() != void.class;
    }

    /**
     * 是否为equals方法
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isEqualsMethod(final Method method) {
        if (method == null || 1 != method.getParameterCount() || !Normal.EQUALS.equals(method.getName())) {
            return false;
        }
        return (method.getParameterTypes()[0] == Object.class);
    }

    /**
     * 是否为hashCode方法
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isHashCodeMethod(final Method method) {
        return method != null && Normal.HASHCODE.equals(method.getName()) && isEmptyParam(method);
    }

    /**
     * 是否为toString方法
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isToStringMethod(final Method method) {
        return method != null && Normal.TOSTRING.equals(method.getName()) && isEmptyParam(method);
    }

    /**
     * 是否为无参数方法
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isEmptyParam(final Method method) {
        return method.getParameterCount() == 0;
    }

    /**
     * 检查给定方法是否为Getter或者Setter方法，规则为：
     * <ul>
     * <li>方法参数必须为0个或1个</li>
     * <li>如果是无参方法，则判断是否以“get”或“is”开头</li>
     * <li>如果方法参数1个，则判断是否以“set”开头</li>
     * </ul>
     *
     * @param method 方法
     * @return the true/false
     */
    public static boolean isGetterOrSetterIgnoreCase(final Method method) {
        return isGetterOrSetter(method, true);
    }

    /**
     * 检查给定方法是否为Getter或者Setter方法，规则为：
     * <ul>
     * <li>方法参数必须为0个或1个</li>
     * <li>方法名称不能是getClass</li>
     * <li>如果是无参方法，则判断是否以“get”或“is”开头</li>
     * <li>如果方法参数1个，则判断是否以“set”开头</li>
     * </ul>
     *
     * @param method     方法
     * @param ignoreCase 是否忽略方法名的大小写
     * @return the true/false
     */
    public static boolean isGetterOrSetter(final Method method, final boolean ignoreCase) {
        // 参数个数必须为1
        final int parameterCount = method.getParameterCount();
        switch (parameterCount) {
        case 0:
            return isGetter(method, ignoreCase);
        case 1:
            return isSetter(method, ignoreCase);
        default:
            return false;
        }
    }

    /**
     * 检查给定方法是否为Setter方法，规则为：
     * <ul>
     * <li>方法参数必须为1个</li>
     * <li>判断是否以“set”开头</li>
     * </ul>
     *
     * @param method     方法
     * @param ignoreCase 是否忽略方法名的大小写
     * @return the true/false
     */
    public static boolean isSetter(final Method method, final boolean ignoreCase) {
        if (null == method) {
            return false;
        }

        // 参数个数必须为1
        final int parameterCount = method.getParameterCount();
        if (1 != parameterCount) {
            return false;
        }

        String name = method.getName();
        // 跳过set这类特殊方法
        if (name.length() < 4) {
            return false;
        }

        if (ignoreCase) {
            name = name.toLowerCase();
        }
        return name.startsWith(Normal.SET);
    }

    /**
     * 检查给定方法是否为Getter方法，规则为：
     * <ul>
     * <li>方法参数必须为0个</li>
     * <li>方法名称不能是getClass</li>
     * <li>"is"开头返回必须为boolean或Boolean</li>
     * <li>是否以“get”</li>
     * </ul>
     *
     * @param method     方法
     * @param ignoreCase 是否忽略方法名的大小写
     * @return the true/false
     */
    public static boolean isGetter(final Method method, final boolean ignoreCase) {
        if (null == method) {
            return false;
        }

        // 参数个数必须为0或1
        if (0 != method.getParameterCount()) {
            return false;
        }

        // 必须有返回值
        if (Void.class == method.getReturnType()) {
            return false;
        }

        String name = method.getName();
        // 跳过getClass、get、is这类特殊方法
        if (name.length() < 3 || "getClass".equals(name) || Normal.GET.equals(name)) {
            return false;
        }

        if (ignoreCase) {
            name = name.toLowerCase();
        }

        if (name.startsWith(Normal.IS)) {
            // 判断返回值是否为Boolean
            return BooleanKit.isBoolean(method.getReturnType());
        }
        return name.startsWith(Normal.GET);
    }

    /**
     * 执行静态方法
     *
     * @param <T>    对象类型
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws InternalException 多种异常包装
     */
    public static <T> T invokeStatic(final Method method, final Object... args) throws InternalException {
        return invoke(null, method, args);
    }

    /**
     * 执行方法 执行前要检查给定参数：
     *
     * <pre>
     * 1. 参数个数是否与方法参数个数一致
     * 2. 如果某个参数为null但是方法这个位置的参数为原始类型，则赋予原始类型默认值
     * </pre>
     *
     * @param <T>    返回对象类型
     * @param object 对象，如果执行静态方法，此值为{@code null}
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws InternalException 一些列异常的包装
     */
    public static <T> T invokeWithCheck(final Object object, final Method method, final Object... args)
            throws InternalException {
        return MethodInvoker.of(method).setCheckArgs(true).invoke(object, args);
    }

    /**
     * 执行方法
     *
     * <p>
     * 对于用户传入参数会做必要检查，包括：
     *
     * <pre>
     *     1、忽略多余的参数
     *     2、参数不够补齐默认值
     *     3、传入参数为null，但是目标参数类型为原始类型，做转换
     * </pre>
     *
     * @param <T>    返回对象类型
     * @param object 对象，如果执行静态方法，此值为{@code null}
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws InternalException 一些列异常的包装
     * @see MethodInvoker#invoke(Object, Method, Object...)
     */
    public static <T> T invoke(final Object object, final Method method, final Object... args)
            throws InternalException {
        return MethodInvoker.of(method).invoke(object, args);
    }

    /**
     * 执行对象中指定方法 如果需要传递的参数为null,请使用NullWrapperBean来传递,不然会丢失类型信息
     *
     * @param <T>        返回对象类型
     * @param object     方法所在对象
     * @param methodName 方法名
     * @param args       参数列表
     * @return 执行结果
     * @throws InternalException IllegalAccessException包装
     * @see NullWrapper
     */
    public static <T> T invoke(final Object object, final String methodName, final Object... args)
            throws InternalException {
        Assert.notNull(object, "Object to get method must be not null!");
        Assert.notBlank(methodName, "Method name must be not blank!");

        final Method method = getMethodOfObject(object, methodName, args);
        if (null == method) {
            throw new InternalException("No such method: [{}] from [{}]", methodName, object.getClass());
        }
        return invoke(object, method, args);
    }

    /**
     * 执行方法 可执行Private方法，也可执行static方法 执行非static方法时，必须满足对象有默认构造方法 非单例模式，如果是非静态方法，每次创建一个新对象
     *
     * @param <T>                     对象类型
     * @param classNameWithMethodName 类名和方法名表达式
     * @param args                    参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(final String classNameWithMethodName, final Object[] args) {
        return invoke(classNameWithMethodName, false, args);
    }

    /**
     * 执行方法 可执行Private方法，也可执行static方法 执行非static方法时，必须满足对象有默认构造方法
     *
     * @param <T>                     对象类型
     * @param classNameWithMethodName 类名和方法名表达式
     * @param isSingleton             是否为单例对象，如果此参数为false，每次执行方法时创建一个新对象
     * @param args                    参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(final String classNameWithMethodName, final boolean isSingleton, final Object... args) {
        if (StringKit.isBlank(classNameWithMethodName)) {
            throw new InternalException("Blank classNameDotMethodName!");
        }

        int splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_SHAPE);
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            throw new InternalException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        final String className = classNameWithMethodName.substring(0, splitIndex);
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);

        return invoke(className, methodName, isSingleton, args);
    }

    /**
     * 执行方法 可执行Private方法，也可执行static方法 执行非static方法时，必须满足对象有默认构造方法 非单例模式，如果是非静态方法，每次创建一个新对象
     *
     * @param <T>        对象类型
     * @param className  类名，完整类路径
     * @param methodName 方法名
     * @param args       参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(final String className, final String methodName, final Object[] args) {
        return invoke(className, methodName, false, args);
    }

    /**
     * 执行方法 可执行Private方法，也可执行static方法 执行非static方法时，必须满足对象有默认构造方法
     *
     * @param <T>         对象类型
     * @param className   类名，完整类路径
     * @param methodName  方法名
     * @param isSingleton 是否为单例对象，如果此参数为false，每次执行方法时创建一个新对象
     * @param args        参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(final String className, final String methodName, final boolean isSingleton,
            final Object... args) {
        final Class<?> clazz = ClassKit.loadClass(className);
        try {
            final Method method = getMethod(clazz, methodName, ClassKit.getClasses(args));
            if (null == method) {
                throw new NoSuchMethodException(StringKit.format("No such method: [{}]", methodName));
            }
            if (ModifierKit.isStatic(method)) {
                return invoke(null, method, args);
            } else {
                return invoke(isSingleton ? Instances.get(clazz) : ReflectKit.newInstance(clazz), method, args);
            }
        } catch (final Exception e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * 调用Getter方法. 支持多级,如：对象名.对象名.方法
     *
     * @param object 对象
     * @param name   属性名
     * @return the object
     */
    public static Object invokeGetter(Object object, String name) {
        for (String method : StringKit.splitToArray(name, Symbol.DOT)) {
            String getterMethodName = Normal.GET + StringKit.capitalize(method);
            object = invoke(object, getterMethodName, new Class[] {}, new Object[] {});
        }
        return object;
    }

    /**
     * 调用Setter方法, 仅匹配方法名 支持多级,如：对象名.对象名.方法
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
                object = invoke(object, getterMethodName, new Class[] {}, new Object[] {});
            } else {
                String setterMethodName = Normal.SET + StringKit.capitalize(names[i]);
                invoke(object, setterMethodName, value);
            }
        }
    }

    /**
     * 检查用户传入参数：
     * <ul>
     * <li>1、忽略多余的参数</li>
     * <li>2、参数不够补齐默认值</li>
     * <li>3、通过NullWrapperBean传递的参数,会直接赋值null</li>
     * <li>4、传入参数为null，但是目标参数类型为原始类型，做转换</li>
     * <li>5、传入参数类型不对应，尝试转换类型</li>
     * </ul>
     *
     * @param method 方法
     * @param args   参数
     * @return 实际的参数数组
     */
    public static Object[] actualArgs(final Method method, final Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (1 == parameterTypes.length && parameterTypes[0].isArray()) {
            // 可变长参数，不做转换
            return args;
        }
        final Object[] actualArgs = new Object[parameterTypes.length];
        if (null != args) {
            for (int i = 0; i < actualArgs.length; i++) {
                if (i >= args.length || null == args[i]) {
                    // 越界或者空值
                    actualArgs[i] = ClassKit.getDefaultValue(parameterTypes[i]);
                } else if (args[i] instanceof NullWrapper) {
                    // 如果是通过NullWrapperBean传递的null参数,直接赋值null
                    actualArgs[i] = null;
                } else if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    // 对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                    final Object targetValue = Convert.convert(parameterTypes[i], args[i], args[i]);
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
     * 获取指定{@link Executable}的{@link MethodType} 此方法主要是读取方法或构造中的方法列表，主要为：
     * <ul>
     * <li>方法：[返回类型, 参数1类型, 参数2类型, ...]</li>
     * <li>构造：[构造对应类类型, 参数1类型, 参数2类型, ...]</li>
     * </ul>
     *
     * @param executable 方法或构造
     * @return {@link MethodType}
     */
    public static MethodType methodType(final Executable executable) {
        return methodType(executable, null);
    }

    /**
     * 获取指定{@link Executable}的{@link MethodType} 此方法主要是读取方法或构造中的方法列表，主要为：
     * <ul>
     * <li>方法：[返回类型, 参数1类型, 参数2类型, ...]</li>
     * <li>构造：[构造对应类类型, 参数1类型, 参数2类型, ...]</li>
     * </ul>
     *
     * @param executable     方法或构造
     * @param declaringClass 方法或构造对应的类，用于获取其声明的参数类型，如果为{@code null}，则使用{@link Executable#getDeclaringClass()}
     * @return {@link MethodType}
     */
    public static MethodType methodType(final Executable executable, Class<?> declaringClass) {
        if (null == declaringClass) {
            declaringClass = executable.getDeclaringClass();
        }
        if (executable instanceof Method) {
            final Method method = (Method) executable;
            return MethodType.methodType(method.getReturnType(), declaringClass, method.getParameterTypes());
        } else {
            final Constructor<?> constructor = (Constructor<?>) executable;
            return MethodType.methodType(declaringClass, constructor.getParameterTypes());
        }
    }

}
