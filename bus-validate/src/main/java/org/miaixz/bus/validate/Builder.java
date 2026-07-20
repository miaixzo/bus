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
package org.miaixz.bus.validate;

import java.lang.annotation.Annotation;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Validator;

/**
 * Predefined validator names within the current framework.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Builder extends Validator {

    /**
     * Constructs a new Builder instance.
     */
    public Builder() {
        // No initialization required.
    }

    /**
     * Default attribute name.
     */
    public static final String DEFAULT_FIELD = "field";

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
     * Always validator name.
     */
    public static final String _ALWAYS = "Always";
    /**
     * Blank validator name.
     */
    public static final String _BLANK = "Blank";
    /**
     * Chinese validator name.
     */
    public static final String _CHINESE = "Chinese";
    /**
     * Citizen identity validator name.
     */
    public static final String _CITIZENID = "CitizenId";
    /**
     * Date validator name.
     */
    public static final String _DATE = "Date";
    /**
     * Each validator name.
     */
    public static final String _EACH = "Each";
    /**
     * Email validator name.
     */
    public static final String _EMAIL = "Email";
    /**
     * English validator name.
     */
    public static final String _ENGLISH = "English";
    /**
     * Equality validator name.
     */
    public static final String _EQUALS = "Equals";
    /**
     * False validator name.
     */
    public static final String _FALSE = "False";
    /**
     * Enum inclusion validator name.
     */
    public static final String _IN_ENUM = "InEnum";
    /**
     * Inclusion validator name.
     */
    public static final String _IN = "In";
    /**
     * Integer range validator name.
     */
    public static final String _INT_RANGE = "IntRange";
    /**
     * IP address validator name.
     */
    public static final String _IP_ADDRESS = "IPAddress";
    /**
     * Length validator name.
     */
    public static final String _LENGTH = "Length";
    /**
     * Mobile number validator name.
     */
    public static final String _MOBILE = "Mobile";
    /**
     * Composite validator name.
     */
    public static final String _MULTI = "Multi";
    /**
     * Not blank validator name.
     */
    public static final String _NOT_BLANK = "NotBlank";
    /**
     * Not empty validator name.
     */
    public static final String _NOT_EMPTY = "NotEmpty";
    /**
     * Not-in validator name.
     */
    public static final String _NOT_IN = "NotIn";
    /**
     * Not null validator name.
     */
    public static final String _NOT_NULL = "NotNull";
    /**
     * Null validator name.
     */
    public static final String _NULL = "Null";
    /**
     * Phone validator name.
     */
    public static final String _PHONE = "Phone";
    /**
     * Reflective validator name.
     */
    public static final String _REFLECT = "Reflect";
    /**
     * Regular expression validator name.
     */
    public static final String _REGEX = "Regex";
    /**
     * Size validator name.
     */
    public static final String _SIZE = "Size";
    /**
     * True validator name.
     */
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
