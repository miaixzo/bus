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
package org.miaixz.bus.validate;

import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.magic.Criterion;
import org.miaixz.bus.validate.magic.ErrorCode;
import org.miaixz.bus.validate.magic.annotation.Complex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service provider for validation operations. This class provides static methods to initiate validation and helper
 * methods for the validation process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Provider {

    /**
     * Creates a new validation instance for the given object. A new object is created each time to avoid thread safety
     * issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>    the type of the object to be validated.
     * @param object the original object.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object) {
        return (T) new Verified(object).access();
    }

    /**
     * Creates a new validation instance for the given object with context. A new object is created each time to avoid
     * thread safety issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>     the type of the object to be validated.
     * @param object  the original object.
     * @param context the validation context.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object, Context context) {
        return (T) new Verified(object, context).access();
    }

    /**
     * Creates a new validation instance for the given object with annotations. A new object is created each time to
     * avoid thread safety issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>         the type of the object to be validated.
     * @param object      the original object.
     * @param annotations the validation annotations.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object, Annotation[] annotations) {
        return (T) new Verified(object, annotations).access();
    }

    /**
     * Creates a new validation instance for the given object with annotations and context. A new object is created each
     * time to avoid thread safety issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>         the type of the object to be validated.
     * @param object      the original object.
     * @param annotations the validation annotations.
     * @param context     the validation context.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context) {
        return (T) new Verified(object, annotations, context).access();
    }

    /**
     * Creates a new validation instance for the given object with annotations, context, and field name. A new object is
     * created each time to avoid thread safety issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>         the type of the object to be validated.
     * @param field       the name of the field being validated.
     * @param object      the original object.
     * @param annotations the validation annotations.
     * @param context     the validation context.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context, String field) {
        return (T) new Verified(object, annotations, context, field).access();
    }

    /**
     * Checks if the given annotation is a validation annotation. A validation annotation is one that is itself
     * annotated with {@link Complex}.
     *
     * @param annotation the annotation to check.
     * @return {@code true} if it is a validation annotation, {@code false} otherwise.
     */
    public static boolean isAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return null != annotationType.getAnnotation(Complex.class);
    }

    /**
     * Checks if the given object is an array.
     *
     * @param object the object to check.
     * @return {@code true} if the object is an array, {@code false} otherwise.
     */
    public static boolean isArray(Object object) {
        return object.getClass().isArray();
    }

    /**
     * Checks if the given object is a {@link Collection}.
     *
     * @param object the object to check.
     * @return {@code true} if the object is a collection, {@code false} otherwise.
     */
    public static boolean isCollection(Object object) {
        return Collection.class.isAssignableFrom(object.getClass());
    }

    /**
     * Checks if the given object is a {@link Map}.
     *
     * @param object the object to check.
     * @return {@code true} if the object is a map, {@code false} otherwise.
     */
    public static boolean isMap(Object object) {
        return Map.class.isAssignableFrom(object.getClass());
    }

    /**
     * Checks if the validation groups of a validator match the currently active groups in the validation context.
     *
     * @param group the validation groups to check.
     * @param list  the list of active groups in the validation context.
     * @return {@code true} if the groups match (i.e., the validator should be executed), {@code false} otherwise.
     *         Returns {@code true} if the validator defines no groups. Returns {@code false} if the validator defines
     *         groups but the context has no active groups.
     */
    public static boolean isGroup(String[] group, List<String> list) {
        if (null == group || group.length == 0) {
            return true;
        } else {
            if (null == list || list.isEmpty()) {
                return false;
            } else {
                return Arrays.stream(group).anyMatch(neededGroup -> list.stream().anyMatch(neededGroup::equals));
            }
        }
    }

    /**
     * Resolves and creates a {@link ValidateException} based on the validation criterion and context.
     *
     * @param criterion the validation criterion, containing validator configuration, error messages, etc.
     * @param context   the validation context, containing runtime information like exception class and error codes.
     * @return a {@link ValidateException} instance created according to the rules and context.
     * @throws NoSuchException if the custom exception class does not meet the requirements (e.g., missing constructor).
     */
    public static ValidateException resolve(Criterion criterion, Context context) {
        // 1. Determine the exception class: Priority is given to the context's exception class, then the criterion's,
        // and finally ValidateException.
        Class<? extends ValidateException> exceptionClass = ObjectKit
                .defaultIfNull(criterion.getException(), context.getException());

        // 2. Determine the error code: Priority is given to the criterion's error code; if it's the default, the
        // context's error code is used.
        String errcode = ObjectKit.defaultIfNull(criterion.getErrcode(), context.getErrcode());

        // 3. Get the error message: Fetched from Errors; if the key is null, the original errorCode is used.
        String errmsg = ObjectKit.defaultIfNull(criterion.getErrmsg(), ErrorCode._115000.getValue());

        // 4. Set the error code and message in the criterion.
        criterion.setErrcode(errcode);
        criterion.setErrmsg(errmsg);

        // 5. Create the exception instance.
        if (exceptionClass == null) {
            return new ValidateException(errcode, criterion.getMessage());
        }
        try {
            Constructor<? extends ValidateException> constructor = exceptionClass
                    .getConstructor(String.class, String.class);
            return constructor.newInstance(errcode, criterion.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchException("Illegal custom validation exception, no constructor(String, String) found: "
                    + exceptionClass.getName());
        } catch (IllegalAccessException e) {
            throw new NoSuchException(
                    "Unable to access constructor of custom validation exception: " + exceptionClass.getName());
        } catch (InstantiationException | InvocationTargetException e) {
            throw new NoSuchException("Failed to instantiate custom validation exception: " + exceptionClass.getName(),
                    e);
        }
    }

    /**
     * Retrieves all validation annotations from a given class.
     *
     * @param clazz the class to inspect.
     * @return a list of validation annotations found on the class.
     */
    public static List<Annotation> getAnnotation(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        return Arrays.stream(annotations).filter(Provider::isAnnotation).collect(Collectors.toList());
    }

}
