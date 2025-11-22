/*
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 * ~                                                                               ~
 * ~ The MIT License (MIT)                                                         ~
 * ~                                                                               ~
 * ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 * ~                                                                               ~
 * ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 * ~ of this software and associated documentation files (the "Software"), to deal ~
 * ~ in the Software without restriction, including without limitation the rights  ~
 * ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 * ~ copies of the Software, and to permit persons to whom the Software is         ~
 * ~ furnished to do so, subject to the following conditions:                      ~
 * ~                                                                               ~
 * ~ The above copyright notice and this permission notice shall be included in    ~
 * ~ all copies or substantial portions of the Software.                           ~
 * ~                                                                               ~
 * ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 * ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 * ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 * ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 * ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 * ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 * ~ THE SOFTWARE.                                                                 ~
 * ~                                                                               ~
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.mapper;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.text.StringBuilderPool;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.parsing.ClassColumn;
import org.miaixz.bus.mapper.parsing.ClassField;

/**
 * A utility class providing static methods for OGNL expressions, type registration, SPI instance retrieval, and
 * functional field name conversion.
 *
 * <p>
 * It also includes utilities for generating MyBatis dynamic SQL tags and parameter mappings, as well as basic SQL
 * injection checking and sanitization.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OGNL {

    /**
     * A string containing SQL syntax keywords for injection checks, delimited by '|'.
     */
    public static final String SQL_SYNTAX_KEYWORD = "and |exec |peformance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|--";
    /**
     * An array of regular expression patterns for detecting sensitive SQL functions commonly used in injection attacks.
     */
    public static final Pattern[] SQL_FUNCTION_PATTERN = new Pattern[] {
            Pattern.compile(".*chr\\s*\\(.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*mid\\s*\\(.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*char\\s*\\(.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*sleep\\s*\\(.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*user\\s*\\(.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*show\\s+tables.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*user[\\s]*\\([\\s]*\\).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*show\\s+databases.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*sleep\\(\\d*\\).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*sleep\\(.*\\).*", Pattern.CASE_INSENSITIVE) };
    /**
     * Template for logging SQL injection warning messages. Uses '{}' as placeholders for the suspicious value and the
     * detected keyword.
     */
    public static final String MESSAGE_TEMPLATE = "SQL injection check: The value '{}' is suspected of SQL injection, keyword: '{}'";

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
     * Cache for the serialized result of {@link Fn} lambda expressions, avoiding repeated and expensive reflection
     * operations.
     */
    private static final ConcurrentHashMap<Fn<?, ?>, ClassField> LAMBDA_CACHE = new ConcurrentHashMap<>();
    /**
     * Class loading cache, avoiding repeated {@code Class.forName} calls.
     */
    private static final ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    /**
     * Cache for the {@code writeReplace} method used for lambda serialization, avoiding repeated method lookups.
     */
    private static final ConcurrentHashMap<Class<?>, Method> WRITE_REPLACE_METHOD_CACHE = new ConcurrentHashMap<>();

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
            Logger.debug(true, "OGNL", "Class not found, ignored: {}", clazz);
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
     * {@link Order} interface if applicable.
     *
     * @param clazz The interface or class.
     * @param <T>   The type parameter.
     * @return A sorted list of implementation instances.
     */
    public static <T> List<T> getInstances(Class<T> clazz) {
        List<T> list = NormalSpiLoader.loadList(false, clazz);
        // Assuming Order is a custom interface defined elsewhere for sorting SPI implementations.
        if (list.size() > 1 && Order.class.isAssignableFrom(clazz)) {
            list.sort(Comparator.comparing(f -> ((Order) f).order()).reversed());
        }
        return list;
    }

    /**
     * Converts a functional interface {@link Fn} (a serializable lambda) to its corresponding field or column name.
     *
     * <p>
     * An optimized version uses a cache mechanism, providing a 5-10 fold performance improvement. It avoids repeated,
     * expensive reflection operations by caching Lambda serialization results and class loading results.
     * </p>
     *
     * @param fn The functional interface instance (e.g., {@code User::getName}).
     * @return A {@link ClassField} or {@link ClassColumn} object containing the entity class and field/column name.
     * @throws RuntimeException if the reflection operation fails.
     */
    public static ClassField fnToFieldName(Fn<?, ?> fn) {
        if (fn == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }

        // Use cache to avoid repeated Lambda serialization operations
        return LAMBDA_CACHE.computeIfAbsent(fn, f -> {
            try {
                Logger.debug(true, "OGNL", "Cache miss for lambda: {}", f.getClass().getName());
                return extractFieldInfo(f);
            } catch (Exception e) {
                throw new MapperException("Failed to convert Fn to field name", e);
            }
        });
    }

    /**
     * Core logic for extracting field information from a functional interface instance.
     *
     * @param fn The functional interface instance.
     * @return The field information object, containing the entity class and field name.
     * @throws ReflectiveOperationException if reflection fails during field extraction.
     */
    private static ClassField extractFieldInfo(Fn<?, ?> fn) {
        try {
            Class<?> clazz = null;

            // Handle special wrapper types FnName and FnType
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
                // Unwrap until the base Fn is reached
                while (fn instanceof Fn.FnType) {
                    fn = ((Fn.FnType<?, ?>) fn).fn;
                }
            }

            // Use the cached writeReplace method to serialize the lambda
            Method writeReplaceMethod = getWriteReplaceMethod(fn.getClass());
            writeReplaceMethod.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(fn);

            String getter = serializedLambda.getImplMethodName();
            if (Args.GET_PATTERN.matcher(getter).matches()) {
                // Remove "get" prefix
                getter = getter.substring(3);
            } else if (Args.IS_PATTERN.matcher(getter).matches()) {
                // Remove "is" prefix
                getter = getter.substring(2);
            }

            // Convert the getter name to the field name (e.g., getName -> name)
            String field = Introspector.decapitalize(getter);

            if (clazz == null) {
                // Extract class name from the instantiated method type signature
                Matcher matcher = Args.CLASS_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
                String implClass;
                if (matcher.find()) {
                    implClass = matcher.group("cls").replaceAll("/", "\\.");
                } else {
                    // Fallback to implementation class
                    implClass = serializedLambda.getImplClass().replaceAll("/", "\\.");
                }

                // Use class loading cache
                clazz = CLASS_CACHE.computeIfAbsent(implClass, className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Class not found: " + className, e);
                    }
                });
            }

            return new ClassField(clazz, field);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to extract field info from Fn", e);
        }
    }

    /**
     * Retrieves the {@code writeReplace} method for lambda serialization, utilizing the cache.
     *
     * @param clazz The target class (usually the functional interface class).
     * @return The {@code writeReplace} method object.
     * @throws NoSuchMethodException if the method cannot be found (should be handled by the cache logic).
     */
    private static Method getWriteReplaceMethod(Class<?> clazz) throws NoSuchMethodException {
        return WRITE_REPLACE_METHOD_CACHE.computeIfAbsent(clazz, c -> {
            try {
                Method method = c.getDeclaredMethod("writeReplace");
                method.setAccessible(Boolean.TRUE);
                return method;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("writeReplace method not found in class: " + c.getName(), e);
            }
        });
    }

    /**
     * Clears all internal caches.
     *
     * <p>
     * Primarily used for testing or specific memory management scenarios.
     * </p>
     */
    public static void clearCache() {
        int lambdaCount = LAMBDA_CACHE.size();
        int classCount = CLASS_CACHE.size();
        int methodCount = WRITE_REPLACE_METHOD_CACHE.size();

        LAMBDA_CACHE.clear();
        CLASS_CACHE.clear();
        WRITE_REPLACE_METHOD_CACHE.clear();

        Logger.info(
                false,
                "OGNL",
                "Cache cleared: lambda={}, class={}, method={}",
                lambdaCount,
                classCount,
                methodCount);
    }

    /**
     * Gets the current cache statistics.
     *
     * @return A {@link CacheStats} object containing the current cache sizes.
     */
    public static CacheStats getCacheStats() {
        return new CacheStats(LAMBDA_CACHE.size(), CLASS_CACHE.size(), WRITE_REPLACE_METHOD_CACHE.size());
    }

    /**
     * Checks if the given parameter value poses a risk of SQL injection based on a predefined set of patterns and
     * keywords.
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
                    false,
                    "OGNL",
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
        for (Pattern pattern : SQL_FUNCTION_PATTERN) {
            if (pattern.matcher(value).matches()) {
                Logger.warn(false, "OGNL", MESSAGE_TEMPLATE, value, pattern.pattern());
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitizes a string to prevent SQL injection. If a potential injection is detected via
     * {@link #validateSql(String)}, it filters out SQL blacklist characters and whitespace using
     * {@link #replaceAllBlank(String)}.
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
     * Checks if the given value contains any of the specified keywords.
     *
     * @param value    The value to check.
     * @param keywords The array of keywords to look for.
     * @return {@code true} if a keyword is found, {@code false} otherwise.
     */
    private static boolean keywords(String value, String[] keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                Logger.warn(false, "OGNL", MESSAGE_TEMPLATE, value, keyword);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes various whitespace and blacklist characters from a string, including newlines, tabs, and spaces, as
     * defined by {@link #REPLACE_BLANK}.
     *
     * @param value The string to process.
     * @return The string with whitespace and blacklist characters removed.
     */
    public static String replaceAllBlank(String value) {
        Matcher matcher = REPLACE_BLANK.matcher(value);
        return matcher.replaceAll(Normal.EMPTY);
    }

    /**
     * Removes escape characters (single quote {@code '} and double quote {@code "}) from a string.
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
     * @param ifTest    The OGNL test condition for the {@code <if>} tag.
     * @param newLine   Whether to wrap the script with new line characters before and after.
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
     * @param prefix          The prefix to add to the result (e.g., "WHERE").
     * @param suffix          The suffix to add to the result (e.g., "Order BY id").
     * @param prefixOverrides A comma-separated list of prefixes to remove (e.g., "AND |OR ").
     * @param suffixOverrides A comma-separated list of suffixes to remove.
     * @return The SQL script wrapped in a {@code <trim>} tag.
     */
    public static String convertTrim(
            final String sqlScript,
            final String prefix,
            final String suffix,
            final String prefixOverrides,
            final String suffixOverrides) {
        StringBuilder sb = StringBuilderPool.acquireRaw(200 + sqlScript.length());
        try {
            sb.append("<trim");
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
            return sb.append(Symbol.GT).append(Symbol.LF).append(sqlScript).append(Symbol.LF).append("</trim>")
                    .toString();
        } finally {
            StringBuilderPool.release(sb);
        }
    }

    /**
     * Creates a MyBatis {@code <choose><when><otherwise>} block.
     *
     * @param whenTest      The OGNL test condition for the {@code <when>} tag.
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
     * @param collection The collection to iterate over (e.g., "list" or "array").
     * @param index      The name for the index variable (optional).
     * @param item       The name for the item variable (e.g., "id").
     * @param separator  The separator to place between elements (e.g., ",").
     * @return The SQL script wrapped in a {@code <foreach>} tag.
     */
    public static String convertForeach(
            final String sqlScript,
            final String collection,
            final String index,
            final String item,
            final String separator) {
        StringBuilder sb = StringBuilderPool.acquireRaw(150 + sqlScript.length());
        try {
            sb.append("<foreach");
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
        } finally {
            StringBuilderPool.release(sb);
        }
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <where>} tag. This tag automatically handles the removal of
     * leading "AND" or "OR" keywords and inserts the "WHERE" keyword when needed.
     *
     * @param sqlScript The SQL script to be placed inside the tag.
     * @return The SQL script wrapped in a {@code <where>} tag.
     */
    public static String convertWhere(final String sqlScript) {
        return "<where>" + Symbol.LF + sqlScript + Symbol.LF + "</where>";
    }

    /**
     * Wraps a SQL script fragment in a MyBatis {@code <set>} tag. This tag automatically handles the removal of leading
     * commas and inserts the "SET" keyword when needed.
     *
     * @param sqlScript The SQL script to be placed inside the tag.
     * @return The SQL script wrapped in a {@code <set>} tag.
     */
    public static String convertSet(final String sqlScript) {
        return "<set>" + Symbol.LF + sqlScript + Symbol.LF + "</set>";
    }

    /**
     * Generates a safe MyBatis parameter placeholder with additional mapping attributes (e.g.,
     * {@code #{param,jdbcType=VARCHAR}}).
     *
     * @param param   The parameter name.
     * @param mapping The parameter mapping configuration (e.g., "jdbcType=VARCHAR,typeHandler=...").
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
     * Generates a safe MyBatis parameter placeholder (e.g., {@code #{param}}).
     *
     * @param param The parameter name.
     * @return The safe parameter placeholder script.
     */
    public static String safeParam(final String param) {
        return safeParam(param, null);
    }

    /**
     * Generates an unsafe (raw) MyBatis parameter placeholder (e.g., {@code ${param}}).
     *
     * <p>
     * **Warning:** Use with caution, as this method directly inserts the parameter value into the SQL string, which can
     * lead to SQL injection vulnerabilities if the parameter is not sanitized.
     * </p>
     *
     * @param param The parameter name.
     * @return The unsafe parameter placeholder script.
     */
    public static String unSafeParam(final String param) {
        return Symbol.DOLLAR_LEFT_BRACE + param + Symbol.C_BRACE_RIGHT;
    }

    /**
     * Generates a mapping configuration string for a numeric scale.
     *
     * @param numericScale The numeric scale (precision after the decimal point).
     * @return The numeric scale mapping string (e.g., "numericScale=2"), or null if the scale is null.
     */
    public static String mappingNumericScale(Integer numericScale) {
        if (numericScale != null) {
            return "numericScale=" + numericScale;
        }
        return null;
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
     * Combines mapping configurations for {@link TypeHandler}, {@link JdbcType}, and numeric scale into a single
     * string, separated by commas.
     *
     * @param typeHandler  The TypeHandler class.
     * @param jdbcType     The JdbcType enum.
     * @param numericScale The numeric scale.
     * @return The combined mapping configuration string (e.g., "jdbcType=VARCHAR,numericScale=2"), or null if all
     *         parameters are null.
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
     * Appends a new mapping configuration item to an existing mapping string, using a comma as a separator.
     *
     * @param mapping The current mapping configuration (can be null).
     * @param other   The mapping configuration to append (must be non-null).
     * @return The concatenated mapping string.
     */
    private static String appendMapping(String mapping, String other) {
        if (mapping != null) {
            return mapping + Symbol.COMMA + other;
        }
        return other;
    }

    /**
     * Cache statistics class.
     */
    public static class CacheStats {

        private final int lambdaCacheSize;
        private final int classCacheSize;
        private final int methodCacheSize;

        /**
         * Constructs a CacheStats instance.
         *
         * @param lambdaCacheSize The size of the lambda serialization cache.
         * @param classCacheSize  The size of the class loading cache.
         * @param methodCacheSize The size of the method lookup cache.
         */
        public CacheStats(int lambdaCacheSize, int classCacheSize, int methodCacheSize) {
            this.lambdaCacheSize = lambdaCacheSize;
            this.classCacheSize = classCacheSize;
            this.methodCacheSize = methodCacheSize;
        }

        /**
         * Gets the size of the lambda serialization cache.
         *
         * @return The lambda cache size.
         */
        public int getLambdaCacheSize() {
            return lambdaCacheSize;
        }

        /**
         * Gets the size of the class loading cache.
         *
         * @return The class cache size.
         */
        public int getClassCacheSize() {
            return classCacheSize;
        }

        /**
         * Gets the size of the method lookup cache.
         *
         * @return The method cache size.
         */
        public int getMethodCacheSize() {
            return methodCacheSize;
        }

        /**
         * Gets the total size of all caches.
         *
         * @return The total cache size.
         */
        public int getTotalSize() {
            return lambdaCacheSize + classCacheSize + methodCacheSize;
        }

        /**
         * Returns a formatted string representation of the cache statistics.
         *
         * @return The statistics string.
         */
        @Override
        public String toString() {
            return String.format(
                    "OGNL Cache Stats{lambda=%d, class=%d, method=%d, total=%d}",
                    lambdaCacheSize,
                    classCacheSize,
                    methodCacheSize,
                    getTotalSize());
        }
    }

}
