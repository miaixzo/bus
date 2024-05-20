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

import org.miaixz.bus.core.center.map.TripleTable;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.creator.DefaultObjectCreator;
import org.miaixz.bus.core.lang.reflect.creator.PossibleObjectCreator;
import org.miaixz.bus.core.text.StringTrimer;

import java.lang.reflect.*;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * 反射相关工具类
 *
 * <p>
 * 本工具类进行了重构，大部分被移动到了{@link FieldKit}、{@link MethodKit}、{@link ModifierKit}等中，
 * 其他相关方法请参考<strong>org.miaixz.bus.core.lang.reflect</strong>包下的类,相关类
 * </p>
 * <ul>
 *     <li>反射修改属性</li>
 *     <li>{@code ReflectKit#setFieldValue(Object, String, Object)} --p {@link FieldKit#setFieldValue(Object, String, Object)}</li>
 *     <li>修改private修饰可被外部访问</li>
 *     <li>{@code ReflectKit.setAccessible(ReflectKit.getMethodByName(Xxx.class, "xxxMethodName"))} --p {@link ReflectKit#setAccessible(AccessibleObject)} --p {@link MethodKit#getMethodByName(Class, String)} </li>
 *     <li>移除final属性</li>
 *     <li>{@code ReflectKit.removeFinalModify(Field)} --p {@link  ModifierKit#removeFinalModify(Field)}</li>
 * </ul>
 * <p>
 * 在字节码中，类型表示如下：
 * <ul>
 *     <li>byte    =  B</li>
 *     <li>char    =  C</li>
 *     <li>double  =  D</li>
 *     <li>long    =  J</li>
 *     <li>short   =  S</li>
 *     <li>boolean =  Z</li>
 *     <li>void    =  V</li>
 *     <li>对象类型以“L”开头，“;”结尾，如Ljava/lang/Object;</li>
 *     <li>数组类型，每一位使用一个前置的[字符来描述，如：java.lang.String[][] = [[Ljava/lang/String;</li>
 * </ul>
 *
 * <p>此类旨在通过类描述信息和类名查找对应的类，如动态加载类等</p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectKit {

    /**
     * void(V).
     */
    public static final char JVM_VOID = 'V';

    /**
     * boolean(Z).
     */
    public static final char JVM_BOOLEAN = 'Z';

    /**
     * byte(B).
     */
    public static final char JVM_BYTE = 'B';

    /**
     * char(C).
     */
    public static final char JVM_CHAR = 'C';

    /**
     * double(D).
     */
    public static final char JVM_DOUBLE = 'D';

    /**
     * float(F).
     */
    public static final char JVM_FLOAT = 'F';

    /**
     * int(I).
     */
    public static final char JVM_INT = 'I';

    /**
     * long(J).
     */
    public static final char JVM_LONG = 'J';

    /**
     * short(S).
     */
    public static final char JVM_SHORT = 'S';

    /**
     * 9种原始类型对应表
     * <pre>
     *     左：原始类型
     *     中：原始类型描述符
     *     右：原始类型名称
     * </pre>
     */
    private static final TripleTable<Class<?>, Character, String> PRIMITIVE_TABLE = new TripleTable<>(9);
    /**
     * 构造对象缓存
     */
    private static final WeakConcurrentMap<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new WeakConcurrentMap<>();

    static {
        PRIMITIVE_TABLE.put(void.class, JVM_VOID, "void");
        PRIMITIVE_TABLE.put(boolean.class, JVM_BOOLEAN, "boolean");
        PRIMITIVE_TABLE.put(byte.class, JVM_BYTE, "byte");
        PRIMITIVE_TABLE.put(char.class, JVM_CHAR, "char");
        PRIMITIVE_TABLE.put(double.class, JVM_DOUBLE, "double");
        PRIMITIVE_TABLE.put(float.class, JVM_FLOAT, "float");
        PRIMITIVE_TABLE.put(int.class, JVM_INT, "int");
        PRIMITIVE_TABLE.put(long.class, JVM_LONG, "long");
        PRIMITIVE_TABLE.put(short.class, JVM_SHORT, "short");
    }

    /**
     * Class描述转Class
     * <pre>{@code
     * "[Z" => boolean[].class
     * "[[Ljava/util/Map;" => java.util.Map[][].class
     * }</pre>
     *
     * @param desc 类描述
     * @return Class
     * @throws InternalException 类没有找到
     */
    public static Class<?> descToClass(final String desc) throws InternalException {
        return descToClass(desc, true, null);
    }

    /**
     * Class描述转Class
     * <pre>{@code
     * "[Z" => boolean[].class
     * "[[Ljava/util/Map;" => java.util.Map[][].class
     * }</pre>
     *
     * @param desc          类描述
     * @param isInitialized 是否初始化类
     * @param cl            {@link ClassLoader}
     * @return Class
     * @throws InternalException 类没有找到
     */
    public static Class<?> descToClass(String desc, final boolean isInitialized, final ClassLoader cl) throws InternalException {
        Assert.notNull(desc, "Name must not be null");
        final char firstChar = desc.charAt(0);
        final Class<?> clazz = PRIMITIVE_TABLE.getLeftByMiddle(firstChar);
        if (null != clazz) {
            return clazz;
        }

        // 去除尾部多余的"."和"/"
        desc = StringKit.trim(desc, StringTrimer.TrimMode.SUFFIX, (c) ->
                Symbol.C_SLASH == c || Symbol.C_DOT == c);

        if ('L' == firstChar) {
            // 正常类的描述中需要去掉L;包装的修饰
            // "Ljava/lang/Object;" ==> "java.lang.Object"
            desc = desc.substring(1, desc.length() - 1);
        }

        return ClassKit.forName(desc, isInitialized, cl);
    }

    /**
     * 获取类描述，这是编译成class文件后的二进制名称
     * <pre>{@code
     *    getDesc(boolean.class)       // Z
     *    getDesc(Boolean.class)       // Ljava/lang/Boolean;
     *    getDesc(double[][][].class)  // [[[D
     *    getDesc(int.class)           // I
     *    getDesc(Integer.class)       // Ljava/lang/Integer;
     * }</pre>
     *
     * @param c class.
     * @return desc.
     */
    public static String getDesc(Class<?> c) {
        final StringBuilder ret = new StringBuilder();

        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }

        if (c.isPrimitive()) {
            final Character desc = PRIMITIVE_TABLE.getMiddleByLeft(c);
            if (null != desc) {
                ret.append(desc.charValue());
            }
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }

    /**
     * 获取方法或构造描述
     * 方法（appendName为{@code true}）：
     * <pre>{@code
     *    int do(int arg1) => "do(I)I"
     *    void do(String arg1,boolean arg2) => "do(Ljava/lang/String;Z)V"
     * }</pre>
     * 构造：
     * <pre>{@code
     *    "()V", "(Ljava/lang/String;I)V"
     * }</pre>
     *
     * <p>当appendName为{@code false}时：</p>
     * <pre>{@code
     *    getDesc(Object.class.getMethod("hashCode"))                    // ()I
     *    getDesc(Object.class.getMethod("toString"))                    // ()Ljava/lang/String;
     *    getDesc(Object.class.getMethod("equals", Object.class))        // (Ljava/lang/Object;)Z
     *    getDesc(ArrayKit.class.getMethod("isEmpty", Object[].class))  // "([Ljava/lang/Object;)Z"
     * }</pre>
     *
     * @param methodOrConstructor 方法或构造
     * @param appendName          是否包含方法名称
     * @return 描述
     */
    public static String getDesc(final Executable methodOrConstructor, final boolean appendName) {
        final StringBuilder ret = new StringBuilder();
        if (appendName && methodOrConstructor instanceof Method) {
            ret.append(methodOrConstructor.getName());
        }
        ret.append('(');

        // 参数
        final Class<?>[] parameterTypes = methodOrConstructor.getParameterTypes();
        for (final Class<?> parameterType : parameterTypes) {
            ret.append(getDesc(parameterType));
        }

        // 返回类型或构造标记
        ret.append(')');
        if (methodOrConstructor instanceof Method) {
            ret.append(getDesc(((Method) methodOrConstructor).getReturnType()));
        } else {
            ret.append('V');
        }

        return ret.toString();
    }

    /**
     * 获得类名称
     * 数组输出xxx[]形式，其它类调用{@link Class#getName()}
     *
     * <pre>{@code
     * java.lang.Object[][].class => "java.lang.Object[][]"
     * }</pre>
     *
     * @param c 类
     * @return 类名称
     */
    public static String getName(Class<?> c) {
        if (c.isArray()) {
            final StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            }
            while (c.isArray());
            return c.getName() + sb;
        }
        return c.getName();
    }

    /**
     * 获取构造或方法的名称表示
     * 构造：
     * <pre>
     * "()", "(java.lang.String,int)"
     * </pre>
     * <p>
     * 方法：
     * <pre>
     *     "void do(int)", "void do()", "int do(java.lang.String,boolean)"
     * </pre>
     *
     * @param executable 方法或构造
     * @return 名称
     */
    public static String getName(final Executable executable) {
        final StringBuilder ret = new StringBuilder("(");

        if (executable instanceof Method) {
            ret.append(getName(((Method) executable).getReturnType())).append(Symbol.C_SPACE);
        }

        // 参数
        final Class<?>[] parameterTypes = executable.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                ret.append(',');
            }
            ret.append(getName(parameterTypes[i]));
        }

        ret.append(')');
        return ret.toString();
    }

    /**
     * 类名称转类
     *
     * <pre>{@code
     * "boolean" => boolean.class
     * "java.util.Map[][]" => java.util.Map[][].class
     * }</pre>
     *
     * @param name          name.
     * @param isInitialized 是否初始化类
     * @param cl            ClassLoader instance.
     * @return Class instance.
     */
    public static Class<?> nameToClass(String name, final boolean isInitialized, final ClassLoader cl) {
        Assert.notNull(name, "Name must not be null");
        // 去除尾部多余的"."和"/"
        name = StringKit.trim(name, StringTrimer.TrimMode.SUFFIX, (c) ->
                Symbol.C_SLASH == c || Symbol.C_DOT == c);

        int c = 0;
        final int index = name.indexOf('[');
        if (index > 0) {
            // c是[]对个数，如String[][]，则表示二维数组，c的值是2，获得desc结果就是[[LString;
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }

        if (c > 0) {
            final StringBuilder sb = new StringBuilder();
            while (c-- > 0) {
                sb.append('[');
            }

            final Class<?> clazz = PRIMITIVE_TABLE.getLeftByRight(name);
            if (null != clazz) {
                // 原始类型数组，根据name获取其描述
                sb.append(PRIMITIVE_TABLE.getMiddleByLeft(clazz).charValue());
            } else {
                // 对象数组必须转换为desc形式
                // "java.lang.Object" ==> "Ljava.lang.Object;"
                sb.append('L').append(name).append(';');
            }
            name = sb.toString();
        } else {
            final Class<?> clazz = PRIMITIVE_TABLE.getLeftByRight(name);
            if (null != clazz) {
                return clazz;
            }
        }

        return ClassKit.forName(name, isInitialized, cl);
    }

    /**
     * 类名称转描述
     *
     * <pre>{@code
     * java.util.Map[][] => "[[Ljava/util/Map;"
     * }</pre>
     *
     * @param name 名称
     * @return 描述
     */
    public static String nameToDesc(String name) {
        final StringBuilder sb = new StringBuilder();
        int c = 0;
        final int index = name.indexOf('[');
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        while (c-- > 0) {
            sb.append('[');
        }

        final Class<?> clazz = PRIMITIVE_TABLE.getLeftByRight(name);
        if (null != clazz) {
            // 原始类型数组，根据name获取其描述
            sb.append(PRIMITIVE_TABLE.getMiddleByLeft(clazz).charValue());
        } else {
            sb.append('L').append(name.replace(Symbol.C_DOT, Symbol.C_SLASH)).append(';');
        }

        return sb.toString();
    }

    /**
     * 类描述转名称
     *
     * <pre>{@code
     * "[[I" => "int[][]"
     * }</pre>
     *
     * @param desc 描述
     * @return 名称
     */
    public static String descToName(final String desc) {
        final StringBuilder sb = new StringBuilder();
        int c = desc.lastIndexOf('[') + 1;
        if (desc.length() == c + 1) {
            final char descChar = desc.charAt(c);
            final Class<?> clazz = PRIMITIVE_TABLE.getLeftByMiddle(descChar);
            if (null != clazz) {
                sb.append(PRIMITIVE_TABLE.getRightByLeft(clazz));
            } else {
                throw new InternalException("Unsupported primitive desc: {}", desc);
            }
        } else {
            sb.append(desc.substring(c + 1, desc.length() - 1).replace(Symbol.C_SLASH, Symbol.C_DOT));
        }
        while (c-- > 0) {
            sb.append("[]");
        }
        return sb.toString();
    }

    /**
     * 获取code base
     *
     * @param clazz 类
     * @return code base
     */
    public static String getCodeBase(final Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        final ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) {
            return null;
        }
        final CodeSource source = domain.getCodeSource();
        if (source == null) {
            return null;
        }
        final URL location = source.getLocation();
        if (location == null) {
            return null;
        }
        return location.getFile();
    }

    /**
     * 设置方法为可访问（私有方法可以被外部调用），静默调用，抛出异常则跳过
     * 注意此方法在jdk9+中抛出异常，须添加`--add-opens=java.base/java.lang=ALL-UNNAMED`启动参数
     *
     * @param <T>              AccessibleObject的子类，比如Class、Method、Field等
     * @param accessibleObject 可设置访问权限的对象，比如Class、Method、Field等
     * @return 被设置可访问的对象
     * @throws SecurityException 访问被禁止抛出此异常
     */
    public static <T extends AccessibleObject> T setAccessibleQuietly(final T accessibleObject) {
        try {
            setAccessible(accessibleObject);
        } catch (final RuntimeException ignore) {
            // ignore
        }
        return accessibleObject;
    }

    /**
     * 设置方法为可访问（私有方法可以被外部调用）
     * 注意此方法在jdk9+中抛出异常，须添加`--add-opens=java.base/java.lang=ALL-UNNAMED`启动参数
     *
     * @param <T>              AccessibleObject的子类，比如Class、Method、Field等
     * @param accessibleObject 可设置访问权限的对象，比如Class、Method、Field等
     * @return 被设置可访问的对象
     * @throws SecurityException 访问被禁止抛出此异常
     */
    public static <T extends AccessibleObject> T setAccessible(final T accessibleObject) throws SecurityException {
        if (null != accessibleObject && !accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }

    /**
     * 查找类中的指定参数的构造方法，如果找到构造方法，会自动设置可访问为true
     *
     * @param <T>            对象类型
     * @param clazz          类
     * @param parameterTypes 参数类型，只要任何一个参数是指定参数的父类或接口或相等即可，此参数可以不传
     * @return 构造方法，如果未找到返回null
     */
    public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        if (null == clazz) {
            return null;
        }

        final Constructor<?>[] constructors = getConstructors(clazz);
        Class<?>[] pts;
        for (final Constructor<?> constructor : constructors) {
            pts = constructor.getParameterTypes();
            if (ClassKit.isAllAssignableFrom(pts, parameterTypes)) {
                // 构造可访问
                ReflectKit.setAccessible(constructor);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    /**
     * 获得一个类中所有构造列表
     *
     * @param <T>       构造的对象类型
     * @param beanClass 类，非{@code null}
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static <T> Constructor<T>[] getConstructors(final Class<T> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
        return (Constructor<T>[]) CONSTRUCTORS_CACHE.computeIfAbsent(beanClass, (key) -> getConstructorsDirectly(beanClass));
    }

    /**
     * 获得一个类中所有构造列表，直接反射获取，无缓存
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Constructor<?>[] getConstructorsDirectly(final Class<?> beanClass) throws SecurityException {
        return beanClass.getDeclaredConstructors();
    }

    /**
     * 实例化对象
     * 类必须有空构造函数
     *
     * @param <T>   对象类型
     * @param clazz 类名
     * @return 对象
     * @throws InternalException 包装各类异常
     */
    public static <T> T newInstance(final String clazz) throws InternalException {
        return (T) DefaultObjectCreator.of(clazz).create();
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
    public static <T> T newInstance(final Class<T> clazz, final Object... params) throws InternalException {
        return DefaultObjectCreator.of(clazz, params).create();
    }

    /**
     * 尝试遍历并调用此类的所有构造方法，直到构造成功并返回
     * 对于某些特殊的接口，按照其默认实现实例化，例如：
     * <pre>
     *     Map       - HashMap
     *     Collction - ArrayList
     *     List      - ArrayList
     *     Set       - HashSet
     * </pre>
     *
     * @param <T>  对象类型
     * @param type 被构造的类
     * @return 构造后的对象，构造失败返回{@code null}
     */
    public static <T> T newInstanceIfPossible(final Class<T> type) {
        return PossibleObjectCreator.of(type).create();
    }

}
