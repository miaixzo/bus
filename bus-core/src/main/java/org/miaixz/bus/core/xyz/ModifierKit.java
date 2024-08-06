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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.EnumMap;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * 类修饰符
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ModifierKit {

    /**
     * 是否同时存在一个或多个修饰符（可能有多个修饰符，如果有指定的修饰符则返回true）
     *
     * @param clazz         类，如果为{@code null}返回{@code false}
     * @param modifierTypes 修饰符枚举，如果为空返回{@code false}
     * @return 是否有指定修饰符，如果有返回true，否则false，如果提供参数为null返回false
     */
    public static boolean hasModifier(final Class<?> clazz, final EnumMap.Modifier... modifierTypes) {
        if (null == clazz || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (clazz.getModifiers() & EnumMap.Modifier.orToInt(modifierTypes));
    }

    /**
     * 是否同时存在一个或多个修饰符（可能有多个修饰符，如果有指定的修饰符则返回true）
     *
     * @param member        构造、字段或方法，如果为{@code null}返回{@code false}
     * @param modifierTypes 修饰符枚举，如果为空返回{@code false}
     * @return 是否有指定修饰符，如果有返回true，否则false，如果提供参数为null返回false
     */
    public static boolean hasModifier(final Member member, final EnumMap.Modifier... modifierTypes) {
        if (null == member || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (member.getModifiers() & EnumMap.Modifier.orToInt(modifierTypes));
    }

    /**
     * 是否同时存在一个或多个修饰符（可能有多个修饰符，如果有指定的修饰符则返回true）
     *
     * @param modifiers        类、构造、字段或方法的修饰符
     * @param checkedModifiers 需要检查的修饰符，如果为空返回{@code false}
     * @return 是否有指定修饰符，如果有返回true，否则false，如果提供参数为null返回false
     */
    public static boolean hasModifier(final int modifiers, final int... checkedModifiers) {
        if (ArrayKit.isEmpty(checkedModifiers)) {
            return false;
        }
        return 0 != (modifiers & EnumMap.Modifier.orToInt(checkedModifiers));
    }

    /**
     * 是否同时存在一个或多个修饰符（可能有多个修饰符，如果有指定的修饰符则返回true）
     *
     * @param modifiers        类、构造、字段或方法的修饰符
     * @param checkedModifiers 需要检查的修饰符，如果为空返回{@code false}
     * @return 是否有指定修饰符，如果有返回true，否则false，如果提供参数为null返回false
     */
    public static boolean hasAllModifier(final int modifiers, final int... checkedModifiers) {
        if (ArrayKit.isEmpty(checkedModifiers)) {
            return false;
        }
        final int checkedModifiersInt = EnumMap.Modifier.orToInt(checkedModifiers);
        return checkedModifiersInt == (modifiers & checkedModifiersInt);
    }

    /**
     * 提供的方法是否为default方法
     *
     * @param method 方法，如果为{@code null}返回{@code false}
     * @return 是否为default方法
     */
    public static boolean isDefault(final Method method) {
        return null != method && method.isDefault();
    }

    /**
     * 是否是public成员，可检测包括构造、字段和方法
     *
     * @param member 构造、字段或方法，如果为{@code null}返回{@code false}
     * @return 是否是public
     */
    public static boolean isPublic(final Member member) {
        return null != member && java.lang.reflect.Modifier.isPublic(member.getModifiers());
    }

    /**
     * 是否是public类
     *
     * @param clazz 类，如果为{@code null}返回{@code false}
     * @return 是否是public
     */
    public static boolean isPublic(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * 是否是private成员，可检测包括构造、字段和方法
     *
     * @param member 构造、字段或方法，如果为{@code null}返回{@code false}
     * @return 是否是private
     */
    public static boolean isPrivate(final Member member) {
        return null != member && java.lang.reflect.Modifier.isPrivate(member.getModifiers());
    }

    /**
     * 是否是private类
     *
     * @param clazz 类，如果为{@code null}返回{@code false}
     * @return 是否是private类
     */
    public static boolean isPrivate(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isPrivate(clazz.getModifiers());
    }

    /**
     * 是否是static成员，包括构造、字段或方法
     *
     * @param member 构造、字段或方法，如果为{@code null}返回{@code false}
     * @return 是否是static
     */
    public static boolean isStatic(final Member member) {
        return null != member && java.lang.reflect.Modifier.isStatic(member.getModifiers());
    }

    /**
     * 是否是static类
     *
     * @param clazz 类，如果为{@code null}返回{@code false}
     * @return 是否是static
     */
    public static boolean isStatic(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isStatic(clazz.getModifiers());
    }

    /**
     * 是否是合成成员（由java编译器生成的）
     *
     * @param member 构造、字段或方法
     * @return 是否是合成字段
     */
    public static boolean isSynthetic(final Member member) {
        return null != member && member.isSynthetic();
    }

    /**
     * 是否是合成类（由java编译器生成的）
     *
     * @param clazz 类
     * @return 是否是合成
     */
    public static boolean isSynthetic(final Class<?> clazz) {
        return null != clazz && clazz.isSynthetic();
    }

    /**
     * 是否抽象成员
     *
     * @param member 构造、字段或方法
     * @return 是否抽象方法
     */
    public static boolean isAbstract(final Member member) {
        return null != member && java.lang.reflect.Modifier.isAbstract(member.getModifiers());
    }

    /**
     * 是否抽象类
     *
     * @param clazz 构造、字段或方法
     * @return 是否抽象类
     */
    public static boolean isAbstract(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是否抽象类
     *
     * @param clazz 构造、字段或方法
     * @return 是否抽象类
     */
    public static boolean isInterface(final Class<?> clazz) {
        return null != clazz && clazz.isInterface();
    }

    /**
     * 设置final的field字段可以被修改 只要不会被编译器内联优化的 final 属性就可以通过反射有效的进行修改 -- 修改后代码中可使用到新的值;
     * <p>
     * 以下属性，编译器会内联优化，无法通过反射修改：
     * </p>
     * <ul>
     * <li>基本类型 byte, char, short, int, long, float, double, boolean</li>
     * <li>Literal String 类型(直接双引号字符串)</li>
     * </ul>
     * <p>
     * 以下属性，可以通过反射修改：
     * </p>
     * <ul>
     * <li>基本类型的包装类 Byte、Character、Short、Long、Float、Double、Boolean</li>
     * <li>字符串，通过 new String("")实例化</li>
     * <li>自定义java类</li>
     * </ul>
     * 
     * <pre class="code">
     * {@code
     * // 示例，移除final修饰符
     * class JdbcDialects {
     *     private static final List<Number> dialects = new ArrayList<>();
     * }
     * Field field = ReflectKit.getField(JdbcDialects.class, fieldName);
     * ReflectKit.removeFinalModify(field);
     * ReflectKit.setFieldValue(JdbcDialects.class, fieldName, dialects);
     * }
     * </pre>
     *
     * <p>
     * JDK9+此方法抛出NoSuchFieldException异常，原因是除非开放，否则模块外无法访问属性
     * </p>
     *
     * @param field 被修改的field，不可以为空
     * @throws InternalException IllegalAccessException等异常包装
     */
    public static void removeFinalModify(final Field field) {
        if (!hasModifier(field, EnumMap.Modifier.FINAL)) {
            return;
        }

        // 将字段的访问权限设为true：即去除private修饰符的影响
        ReflectKit.setAccessible(field);

        // 去除final修饰符的影响，将字段设为可修改的
        final Field modifiersField;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (final NoSuchFieldException e) {
            throw new InternalException(e, "Field [modifiers] not exist!");
        }

        try {
            // Field 的 modifiers 是私有的
            modifiersField.setAccessible(true);
            // & ：位与运算符，按位与； 运算规则：两个数都转为二进制，然后从高位开始比较，如果两个数都为1则为1，否则为0。
            // ~ ：位非运算符，按位取反；运算规则：转成二进制，如果位为0，结果是1，如果位为1，结果是0.
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        } catch (final IllegalAccessException e) {
            // 内部，工具类，基本不抛出异常
            throw new InternalException(e, "IllegalAccess for [{}.{}]", field.getDeclaringClass(), field.getName());
        }
    }

}
