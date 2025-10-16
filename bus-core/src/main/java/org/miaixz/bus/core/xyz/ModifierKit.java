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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.ClassMember;

/**
 * Class modifier utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ModifierKit {

    /**
     * Checks if a class has any of the specified modifiers.
     *
     * @param clazz         The class.
     * @param modifierTypes The modifiers to check for.
     * @return `true` if the class has at least one of the specified modifiers.
     */
    public static boolean hasAny(final Class<?> clazz, final EnumValue.Modifier... modifierTypes) {
        if (null == clazz || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return hasAny(ClassMember.of(clazz), modifierTypes);
    }

    /**
     * Checks if a member (constructor, field, or method) has any of the specified modifiers.
     *
     * @param member        The member.
     * @param modifierTypes The modifiers to check for.
     * @return `true` if the member has at least one of the specified modifiers.
     */
    public static boolean hasAny(final Member member, final EnumValue.Modifier... modifierTypes) {
        if (null == member || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (member.getModifiers() & EnumValue.Modifier.orToInt(modifierTypes));
    }

    /**
     * Checks if a set of modifiers contains any of the specified modifiers.
     *
     * @param modifiers        The modifier flags.
     * @param checkedModifiers The modifiers to check for.
     * @return `true` if at least one modifier matches.
     */
    public static boolean hasAny(final int modifiers, final int... checkedModifiers) {
        if (ArrayKit.isEmpty(checkedModifiers)) {
            return false;
        }
        return 0 != (modifiers & EnumValue.Modifier.orToInt(checkedModifiers));
    }

    /**
     * Checks if a class has all of the specified modifiers.
     *
     * @param clazz         The class.
     * @param modifierTypes The modifiers to check for.
     * @return `true` if the class has all of the specified modifiers.
     */
    public static boolean hasAll(final Class<?> clazz, final EnumValue.Modifier... modifierTypes) {
        if (null == clazz || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return hasAll(ClassMember.of(clazz), modifierTypes);
    }

    /**
     * Checks if a member has all of the specified modifiers.
     *
     * @param member        The member.
     * @param modifierTypes The modifiers to check for.
     * @return `true` if the member has all of the specified modifiers.
     */
    public static boolean hasAll(final Member member, final EnumValue.Modifier... modifierTypes) {
        if (null == member || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        final int checkedModifiersInt = EnumValue.Modifier.orToInt(modifierTypes);
        return checkedModifiersInt == (member.getModifiers() & checkedModifiersInt);
    }

    /**
     * Checks if a set of modifiers contains all of the specified modifiers.
     *
     * @param modifiers        The modifier flags.
     * @param checkedModifiers The modifiers to check for.
     * @return `true` if all modifiers match.
     */
    public static boolean hasAll(final int modifiers, final int... checkedModifiers) {
        if (ArrayKit.isEmpty(checkedModifiers)) {
            return false;
        }
        final int checkedModifiersInt = EnumValue.Modifier.orToInt(checkedModifiers);
        return checkedModifiersInt == (modifiers & checkedModifiersInt);
    }

    /**
     * Checks if a method is a `default` method.
     *
     * @param method The method.
     * @return `true` if it is a `default` method.
     */
    public static boolean isDefault(final Method method) {
        return null != method && method.isDefault();
    }

    /**
     * Checks if a member is public.
     *
     * @param member The member.
     * @return `true` if public.
     */
    public static boolean isPublic(final Member member) {
        return null != member && java.lang.reflect.Modifier.isPublic(member.getModifiers());
    }

    /**
     * Checks if a class is public.
     *
     * @param clazz The class.
     * @return `true` if public.
     */
    public static boolean isPublic(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * Checks if a member is private.
     *
     * @param member The member.
     * @return `true` if private.
     */
    public static boolean isPrivate(final Member member) {
        return null != member && java.lang.reflect.Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Checks if a class is private.
     *
     * @param clazz The class.
     * @return `true` if private.
     */
    public static boolean isPrivate(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isPrivate(clazz.getModifiers());
    }

    /**
     * Checks if a member is static.
     *
     * @param member The member.
     * @return `true` if static.
     */
    public static boolean isStatic(final Member member) {
        return null != member && java.lang.reflect.Modifier.isStatic(member.getModifiers());
    }

    /**
     * Checks if a class is static.
     *
     * @param clazz The class.
     * @return `true` if static.
     */
    public static boolean isStatic(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isStatic(clazz.getModifiers());
    }

    /**
     * Checks if a member is synthetic (generated by the compiler).
     *
     * @param member The member.
     * @return `true` if synthetic.
     */
    public static boolean isSynthetic(final Member member) {
        return null != member && member.isSynthetic();
    }

    /**
     * Checks if a class is synthetic.
     *
     * @param clazz The class.
     * @return `true` if synthetic.
     */
    public static boolean isSynthetic(final Class<?> clazz) {
        return null != clazz && clazz.isSynthetic();
    }

    /**
     * Checks if a member is abstract.
     *
     * @param member The member.
     * @return `true` if abstract.
     */
    public static boolean isAbstract(final Member member) {
        return null != member && java.lang.reflect.Modifier.isAbstract(member.getModifiers());
    }

    /**
     * Checks if a class is abstract.
     *
     * @param clazz The class.
     * @return `true` if abstract.
     */
    public static boolean isAbstract(final Class<?> clazz) {
        return null != clazz && java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if a class is an interface.
     *
     * @param clazz The class.
     * @return `true` if it is an interface.
     */
    public static boolean isInterface(final Class<?> clazz) {
        return null != clazz && clazz.isInterface();
    }

    /**
     * Removes the `final` modifier from a field, allowing it to be modified.
     *
     * @param field The field to modify.
     * @throws InternalException if modification fails.
     */
    public static void removeFinalModify(final Field field) {
        if (!hasAny(field, EnumValue.Modifier.FINAL)) {
            return;
        }

        ReflectKit.setAccessible(field);

        final Field modifiersField;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (final NoSuchFieldException e) {
            throw new InternalException(e, "Field [modifiers] not exist!");
        }

        try {
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        } catch (final IllegalAccessException e) {
            throw new InternalException(e, "IllegalAccess for [{}.{}]", field.getDeclaringClass(), field.getName());
        }
    }

}
