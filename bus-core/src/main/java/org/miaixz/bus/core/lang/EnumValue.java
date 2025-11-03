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
package org.miaixz.bus.core.lang;

import java.awt.*;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.center.map.BiMap;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Generic interface for enum elements, allowing custom enums to implement this interface for data conversion. It is
 * recommended to save {@code code()} values rather than {@code ordinal()} when persisting to a database, to guard
 * against future requirement changes.
 *
 * @param <E> The type of the enum implementing this interface.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EnumValue<E extends EnumValue<E>> extends Enumers {

    /**
     * Enumeration for action types.
     */
    @Getter
    @AllArgsConstructor
    enum Action {

        /**
         * Represents an insertion operation.
         */
        INSERT,
        /**
         * Represents a deletion operation.
         */
        DELETE,
        /**
         * Represents an update operation.
         */
        UPDATE,
        /**
         * Represents a selection or query operation.
         */
        SELECT,
        /**
         * Represents an import operation.
         */
        IMPORT,
        /**
         * Represents an export operation.
         */
        EXPORT,
        /**
         * Represents a grant or authorization operation.
         */
        GRANT,
        /**
         * Represents a data clearing operation.
         */
        CLEAN,
        /**
         * Represents any other unspecified operation.
         */
        OTHER,
    }

    /**
     * Enumeration for append modes.
     */
    enum Append {

        /**
         * Append to the beginning.
         */
        FIRST,

        /**
         * Append to the end.
         */
        LAST

    }

    /**
     * Enumeration for text alignment options.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    @Getter
    @AllArgsConstructor
    enum Align {
        /**
         * Left alignment.
         */
        LEFT,
        /**
         * Right alignment.
         */
        RIGHT,
        /**
         * Center alignment.
         */
        CENTER
    }

    /**
     * Enumeration for load balancing strategies.
     */
    @Getter
    @AllArgsConstructor
    enum Balance {
        /**
         * Round-robin strategy.
         */
        ROUND_ROBIN,
        /**
         * Random selection strategy.
         */
        RANDOM,
        /**
         * Weighted selection strategy.
         */
        WEIGHT
    }

    /**
     * Enumeration for comparison operators.
     */
    @Getter
    @AllArgsConstructor
    enum Compare {

        /**
         * Equal to.
         */
        EQ("="),
        /**
         * Not equal to.
         */
        NE("!="),
        /**
         * Less than.
         */
        LT("<"),
        /**
         * Less than or equal to.
         */
        LE("<="),
        /**
         * Greater than.
         */
        GT(">"),
        /**
         * Greater than or equal to.
         */
        GE(">="),

        /**
         * LIKE operator for pattern matching.
         */
        LIKE("LIKE");

        /**
         * The string representation of the comparison operator.
         */
        String code;

    }

    /**
     * Enumeration for festival types.
     */
    @Getter
    @AllArgsConstructor
    enum Festival {

        /**
         * Represents a regular day.
         */
        DAY(0, "日期"),
        /**
         * Represents a solar term (节气).
         */
        TERM(1, "节气"),
        /**
         * Represents New Year's Eve (除夕).
         */
        EVE(2, "除夕");

        /**
         * The code of the festival type.
         */
        private final int code;

        /**
         * The Chinese name of the festival type.
         */
        private final String name;

        /**
         * Returns the {@code Festival} enum constant corresponding to the given code.
         *
         * @param code The integer code to search for.
         * @return The {@code Festival} enum constant, or {@code null} if no match is found.
         */
        public static Festival fromCode(Integer code) {
            if (null == code) {
                return null;
            }
            for (Festival item : values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Returns the {@code Festival} enum constant corresponding to the given name.
         *
         * @param name The string name to search for.
         * @return The {@code Festival} enum constant, or {@code null} if no match is found.
         */
        public static Festival fromName(String name) {
            if (null == name) {
                return null;
            }
            for (Festival item : values()) {
                if (item.getName().equals(name)) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Returns the Chinese name of the festival.
         *
         * @return The Chinese name.
         */
        @Override
        public String toString() {
            return getName();
        }

    }

    /**
     * Enumeration for gradient directions.
     */
    @Getter
    @AllArgsConstructor
    enum Gradient {
        /**
         * Gradient from top to bottom.
         */
        TOP_BOTTOM,
        /**
         * Gradient from left to right.
         */
        LEFT_RIGHT,
        /**
         * Gradient from top-left to bottom-right.
         */
        LEFT_TOP_TO_RIGHT_BOTTOM,
        /**
         * Gradient from top-right to bottom-left.
         */
        RIGHT_TOP_TO_LEFT_BOTTOM
    }

    /**
     * Represents the lifecycle status of a managed service process.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    enum Lifecycle {

        /**
         * The service is currently running and operational.
         */
        RUNNING,

        /**
         * The service is not running.
         */
        STOPPED,

        /**
         * The service is in an error state or has crashed.
         */
        ERROR,

        /**
         * The service is in the process of starting up.
         */
        STARTING,

        /**
         * The service is in the process of shutting down.
         */
        STOPPING,

        /**
         * ACTIVE mode for FTP connections.
         */
        ACTIVE,
        /**
         * PASSIVE mode for FTP connections.
         */
        PASSIVE,
        /**
         * The status of the service is unknown.
         */
        UNKNOWN

    }

    /**
     * Enumeration of desensitization types.
     */
    @Getter
    @AllArgsConstructor
    enum Masking {
        /**
         * Full masking. Replaces the entire string with masking characters.
         */
        FULL,
        /**
         * No desensitization.
         */
        NONE,
        /**
         * Default desensitization strategy.
         */
        DEFAUL,

        /**
         * Partial masking. Masks a portion of the string, typically the middle.
         */
        PARTIAL,
        /**
         * For names.
         */
        NAME,
        /**
         * For citizen ID numbers.
         */
        CITIZENID,
        /**
         * For landline phone numbers.
         */
        PHONE,
        /**
         * For mobile phone numbers.
         */
        MOBILE,
        /**
         * For addresses.
         */
        ADDRESS,
        /**
         * For email addresses.
         */
        EMAIL,
        /**
         * For bank card numbers.
         */
        BANK_CARD,
        /**
         * For CNAPS (China National Advanced Payment System) codes.
         */
        CNAPS_CODE,
        /**
         * For payment agreement numbers.
         */
        PAY_SIGN_NO,
        /**
         * For passwords (typically replaces with an empty string).
         */
        PASSWORD,
        /**
         * For generic numbers or codes.
         */
        GENERIC,
        /**
         * Replacement masking. Replaces the string with a fixed replacement string.
         */
        REPLACE,
        /**
         * For user IDs.
         */
        USER_ID,
        /**
         * For Chinese names.
         */
        CHINESE_NAME,
        /**
         * For Mainland China car license plates, including standard and new energy vehicles.
         */
        CAR_LICENSE,
        /**
         * For IPv4 addresses.
         */
        IPV4,
        /**
         * For IPv6 addresses.
         */
        IPV6,
        /**
         * A rule that masks all but the first character.
         */
        FIRST_MASK,
        /**
         * Clears the value to {@code null}.
         */
        CLEAR_TO_NULL,
        /**
         * Clears the value to an empty string ("").
         */
        CLEAR_TO_EMPTY

    }

    /**
     * Defines the masking mode for desensitization.
     */
    @Getter
    @AllArgsConstructor
    enum Mode {
        /**
         * Masks the beginning of the string.
         */
        HEAD,
        /**
         * Masks the end of the string.
         */
        TAIL,
        /**
         * Masks the middle of the string.
         */
        MIDDLE
    }

    /**
     * Enumeration for Java reflection modifiers.
     */
    @Getter
    @AllArgsConstructor
    enum Modifier {

        /**
         * Public modifier, accessible by all classes.
         */
        PUBLIC(java.lang.reflect.Modifier.PUBLIC),
        /**
         * Private modifier, accessible and modifiable only by the declaring class.
         */
        PRIVATE(java.lang.reflect.Modifier.PRIVATE),
        /**
         * Protected modifier, accessible by the declaring class, subclasses, and classes within the same package.
         */
        PROTECTED(java.lang.reflect.Modifier.PROTECTED),
        /**
         * Static modifier, indicating that a variable is shared by all objects of the class. The variable belongs to
         * the class.
         */
        STATIC(java.lang.reflect.Modifier.STATIC),
        /**
         * Final modifier, indicating that the value of a variable cannot be changed. When applied to a method, it means
         * the method cannot be overridden.
         */
        FINAL(java.lang.reflect.Modifier.FINAL),
        /**
         * Synchronized modifier, used in multi-threaded environments to lock a method before execution to prevent
         * access from other threads, and unlock it after execution.
         */
        SYNCHRONIZED(java.lang.reflect.Modifier.SYNCHRONIZED),
        /**
         * Volatile modifier, indicating that a variable can be controlled and modified by several threads
         * simultaneously.
         */
        VOLATILE(java.lang.reflect.Modifier.VOLATILE),
        /**
         * Transient modifier, indicating that a variable is system-reserved, has no special temporary purpose, and is
         * ignored during serialization.
         */
        TRANSIENT(java.lang.reflect.Modifier.TRANSIENT),
        /**
         * Native modifier, indicating that the method body is written in another language outside the program.
         */
        NATIVE(java.lang.reflect.Modifier.NATIVE),

        /**
         * Abstract modifier, declaring a class as abstract, with unimplemented methods that need to be provided by
         * subclasses.
         */
        ABSTRACT(java.lang.reflect.Modifier.ABSTRACT),
        /**
         * Strictfp modifier. When used to declare a class, interface, or method, all floating-point operations within
         * that scope are precise and conform to the IEEE-754 standard.
         */
        STRICT(java.lang.reflect.Modifier.STRICT);

        /**
         * The integer value of the modifier enum.
         */
        private final int code;

        /**
         * Performs a bitwise OR operation on multiple modifier types to combine them.
         *
         * @param modifierTypes An array of {@code Modifier} enums. Must not be empty.
         * @return The combined integer modifier value.
         */
        public static int orToInt(final Modifier... modifierTypes) {
            int modifier = modifierTypes[0].getCode();
            for (int i = 1; i < modifierTypes.length; i++) {
                modifier |= modifierTypes[i].getCode();
            }
            return modifier;
        }

        /**
         * Performs a bitwise OR operation on multiple integer modifier values to combine them.
         *
         * @param modifierTypes An array of integer modifier values. Must not be empty.
         * @return The combined integer modifier value.
         */
        public static int orToInt(final int... modifierTypes) {
            int modifier = modifierTypes[0];
            for (int i = 1; i < modifierTypes.length; i++) {
                modifier |= modifierTypes[i];
            }
            return modifier;
        }

    }

    /**
     * Enumeration for naming conventions or patterns.
     */
    @Getter
    @AllArgsConstructor
    enum Naming {

        /**
         * Default or normal naming convention.
         */
        NORMAL(0, "默认"),

        /**
         * Bold or intensified style.
         */
        BOLD(1, "粗体"),

        /**
         * Faint or de-emphasized style.
         */
        FAINT(2, "弱化"),
        /**
         * Italic style.
         */
        ITALIC(3, "斜体"),
        /**
         * Convert to uppercase.
         */
        UPPER_CASE(4, "大写"),
        /**
         * Convert to lowercase.
         */
        LOWER_CASE(5, "小写"),
        /**
         * Camel case naming convention.
         */
        CAMEL(6, "驼峰"),
        /**
         * Convert camel case to uppercase with underscores.
         */
        CAMEL_UNDERLINE_UPPER_CASE(7, "驼峰转下划线大写"),
        /**
         * Convert camel case to lowercase with underscores.
         */
        CAMEL_UNDERLINE_LOWER_CASE(8, "驼峰转下划线小写");

        /**
         * The code associated with the naming convention.
         */
        private final long code;
        /**
         * The Chinese name of the naming convention.
         */
        private final String name;

    }

    /**
     * Enumeration for parameter sources in a request.
     */
    enum Params {
        /**
         * Parameter from request header.
         */
        HEADER,
        /**
         * Parameter from request (including form and URL parameters).
         */
        PARAMETER,
        /**
         * Parameter from JSON request body.
         */
        JSON_BODY,
        /**
         * Parameter from Cookie.
         */
        COOKIE,
        /**
         * Parameter from path variable.
         */
        PATH_VARIABLE,
        /**
         * Parameter from multipart file upload.
         */
        MULTIPART,
        /**
         * Parameter from thread context.
         */
        CONTEXT,
        /**
         * Parameter from all sources (priority: Header > Parameter > Path Variable > JSON Body > Cookie > Multipart >
         * Context).
         */
        ALL
    }

    /**
     * Enumeration for strategy patterns.
     */
    @Getter
    @AllArgsConstructor
    enum Povider {

        /**
         * Encryption and decryption strategy.
         */
        CRYPTO("CRYPTO"),
        /**
         * Captcha strategy.
         */
        CAPTCHA("CAPTCHA"),
        /**
         * Natural Language Processing (NLP) strategy.
         */
        NLP("NLP"),
        /**
         * Pinyin conversion strategy.
         */
        PINYIN("PINYIN"),
        /**
         * Template processing strategy.
         */
        TEMPLATE("TEMPLATE"),
        /**
         * JSON processing strategy.
         */
        JSON("JSON"),
        /**
         * Logging strategy.
         */
        LOGGING("LOGGING"),
        /**
         * Hotspot/Degradation strategy.
         */
        LIMITER("LIMITER"),
        /**
         * Notification strategy.
         */
        NOTIFY("NOTIFY"),
        /**
         * Authorization strategy.
         */
        AUTH("AUTH"),
        /**
         * Payment strategy.
         */
        PAY("PAY"),
        /**
         * Data anonymization/desensitization strategy.
         */
        SENSITIVE("SENSITIVE"),
        /**
         * Storage strategy.
         */
        STORAGE("STORAGE"),
        /**
         * Validation strategy.
         */
        VALIDATE("VALIDATE");

        /**
         * The code value of the provider.
         */
        String code;

    }

    /**
     * Enumeration for probe statuses, often used in health checks or traffic management.
     */
    @Getter
    @AllArgsConstructor
    enum Probe {

        /**
         * Indicates that traffic is refused.
         */
        REFUSE("refuse"),
        /**
         * Indicates that traffic is accepted.
         */
        ACCEPT("accept"),
        /**
         * Indicates a correct or healthy status.
         */
        CORRECT("correct"),
        /**
         * Indicates a broken or unhealthy status.
         */
        BROKEN("broken");

        private final String value;

    }

    /**
     * Enumeration for sorting orders.
     */
    @Getter
    @AllArgsConstructor
    enum Sort {

        /**
         * Ascending order.
         */
        ASC("ASC"),
        /**
         * Descending order.
         */
        DESC("DESC");

        /**
         * The code value representing the sort order.
         */
        String code;

    }

    /**
     * Enumeration for switch states.
     */
    enum Switch {
        /**
         * On state.
         */
        ON,

        /**
         * Off state.
         */
        OFF
    }

    /**
     * Enumeration for image thumbnail scaling methods.
     */
    @Getter
    @AllArgsConstructor
    enum Thumb {

        /**
         * Default scaling algorithm.
         */
        DEFAULT(Image.SCALE_DEFAULT),
        /**
         * Fast scaling algorithm.
         */
        FAST(Image.SCALE_FAST),
        /**
         * Smooth scaling algorithm.
         */
        SMOOTH(Image.SCALE_SMOOTH),
        /**
         * Image scaling algorithm using ReplicateScaleFilter class.
         */
        REPLICATE(Image.SCALE_REPLICATE),
        /**
         * Area Averaging scaling algorithm.
         */
        AREA_AVERAGING(Image.SCALE_AREA_AVERAGING);

        private final int code;

    }

    /**
     * Enum of basic variable types. Basic type enums include primitive types and wrapper types.
     */
    enum Type {

        /**
         * byte primitive type.
         */
        BYTE,
        /**
         * short primitive type.
         */
        SHORT,
        /**
         * int primitive type.
         */
        INT,
        /**
         * {@link Integer} wrapper type.
         */
        INTEGER,
        /**
         * long primitive type.
         */
        LONG,
        /**
         * double primitive type.
         */
        DOUBLE,
        /**
         * float primitive type.
         */
        FLOAT,
        /**
         * boolean primitive type.
         */
        BOOLEAN,
        /**
         * char primitive type.
         */
        CHAR,
        /**
         * {@link Character} wrapper type.
         */
        CHARACTER,
        /**
         * {@link String} type.
         */
        STRING;

        /**
         * A bidirectional map storing the mapping between wrapper types and their corresponding primitive types. For
         * example: Integer.class => int.class. This map is initialized directly using a factory method, making it
         * immutable and thread-safe by construction.
         */
        private static final BiMap<Class<?>, Class<?>> PRIMITIVE_MAP = new BiMap<>(Map.of(
                Boolean.class,
                boolean.class,
                Byte.class,
                byte.class,
                Character.class,
                char.class,
                Double.class,
                double.class,
                Float.class,
                float.class,
                Integer.class,
                int.class,
                Long.class,
                long.class,
                Short.class,
                short.class));

        /**
         * Converts a primitive class to its corresponding wrapper class. If the provided class is not a primitive type,
         * the original class is returned.
         *
         * @param clazz The primitive class to wrap.
         * @return The wrapper class.
         */
        public static Class<?> wrap(final Class<?> clazz) {
            return wrap(clazz, false);
        }

        /**
         * Converts a primitive class to its corresponding wrapper class. If the provided class is not a primitive type,
         * the original class is returned. If {@code errorReturnNull} is {@code true} and no corresponding primitive
         * type is found, {@code null} is returned.
         *
         * @param clazz           The primitive class to wrap.
         * @param errorReturnNull If {@code true}, returns {@code null} if no corresponding primitive type is found;
         *                        otherwise, returns the original class.
         * @return The wrapper class, or {@code null} if {@code errorReturnNull} is {@code true} and no match is found.
         */
        public static Class<?> wrap(final Class<?> clazz, final boolean errorReturnNull) {
            if (null == clazz || !clazz.isPrimitive()) {
                return clazz;
            }
            final Class<?> result = PRIMITIVE_MAP.getInverse().get(clazz);
            return (null == result) ? errorReturnNull ? null : clazz : result;
        }

        /**
         * Converts a wrapper class to its corresponding primitive class. If the provided class is not a wrapper type,
         * the original class is returned.
         *
         * @param clazz The wrapper class to unwrap.
         * @return The primitive class.
         */
        public static Class<?> unWrap(final Class<?> clazz) {
            if (null == clazz || clazz.isPrimitive()) {
                return clazz;
            }
            final Class<?> result = PRIMITIVE_MAP.get(clazz);
            return (null == result) ? clazz : result;
        }

        /**
         * Checks if the given class is a primitive wrapper type (e.g., {@code Integer.class}, {@code Boolean.class}).
         *
         * @param clazz The class to check.
         * @return {@code true} if the class is a primitive wrapper type, {@code false} otherwise.
         */
        public static boolean isPrimitiveWrapper(final Class<?> clazz) {
            if (null == clazz) {
                return false;
            }
            return PRIMITIVE_MAP.containsKey(clazz);
        }

        /**
         * Returns a set of all primitive types (e.g., {@code int.class}, {@code boolean.class}).
         *
         * @return A {@link Set} containing all primitive types.
         */
        public static Set<Class<?>> getPrimitiveSet() {
            return PRIMITIVE_MAP.getInverse().keySet();
        }

        /**
         * Returns a set of all wrapper types (e.g., {@code Integer.class}, {@code Boolean.class}).
         *
         * @return A {@link Set} containing all wrapper types.
         */
        public static Set<Class<?>> getWrapperSet() {
            return PRIMITIVE_MAP.keySet();
        }

    }

    /**
     * Enumeration for image zooming/scaling modes.
     */
    @Getter
    @AllArgsConstructor
    enum Zoom {
        /**
         * Original ratio, no scaling.
         */
        ORIGIN,
        /**
         * Specify width, height scales proportionally.
         */
        WIDTH,
        /**
         * Specify height, width scales proportionally.
         */
        HEIGHT,
        /**
         * Custom height and width, forced scaling.
         */
        OPTIONAL
    }

}
