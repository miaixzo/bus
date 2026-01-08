/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Validator;

import java.lang.annotation.Annotation;

/**
 * Predefined validator names within the current framework.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder extends Validator {

    /**
     * Default attribute name.
     */
    public static final String DEFAULT_FIELD = "field";
    /**
     * Default error code.
     */
    public static final String DEFAULT_ERRCODE = "115000";
    /**
     * The object parameter to be validated.
     */
    public static final String VALUE = "value";
    /**
     * The name of the field being validated.
     */
    public static final String FIELD = "field";
    /**
     * The validation group.
     */
    public static final String GROUP = "group";

    /**
     * Parameter validation constants.
     */
    public static final String _ALWAYS = "Always";
    public static final String _BLANK = "Blank";
    public static final String _CHINESE = "Chinese";
    public static final String _CITIZENID = "CitizenId";
    public static final String _DATE = "Date";
    public static final String _EACH = "Each";
    public static final String _EMAIL = "Email";
    public static final String _ENGLISH = "English";
    public static final String _EQUALS = "Equals";
    public static final String _FALSE = "False";
    public static final String _IN_ENUM = "InEnum";
    public static final String _IN = "In";
    public static final String _INT_RANGE = "IntRange";
    public static final String _IP_ADDRESS = "IPAddress";
    public static final String _LENGTH = "Length";
    public static final String _MOBILE = "Mobile";
    public static final String _MULTI = "Multi";
    public static final String _NOT_BLANK = "NotBlank";
    public static final String _NOT_EMPTY = "NotEmpty";
    public static final String _NOT_IN = "NotIn";
    public static final String _NOT_NULL = "NotNull";
    public static final String _NULL = "Null";
    public static final String _PHONE = "Phone";
    public static final String _REFLECT = "Reflect";
    public static final String _REGEX = "Regex";
    public static final String _SIZE = "Size";
    public static final String _TRUE = "True";

    /**
     * Creates a new validation instance for the given object. A new object is created each time to avoid thread safety
     * issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>    the type of the object to be validated.
     * @param object the original object.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object) {
        return Instances.singletion(Provider.class).on(object);
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
        return Instances.singletion(Provider.class).on(object, context);
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
        return Instances.singletion(Provider.class).on(object, annotations);
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
        return Instances.singletion(Provider.class).on(object, annotations, context);
    }

    /**
     * Creates a new validation instance for the given object with annotations, context, and field name. A new object is
     * created each time to avoid thread safety issues. {@link ThreadLocal} can be used for optimization.
     *
     * @param <T>         the type of the object to be validated.
     * @param object      the original object.
     * @param annotations the validation annotations.
     * @param context     the validation context.
     * @param field       the name of the field being validated.
     * @return a validation instance for the object.
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context, String field) {
        return Instances.singletion(Provider.class).on(object, annotations, context, field);
    }

}
