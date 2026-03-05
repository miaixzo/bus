/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
     * Checks if the given field is declared as transient.
     *
     * @param field The Field object to check.
     * @return {@code true} if the field is transient; otherwise, {@code false}. Also returns {@code false} if the
     *         {@code field} parameter is {@code null}.
     */
    public static boolean isTransient(Field field) {
        // Defensive programming: If the field object is null, return false immediately
        if (field == null) {
            return false;
        }

        // 1. Get the integer representation of the field's modifiers.
        // field.getModifiers() returns an int containing bit flags for all modifiers
        // (public, private, static, final, transient, etc.).
        int modifiers = field.getModifiers();

        // 2. Use the static method from the Modifier class to check the transient modifier flag.
        // Modifier.isTransient(int) performs a bitwise check against the Modifier.TRANSIENT constant.
        return Modifier.isTransient(modifiers);
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
