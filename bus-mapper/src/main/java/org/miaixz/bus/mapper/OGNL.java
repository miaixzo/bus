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
package org.miaixz.bus.mapper;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.support.ClassColumn;
import org.miaixz.bus.mapper.support.ClassField;

/**
 * A utility class providing static methods for OGNL expressions, type registration, SPI instance retrieval, and
 * functional field name conversion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OGNL {

    /**
     * Regular expression for SQL syntax checking. A match is considered valid only if two keywords are found in order.
     */
    public static final Pattern SQL_SYNTAX_PATTERN = Pattern.compile(
            "(insert|delete|update|select|create|drop|truncate|grant|alter|deny|revoke|call|execute|exec|declare|show|rename|set)"
                    + "\\s+.*(into|from|set|where|table|database|view|index|on|cursor|procedure|trigger|for|password|union|and|or)|(select\\s*\\*\\s*from\\s+)|(and|or)\\s+.*",
            Pattern.CASE_INSENSITIVE);

    /**
     * Regular expression to detect SQL comments and potential truncation attacks, matching SQL statements that contain
     * single quotes, comments, or semicolons.
     */
    public static final Pattern SQL_COMMENT_PATTERN = Pattern
            .compile("'.*(or|union|--|#|/\\*|;)", Pattern.CASE_INSENSITIVE);

    /**
     * A string containing SQL syntax keywords for injection checks.
     */
    public static final String SQL_SYNTAX_KEYWORD = "and |exec |peformance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|--";

    /**
     * An array of regular expression patterns for detecting sensitive SQL functions.
     */
    public static final String[] SQL_FUNCTION_PATTERN = new String[] { "chr\\s*\\(", "mid\\s*\\(", " char\\s*\\(",
            "sleep\\s*\\(", "user\\s*\\(", "show\\s+tables", "user[\\s]*\\([\\s]*\\)", "show\\s+databases",
            "sleep\\(\\d*\\)", "sleep\\(.*\\)", };

    /**
     * Template for logging SQL injection warning messages.
     */
    public static final String MESSAGE_TEMPLATE = "SQL injection check: The value '{}' is suspected of SQL injection, keyword: '{}'";

    /**
     * A pattern to remove whitespace and special characters from a string to prevent SQL injection. This includes
     * common SQL injection blacklist characters and whitespace characters.
     */
    public static final Pattern REPLACE_BLANK = Pattern.compile("'|\"|\\<|\\>|&|\\*|\\+|=|#|-|;|\\s*|\t|\r|\n");

    /**
     * Registers a new class as a "simple type" for the mapper.
     *
     * @param clazz The class to register.
     */
    public static void registerSimpleType(Class<?> clazz) {
        Args.SIMPLE_TYPE_SET.add(clazz);
    }

    /**
     * Registers multiple simple types from a comma-separated string of fully qualified class names.
     *
     * @param classes A comma-separated string of class names.
     * @throws RuntimeException if a class name is invalid or cannot be found.
     */
    public static void registerSimpleType(String classes) {
        if (StringKit.isNotEmpty(classes)) {
            String[] cls = classes.split(Symbol.COMMA);
            for (String c : cls) {
                try {
                    Args.SIMPLE_TYPE_SET.add(Class.forName(c));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to register type: " + c, e);
                }
            }
        }
    }

    /**
     * Registers a simple type silently, ignoring {@link ClassNotFoundException}.
     *
     * @param clazz The fully qualified name of the class.
     */
    public static void registerSimpleTypeSilence(String clazz) {
        try {
            Args.SIMPLE_TYPE_SET.add(Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            Logger.debug("Class not found, ignored: " + clazz);
        }
    }

    /**
     * Checks if the specified class is a registered simple type.
     *
     * @param clazz The class to check.
     * @return {@code true} if the class is a simple type, {@code false} otherwise.
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return Args.SIMPLE_TYPE_SET.contains(clazz);
    }

    /**
     * Gets all SPI (Service Provider Interface) implementation instances for a given interface or class, sorted by the
     * {@link ORDER} interface if applicable.
     *
     * @param clazz The interface or class.
     * @param <T>   The type parameter.
     * @return A sorted list of implementation instances.
     */
    public static <T> List<T> getInstances(Class<T> clazz) {
        List<T> list = NormalSpiLoader.loadList(false, clazz);
        if (list.size() > 1 && ORDER.class.isAssignableFrom(clazz)) {
            list.sort(Comparator.comparing(f -> ((ORDER) f).order()).reversed());
        }
        return list;
    }

    /**
     * Converts a functional interface {@link Fn} (a serializable lambda) to its corresponding field or column name.
     *
     * @param fn The functional interface instance (e.g., {@code User::getName}).
     * @return A {@link ClassField} or {@link ClassColumn} object containing the class and field/column name.
     * @throws RuntimeException if the reflection operation fails.
     */
    public static ClassField fnToFieldName(Fn<?, ?> fn) {
        try {
            Class<?> clazz = null;
            if (fn instanceof Fn.FnName<?, ?> field) {
                if (field.column) {
                    return new ClassColumn(field.entityClass, field.name);
                } else {
                    return new ClassField(field.entityClass, field.name);
                }
            }
            if (fn instanceof Fn.FnType) {
                clazz = ((Fn.FnType<?, ?>) fn).entityClass;
                fn = ((Fn.FnType<?, ?>) fn).fn;
                while (fn instanceof Fn.FnType) {
                    fn = ((Fn.FnType<?, ?>) fn).fn;
                }
            }
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
            String getter = serializedLambda.getImplMethodName();
            if (Args.GET_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(3);
            } else if (Args.IS_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(2);
            }
            String field = Introspector.decapitalize(getter);
            if (clazz == null) {
                Matcher matcher = Args.CLASS_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
                String implClass;
                if (matcher.find()) {
                    implClass = matcher.group("cls").replaceAll("/", "\\.");
                } else {
                    implClass = serializedLambda.getImplClass().replaceAll("/", "\\.");
                }
                clazz = Class.forName(implClass);
            }
            return new ClassField(clazz, field);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to convert Fn to field name", e);
        }
    }

    /**
     * Checks if the given parameter value poses a risk of SQL injection.
     *
     * @param value The parameter value to check.
     * @return {@code true} if a SQL injection risk is detected, {@code false} otherwise.
     */
    public static boolean validateSql(String value) {
        if (StringKit.isBlank(value)) {
            return false;
        }
        // Check for SQL comment characters or sensitive SQL injection characters
        if (SQL_COMMENT_PATTERN.matcher(value).find() || SQL_SYNTAX_PATTERN.matcher(value).find()) {
            Logger.warn(
                    "SQL injection check: The value '{}' contains SQL comment characters or sensitive SQL injection characters",
                    value);
            return true;
        }
        // Convert to lower case for comparison
        value = value.toLowerCase().trim();
        // Check for SQL syntax keywords
        if (keywords(value, SQL_SYNTAX_KEYWORD.split("\\|"))) {
            return true;
        }

        // Check for sensitive SQL function patterns
        for (String pattern : SQL_FUNCTION_PATTERN) {
            if (Pattern.matches(".*" + pattern + ".*", value)) {
                Logger.warn(MESSAGE_TEMPLATE, value, pattern);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given value contains any of the specified keywords.
     *
     * @param value    The value to check.
     * @param keywords The array of keywords to look for.
     * @return {@code true} if a keyword is found, {@code false} otherwise.
     */
    private static boolean keywords(String value, String[] keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                Logger.warn(MESSAGE_TEMPLATE, value, keyword);
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitizes a string to prevent SQL injection. If a potential injection is detected, it filters out SQL blacklist
     * characters and whitespace.
     *
     * @param value The string to sanitize.
     * @return The sanitized string.
     */
    public static String injection(String value) {
        if (validateSql(value)) {
            // Filter SQL blacklist characters and remove whitespace if injection is suspected
            value = replaceAllBlank(value);
        }
        return value;
    }

    /**
     * Removes various whitespace characters from a string, including newlines, tabs, and spaces.
     *
     * @param value The string to process.
     * @return The string with whitespace removed.
     */
    public static String replaceAllBlank(String value) {
        Matcher matcher = REPLACE_BLANK.matcher(value);
        return matcher.replaceAll(Normal.EMPTY);
    }

    /**
     * Removes escape characters (single and double quotes) from a string.
     *
     * @param text The string to process.
     * @return The string with escape characters removed.
     * @throws NullPointerException if text is null.
     */
    public static String removeEscapeCharacter(String text) {
        Objects.requireNonNull(text);
        return text.replaceAll("\"", "").replaceAll("'", "");
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <if>} tag.
     *
     * @param sqlScript The SQL script fragment.
     * @param ifTest    The test condition for the {@code <if>} tag.
     * @param newLine   Whether to wrap the script in new lines.
     * @return The SQL script wrapped in an {@code <if>} tag.
     */
    public static String convertIf(final String sqlScript, final String ifTest, boolean newLine) {
        String newSqlScript = sqlScript;
        if (newLine) {
            newSqlScript = Symbol.LF + newSqlScript + Symbol.LF;
        }
        return String.format("<if test=\"%s\">%s</if>", ifTest, newSqlScript);
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <trim>} tag.
     *
     * @param sqlScript       The SQL script fragment.
     * @param prefix          The prefix to add.
     * @param suffix          The suffix to add.
     * @param prefixOverrides The prefixes to override.
     * @param suffixOverrides The suffixes to override.
     * @return The SQL script wrapped in a {@code <trim>} tag.
     */
    public static String convertTrim(
            final String sqlScript,
            final String prefix,
            final String suffix,
            final String prefixOverrides,
            final String suffixOverrides) {
        StringBuilder sb = new StringBuilder("<trim");
        if (StringKit.isNotBlank(prefix)) {
            sb.append(" prefix=\"").append(prefix).append(Symbol.SINGLE_QUOTE);
        }
        if (StringKit.isNotBlank(suffix)) {
            sb.append(" suffix=\"").append(suffix).append(Symbol.SINGLE_QUOTE);
        }
        if (StringKit.isNotBlank(prefixOverrides)) {
            sb.append(" prefixOverrides=\"").append(prefixOverrides).append(Symbol.SINGLE_QUOTE);
        }
        if (StringKit.isNotBlank(suffixOverrides)) {
            sb.append(" suffixOverrides=\"").append(suffixOverrides).append(Symbol.SINGLE_QUOTE);
        }
        return sb.append(Symbol.GT).append(Symbol.LF).append(sqlScript).append(Symbol.LF).append("</trim>").toString();
    }

    /**
     * Creates a MyBatis {@code <choose><when><otherwise>} block.
     *
     * @param whenTest      The test condition for the {@code <when>} tag.
     * @param whenSqlScript The SQL script for the {@code <when>} block.
     * @param otherwise     The content for the {@code <otherwise>} block.
     * @return A string representing the {@code <choose>} block.
     */
    public static String convertChoose(final String whenTest, final String whenSqlScript, final String otherwise) {
        return "<choose>" + Symbol.LF + "<when test=\"" + whenTest + Symbol.SINGLE_QUOTE + Symbol.GT + Symbol.LF
                + whenSqlScript + Symbol.LF + "</when>" + Symbol.LF + "<otherwise>" + otherwise + "</otherwise>"
                + Symbol.LF + "</choose>";
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <foreach>} tag.
     *
     * @param sqlScript  The SQL script fragment inside the loop.
     * @param collection The collection to iterate over.
     * @param index      The name for the index variable.
     * @param item       The name for the item variable.
     * @param separator  The separator to place between elements.
     * @return The SQL script wrapped in a {@code <foreach>} tag.
     */
    public static String convertForeach(
            final String sqlScript,
            final String collection,
            final String index,
            final String item,
            final String separator) {
        StringBuilder sb = new StringBuilder("<foreach");
        if (StringKit.isNotBlank(collection)) {
            sb.append(" collection=\"").append(collection).append(Symbol.SINGLE_QUOTE);
        }
        if (StringKit.isNotBlank(index)) {
            sb.append(" index=\"").append(index).append(Symbol.SINGLE_QUOTE);
        }
        if (StringKit.isNotBlank(item)) {
            sb.append(" item=\"").append(item).append(Symbol.SINGLE_QUOTE);
        }
        if (StringKit.isNotBlank(separator)) {
            sb.append(" separator=\"").append(separator).append(Symbol.SINGLE_QUOTE);
        }
        return sb.append(Symbol.GT).append(Symbol.LF).append(sqlScript).append(Symbol.LF).append("</foreach>")
                .toString();
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <where>} tag.
     *
     * @param sqlScript The SQL script to be placed inside the tag.
     * @return The SQL script wrapped in a {@code <where>} tag.
     */
    public static String convertWhere(final String sqlScript) {
        return "<where>" + Symbol.LF + sqlScript + Symbol.LF + "</where>";
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <set>} tag.
     *
     * @param sqlScript The SQL script to be placed inside the tag.
     * @return The SQL script wrapped in a {@code <set>} tag.
     */
    public static String convertSet(final String sqlScript) {
        return "<set>" + Symbol.LF + sqlScript + Symbol.LF + "</set>";
    }

    /**
     * Generates a safe MyBatis parameter placeholder (e.g., {@code #{param}}).
     *
     * @param param The parameter name.
     * @return The safe parameter placeholder script.
     */
    public static String safeParam(final String param) {
        return safeParam(param, null);
    }

    /**
     * Generates a safe MyBatis parameter placeholder with additional mapping attributes (e.g.,
     * {@code #{param,jdbcType=VARCHAR}}).
     *
     * @param param   The parameter name.
     * @param mapping The parameter mapping configuration (e.g., "jdbcType=VARCHAR").
     * @return The safe parameter placeholder script.
     */
    public static String safeParam(final String param, final String mapping) {
        String target = Symbol.HASH_LEFT_BRACE + param;
        if (StringKit.isBlank(mapping)) {
            return target + Symbol.C_BRACE_RIGHT;
        }
        return target + Symbol.COMMA + mapping + Symbol.C_BRACE_RIGHT;
    }

    /**
     * Generates an unsafe (raw) MyBatis parameter placeholder (e.g., {@code ${param}}).
     *
     * @param param The parameter name.
     * @return The unsafe parameter placeholder script.
     */
    public static String unSafeParam(final String param) {
        return Symbol.DOLLAR_LEFT_BRACE + param + Symbol.C_BRACE_RIGHT;
    }

    /**
     * Generates a mapping configuration string for a {@link TypeHandler}.
     *
     * @param typeHandler The TypeHandler class.
     * @return The TypeHandler mapping string (e.g., "typeHandler=com.example.MyTypeHandler"), or null if the handler is
     *         null.
     */
    public static String mappingTypeHandler(Class<? extends TypeHandler<?>> typeHandler) {
        if (typeHandler != null) {
            return "typeHandler=" + typeHandler.getName();
        }
        return null;
    }

    /**
     * Generates a mapping configuration string for a {@link JdbcType}.
     *
     * @param jdbcType The JdbcType enum.
     * @return The JdbcType mapping string (e.g., "jdbcType=VARCHAR"), or null if the type is null.
     */
    public static String mappingJdbcType(JdbcType jdbcType) {
        if (jdbcType != null) {
            return "jdbcType=" + jdbcType.name();
        }
        return null;
    }

    /**
     * Generates a mapping configuration string for a numeric scale.
     *
     * @param numericScale The numeric scale.
     * @return The numeric scale mapping string (e.g., "numericScale=2"), or null if the scale is null.
     */
    public static String mappingNumericScale(Integer numericScale) {
        if (numericScale != null) {
            return "numericScale=" + numericScale;
        }
        return null;
    }

    /**
     * Combines mapping configurations for {@link TypeHandler}, {@link JdbcType}, and numeric scale into a single
     * string.
     *
     * @param typeHandler  The TypeHandler class.
     * @param jdbcType     The JdbcType enum.
     * @param numericScale The numeric scale.
     * @return The combined mapping configuration string, or null if all parameters are null.
     */
    public static String convertParamMapping(
            Class<? extends TypeHandler<?>> typeHandler,
            JdbcType jdbcType,
            Integer numericScale) {
        if (typeHandler == null && jdbcType == null && numericScale == null) {
            return null;
        }
        String mapping = null;
        if (typeHandler != null) {
            mapping = mappingTypeHandler(typeHandler);
        }
        if (jdbcType != null) {
            mapping = appendMapping(mapping, mappingJdbcType(jdbcType));
        }
        if (numericScale != null) {
            mapping = appendMapping(mapping, mappingNumericScale(numericScale));
        }
        return mapping;
    }

    /**
     * Appends a mapping configuration item to an existing mapping string.
     *
     * @param mapping The current mapping configuration.
     * @param other   The mapping configuration to append.
     * @return The concatenated mapping string.
     */
    private static String appendMapping(String mapping, String other) {
        if (mapping != null) {
            return mapping + Symbol.COMMA + other;
        }
        return other;
    }

}
