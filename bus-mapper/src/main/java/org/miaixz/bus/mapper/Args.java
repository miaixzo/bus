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
 * 常量信息类，定义 MyBatis 相关配置和 SQL 片段。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Args {

    /**
     * Getter 方法正则表达式
     */
    public static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");

    /**
     * is 方法正则表达式
     */
    public static final Pattern IS_PATTERN = Pattern.compile("^is[A-Z].*");

    /**
     * 实例化类正则表达式
     */
    public static final Pattern CLASS_PATTERN = Pattern.compile("\\(L(?<cls>.+);\\).+");

    /**
     * 字段分隔符正则表达式
     */
    public static final Pattern DELIMITER = Pattern.compile("^[`\\[\"]?(.*?)[`\\]\"]?$");

    /**
     * 默认名转换
     */
    public static final String NORMAL = EnumValue.Naming.NORMAL.name().toLowerCase();

    /**
     * 转换为小写
     */
    public static final String LOWER_CASE = EnumValue.Naming.LOWER_CASE.name().toLowerCase();

    /**
     * 转换为大写
     */
    public static final String UPPER_CASE = EnumValue.Naming.UPPER_CASE.name().toLowerCase();

    /**
     * 驼峰转小写下划线
     */
    public static final String CAMEL_UNDERLINE_LOWER_CASE = EnumValue.Naming.CAMEL_UNDERLINE_LOWER_CASE.name()
            .toLowerCase();

    /**
     * 驼峰转大写下划线
     */
    public static final String CAMEL_UNDERLINE_UPPER_CASE = EnumValue.Naming.CAMEL_UNDERLINE_UPPER_CASE.name()
            .toLowerCase();

    /**
     * 表前缀
     */
    public static final String TABLE_PREFIX_KEY = "table.prefix";

    /**
     * 命名规则
     */
    public static final String NAMING_KEY = "provider.naming";

    /**
     * 是否一次缓存
     */
    public static final String USEONCE_KEY = "provider.useOnce";

    /**
     * 缓存初始大小
     */
    public static final String INITSIZE_KEY = "provider.initSize";

    /**
     * 主键生成并发
     */
    public static final String CONCURRENCY_KEY = "provider.concurrency";

    /**
     * 默认结果映射名称
     */
    public static final String RESULT_MAP_NAME = "SuperResultMap";

    /**
     * Condition 结构动态 SET 子句 SQL 片段
     */
    public static final String CONDITION_SET_CLAUSE_INNER_WHEN = "<set>"
            + "  <foreach collection=\"condition.setValues\" item=\"setValue\">\n" + "    <choose>\n"
            + "      <when test=\"setValue.noValue\">\n" + "        ${setValue.condition},\n" + "      </when>\n"
            + "      <when test=\"setValue.singleValue\">\n"
            + "        ${setValue.condition} = ${setValue.variables('setValue.value')},\n" + "      </when>\n"
            + "    </choose>\n" + "  </foreach>\n" + "</set>";

    /**
     * Condition 结构动态 WHERE 子句条件 SQL 片段
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
     * Condition 结构的动态 SQL WHERE 子句，用于多个参数时，Condition 使用 @Param("condition") 注解。
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
     * Condition 结构的动态 SQL WHERE 子句，用于接口参数仅包含一个 Condition 对象的情况。
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
     * 简单类型集合，包含基本类型及其包装类、日期类型等。
     * <p>
     * 注意：由于基本类型有默认值，建议在实体类中避免使用基本类型作为数据库字段类型。
     * </p>
     */
    public static final Set<Class<?>> SIMPLE_TYPE_SET = new HashSet<>(Arrays.asList(byte.class, short.class, char.class,
            int.class, long.class, float.class, double.class, boolean.class, byte[].class, String.class, Byte.class,
            Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class,
            Date.class, Timestamp.class, Class.class, BigInteger.class, BigDecimal.class, Instant.class,
            LocalDateTime.class, LocalDate.class, LocalTime.class, OffsetDateTime.class, OffsetTime.class,
            ZonedDateTime.class, Year.class, Month.class, YearMonth.class));

}