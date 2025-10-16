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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.EnumValue;

/**
 * This class defines constants for MyBatis configuration and SQL fragments.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Args {

    /**
     * Regular expression for getter methods.
     */
    public static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");

    /**
     * Regular expression for is-methods (for boolean getters).
     */
    public static final Pattern IS_PATTERN = Pattern.compile("^is[A-Z].*");

    /**
     * Regular expression to extract class names from lambda expressions.
     */
    public static final Pattern CLASS_PATTERN = Pattern.compile("\\(L(?<cls>.+);\\).+");

    /**
     * Regular expression to remove potential delimiters (like backticks or brackets) from field names.
     */
    public static final Pattern DELIMITER = Pattern.compile("^[`\\[\"]?(.*?)[`\\]\"]?$");

    /**
     * Represents the 'normal' naming convention (no change).
     */
    public static final String NORMAL = EnumValue.Naming.NORMAL.name().toLowerCase();

    /**
     * Represents the 'lower case' naming convention.
     */
    public static final String LOWER_CASE = EnumValue.Naming.LOWER_CASE.name().toLowerCase();

    /**
     * Represents the 'upper case' naming convention.
     */
    public static final String UPPER_CASE = EnumValue.Naming.UPPER_CASE.name().toLowerCase();

    /**
     * Represents the 'camel case to lower case with underscore' naming convention.
     */
    public static final String CAMEL_UNDERLINE_LOWER_CASE = EnumValue.Naming.CAMEL_UNDERLINE_LOWER_CASE.name()
            .toLowerCase();

    /**
     * Represents the 'camel case to upper case with underscore' naming convention.
     */
    public static final String CAMEL_UNDERLINE_UPPER_CASE = EnumValue.Naming.CAMEL_UNDERLINE_UPPER_CASE.name()
            .toLowerCase();

    /**
     * Configuration key for table prefix.
     */
    public static final String TABLE_PREFIX_KEY = "table.prefix";

    /**
     * Configuration key for tables to ignore for multi-tenancy.
     */
    public static final String TENANT_IGNORE_KEY = "tenant.ignore";
    /**
     * The name of the tenant table to be ignored.
     */
    public static final String TENANT_IGNORE_TABLE = "tenant";

    /**
     * Configuration key for the tenant ID column name.
     */
    public static final String TENANT_COLUMN_KEY = "tenant.column";

    /**
     * Default column name for the tenant ID.
     */
    public static final String TENANT_TABLE_COLUMN = "tenant_id";

    /**
     * Configuration key for the naming convention.
     */
    public static final String NAMING_KEY = "provider.naming";

    /**
     * Configuration key for enabling/disabling one-time caching.
     */
    public static final String USEONCE_KEY = "provider.useOnce";

    /**
     * Configuration key for the initial size of the cache.
     */
    public static final String INITSIZE_KEY = "provider.initSize";

    /**
     * Configuration key for concurrency level of primary key generation.
     */
    public static final String CONCURRENCY_KEY = "provider.concurrency";

    /**
     * Default name for the base result map.
     */
    public static final String RESULT_MAP_NAME = "SuperResultMap";

    /**
     * Dynamic SQL fragment for the SET clause in a Condition object.
     */
    public static final String CONDITION_SET_CLAUSE_INNER_WHEN = "<set>"
            + "  <foreach collection=\"condition.setValues\" item=\"setValue\">\n" + "    <choose>\n"
            + "      <when test=\"setValue.noValue\">\n" + "        ${setValue.condition},\n" + "      </when>\n"
            + "      <when test=\"setValue.singleValue\">\n"
            + "        ${setValue.condition} = ${setValue.variables('setValue.value')},\n" + "      </when>\n"
            + "    </choose>\n" + "  </foreach>\n" + "</set>";

    /**
     * Dynamic SQL fragment for the inner 'when' conditions within a WHERE clause for a Condition object.
     */
    public static final String CONDITION_WHERE_CLAUSE_INNER_WHEN = "              <when test=\"criterion.noValue\">\n"
            + "              AND ${criterion.condition}\n" + "            </when>\n"
            + "            <when test=\"criterion.singleValue\">\n"
            + "              AND ${criterion.condition} ${criterion.variables('criterion.value')}\n"
            + "            </when>\n" + "            <when test=\"criterion.betweenValue\">\n"
            + "              AND ${criterion.condition} ${criterion.variables('criterion.value')} AND\n"
            + "              ${criterion.variables('criterion.secondValue')}\n" + "            </when>\n"
            + "            <when test=\"criterion.listValue\">\n" + "              AND ${criterion.condition}\n"
            + "              <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\"\n"
            + "                open=\"(\" separator=\",\">\n" + "                ${criterion.variables('listItem')}\n"
            + "              </foreach>\n" + "            </when>\n";

    /**
     * Dynamic SQL WHERE clause for a Condition object, used when the Condition is passed as a parameter annotated with
     * {@code @Param("condition")}.
     */
    public static final String UPDATE_BY_CONDITION_WHERE_CLAUSE = "<where>\n"
            + "  <foreach collection=\"condition.oredCriteria\" item=\"criteria\"\n separator=\" OR \">\n"
            + "    <if test=\"criteria.valid\">\n" + "      <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n"
            + "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" + "          <choose>\n"
            + CONDITION_WHERE_CLAUSE_INNER_WHEN + "            <when test=\"criterion.orValue\">\n"
            + "              <foreach collection=\"criterion.value\" item=\"orCriteria\" separator=\" OR \" open = \" AND (\" close = \")\">\n"
            + "                <if test=\"orCriteria.valid\">\n"
            + "                  <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n"
            + "                    <foreach collection=\"orCriteria.criteria\" item=\"criterion\">\n"
            + "                      <choose>\n" + CONDITION_WHERE_CLAUSE_INNER_WHEN
            + "                      </choose>\n" + "                    </foreach>\n" + "                  </trim>\n"
            + "                </if>\n" + "              </foreach>\n" + "            </when>\n"
            + "          </choose>\n" + "        </foreach>\n" + "      </trim>\n" + "    </if>\n" + "  </foreach>\n"
            + "</where>\n";

    /**
     * Dynamic SQL WHERE clause for a Condition object, used when the interface method has only one Condition parameter.
     */
    public static final String CONDITION_WHERE_CLAUSE = "<where>\n"
            + "  <foreach collection=\"oredCriteria\" item=\"criteria\" separator=\" OR \">\n"
            + "    <if test=\"criteria.valid\">\n" + "      <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n"
            + "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" + "          <choose>\n"
            + CONDITION_WHERE_CLAUSE_INNER_WHEN + "            <when test=\"criterion.orValue\">\n"
            + "              <foreach collection=\"criterion.value\" item=\"orCriteria\" separator=\" OR \" open = \" AND (\" close = \")\">\n"
            + "                <if test=\"orCriteria.valid\">\n"
            + "                  <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n"
            + "                    <foreach collection=\"orCriteria.criteria\" item=\"criterion\">\n"
            + "                      <choose>\n" + CONDITION_WHERE_CLAUSE_INNER_WHEN
            + "                      </choose>\n" + "                    </foreach>\n" + "                  </trim>\n"
            + "                </if>\n" + "              </foreach>\n" + "            </when>\n"
            + "          </choose>\n" + "        </foreach>\n" + "      </trim>\n" + "    </if>\n" + "  </foreach>\n"
            + "</where>\n";

    /**
     * A set of simple types, including primitives, their wrapper classes, date/time types, etc.
     * <p>
     * Note: It is recommended to avoid using primitive types for database fields in entity classes, as they have
     * default values.
     * </p>
     */
    public static final Set<Class<?>> SIMPLE_TYPE_SET = new HashSet<>(Arrays.asList(
            byte.class,
            short.class,
            char.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            byte[].class,
            String.class,
            Byte.class,
            Short.class,
            Character.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Boolean.class,
            Date.class,
            Timestamp.class,
            Class.class,
            BigInteger.class,
            BigDecimal.class,
            Instant.class,
            LocalDateTime.class,
            LocalDate.class,
            LocalTime.class,
            OffsetDateTime.class,
            OffsetTime.class,
            ZonedDateTime.class,
            Year.class,
            Month.class,
            YearMonth.class));

}
