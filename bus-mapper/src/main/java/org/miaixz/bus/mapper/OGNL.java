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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.support.ClassColumn;
import org.miaixz.bus.mapper.support.ClassField;

/**
 * OGNL 静态方法工具类，提供类型注册、SPI 实例获取及函数式字段名转换功能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OGNL {

    /**
     * SQL 语法检查正则，匹配包含两个关键字（有先后顺序）的 SQL 语句
     */
    public static final Pattern SQL_SYNTAX_PATTERN = Pattern.compile(
            "(?i)(?:\\b(insert\\s+into\\s+\\w+|delete\\s+from\\s+\\w+|update\\s+\\w+\\s*(?:set\\s+.*)?|create\\s+(table|database|view|index|procedure|trigger)\\s+\\w+|drop\\s+(table|database|view|index)\\s+\\w+|truncate\\s+table\\s+\\w+|grant\\s+.*\\s+on\\s+.*|alter\\s+(table|database)\\s+\\w+|deny\\s+.*\\s+on\\s+.*|revoke\\s+.*\\s+on\\s+.*|call\\s+\\w+|execute\\s+\\w+|exec\\s+\\w+|declare\\s+@\\w+|show\\s+(databases|tables|columns)|rename\\s+table\\s+\\w+|set\\s+password|union\\s+select\\s+.*|insert\\s+\\w+)(?:\\s*(?:;|\\/\\*|\\-\\-)|\\b)|\\b(if|substr|substring|char|concat|benchmark|sleep)\\s*\\([^)]*\\)|\\b(or|and)\\s+['\"0-1]=['\"0-1])",
            Pattern.CASE_INSENSITIVE);

    /**
     * SQL 注释截断检查正则，匹配包含单引号、注释或分号的 SQL 语句
     */
    public static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("'.*(or|union|--|#|/\\*|;)",
            Pattern.CASE_INSENSITIVE);

    /**
     * 注册新的简单类型。
     *
     * @param clazz 要注册的类型
     */
    public static void registerSimpleType(Class<?> clazz) {
        Args.SIMPLE_TYPE_SET.add(clazz);
    }

    /**
     * 批量注册简单类型，通过逗号分隔的类名字符串。
     *
     * @param classes 类名字符串，格式为全限定类名，逗号分隔
     * @throws RuntimeException 如果类名无效或无法找到
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
     * 静默注册简单类型，忽略类不存在的异常。
     *
     * @param clazz 类名
     */
    public static void registerSimpleTypeSilence(String clazz) {
        try {
            Args.SIMPLE_TYPE_SET.add(Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            Logger.debug("Class not found, ignored: " + clazz);
        }
    }

    /**
     * 判断指定类是否为已知的简单类型。
     *
     * @param clazz 要检查的类
     * @return 如果是简单类型则返回 true，否则返回 false
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return Args.SIMPLE_TYPE_SET.contains(clazz);
    }

    /**
     * 获取指定接口或类的所有 SPI 实现实例，并按 ORDER 接口的顺序排序（如果适用）。
     *
     * @param clazz 接口或类
     * @param <T>   类型参数
     * @return 按顺序排列的实现实例列表
     */
    public static <T> List<T> getInstances(Class<T> clazz) {
        List<T> list = NormalSpiLoader.loadList(false, clazz);
        if (list.size() > 1 && ORDER.class.isAssignableFrom(clazz)) {
            list.sort(Comparator.comparing(f -> ((ORDER) f).order()).reversed());
        }
        return list;
    }

    /**
     * 将函数式接口 Fn 转换为对应的字段名或列名。
     *
     * @param fn 函数式接口实例
     * @return 包含类和字段名/列名的 ClassField 或 ClassColumn 对象
     * @throws RuntimeException 如果反射操作失败
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
     * 检查参数是否存在 SQL 注入风险。
     *
     * @param value 要检查的参数
     * @return 如果存在 SQL 注入风险返回 true，否则返回 false
     * @throws NullPointerException 如果 value 为 null
     */
    public static boolean validateSql(String value) {
        Objects.requireNonNull(value);
        return SQL_COMMENT_PATTERN.matcher(value).find() || SQL_SYNTAX_PATTERN.matcher(value).find();
    }

    /**
     * 删除字段中的转义字符（单引号和双引号）。
     *
     * @param text 待处理的字段
     * @return 删除转义字符后的字段值
     * @throws NullPointerException 如果 text 为 null
     */
    public static String removeEscapeCharacter(String text) {
        Objects.requireNonNull(text);
        return text.replaceAll("\"", "").replaceAll("'", "");
    }

    /**
     * 生成包含 if 标签的 SQL 脚本。
     *
     * @param sqlScript SQL 脚本片段
     * @param ifTest    if 标签的 test 条件
     * @param newLine   是否在新行包裹脚本
     * @return 包含 if 标签的 SQL 脚本
     */
    public static String convertIf(final String sqlScript, final String ifTest, boolean newLine) {
        String newSqlScript = sqlScript;
        if (newLine) {
            newSqlScript = Symbol.LF + newSqlScript + Symbol.LF;
        }
        return String.format("<if test=\"%s\">%s</if>", ifTest, newSqlScript);
    }

    /**
     * 生成包含 trim 标签的 SQL 脚本。
     *
     * @param sqlScript       SQL 脚本片段
     * @param prefix          前缀
     * @param suffix          后缀
     * @param prefixOverrides 要移除的前缀
     * @param suffixOverrides 要移除的后缀
     * @return 包含 trim 标签的 SQL 脚本
     */
    public static String convertTrim(final String sqlScript, final String prefix, final String suffix,
            final String prefixOverrides, final String suffixOverrides) {
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
     * 生成包含 choose 标签的 SQL 脚本。
     *
     * @param whenTest      when 标签的 test 条件
     * @param whenSqlScript when 条件成立时的 SQL 脚本
     * @param otherwise     otherwise 标签的内容
     * @return 包含 choose 标签的 SQL 脚本
     */
    public static String convertChoose(final String whenTest, final String whenSqlScript, final String otherwise) {
        return "<choose>" + Symbol.LF + "<when test=\"" + whenTest + Symbol.SINGLE_QUOTE + Symbol.GT + Symbol.LF
                + whenSqlScript + Symbol.LF + "</when>" + Symbol.LF + "<otherwise>" + otherwise + "</otherwise>"
                + Symbol.LF + "</choose>";
    }

    /**
     * 生成包含 foreach 标签的 SQL 脚本。
     *
     * @param sqlScript  foreach 内部的 SQL 脚本
     * @param collection 集合名称
     * @param index      索引名称
     * @param item       元素名称
     * @param separator  分隔符
     * @return 包含 foreach 标签的 SQL 脚本
     */
    public static String convertForeach(final String sqlScript, final String collection, final String index,
            final String item, final String separator) {
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
     * 生成包含 where 标签的 SQL 脚本。
     *
     * @param sqlScript where 内部的 SQL 脚本
     * @return 包含 where 标签的 SQL 脚本
     */
    public static String convertWhere(final String sqlScript) {
        return "<where>" + Symbol.LF + sqlScript + Symbol.LF + "</where>";
    }

    /**
     * 生成包含 set 标签的 SQL 脚本。
     *
     * @param sqlScript set 内部的 SQL 脚本
     * @return 包含 set 标签的 SQL 脚本
     */
    public static String convertSet(final String sqlScript) {
        return "<set>" + Symbol.LF + sqlScript + Symbol.LF + "</set>";
    }

    /**
     * 生成安全的 MyBatis 参数占位符（#{param}）。
     *
     * @param param 参数名称
     * @return 安全的参数占位符脚本
     */
    public static String safeParam(final String param) {
        return safeParam(param, null);
    }

    /**
     * 生成安全的 MyBatis 参数占位符（#{param,mapping}）。
     *
     * @param param   参数名称
     * @param mapping 参数映射配置
     * @return 安全的参数占位符脚本
     */
    public static String safeParam(final String param, final String mapping) {
        String target = Symbol.HASH_LEFT_BRACE + param;
        if (StringKit.isBlank(mapping)) {
            return target + Symbol.C_BRACE_RIGHT;
        }
        return target + Symbol.COMMA + mapping + Symbol.C_BRACE_RIGHT;
    }

    /**
     * 生成非安全的 MyBatis 参数占位符（${param}）。
     *
     * @param param 参数名称
     * @return 非安全的参数占位符脚本
     */
    public static String unSafeParam(final String param) {
        return Symbol.DOLLAR_LEFT_BRACE + param + Symbol.C_BRACE_RIGHT;
    }

    /**
     * 生成 TypeHandler 的映射配置。
     *
     * @param typeHandler TypeHandler 类
     * @return TypeHandler 映射配置字符串，若 typeHandler 为 null 则返回 null
     */
    public static String mappingTypeHandler(Class<? extends TypeHandler<?>> typeHandler) {
        if (typeHandler != null) {
            return "typeHandler=" + typeHandler.getName();
        }
        return null;
    }

    /**
     * 生成 JdbcType 的映射配置。
     *
     * @param jdbcType JdbcType 类型
     * @return JdbcType 映射配置字符串，若 jdbcType 为 null 则返回 null
     */
    public static String mappingJdbcType(JdbcType jdbcType) {
        if (jdbcType != null) {
            return "jdbcType=" + jdbcType.name();
        }
        return null;
    }

    /**
     * 生成数字精度的映射配置。
     *
     * @param numericScale 数字精度
     * @return 数字精度映射配置字符串，若 numericScale 为 null 则返回 null
     */
    public static String mappingNumericScale(Integer numericScale) {
        if (numericScale != null) {
            return "numericScale=" + numericScale;
        }
        return null;
    }

    /**
     * 组合 TypeHandler、JdbcType 和数字精度的映射配置。
     *
     * @param typeHandler  TypeHandler 类
     * @param jdbcType     JdbcType 类型
     * @param numericScale 数字精度
     * @return 组合的映射配置字符串，若所有参数为 null 则返回 null
     */
    public static String convertParamMapping(Class<? extends TypeHandler<?>> typeHandler, JdbcType jdbcType,
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
     * 拼接映射配置项。
     *
     * @param mapping 当前映射配置
     * @param other   要追加的映射配置
     * @return 拼接后的映射配置字符串
     */
    private static String appendMapping(String mapping, String other) {
        if (mapping != null) {
            return mapping + Symbol.COMMA + other;
        }
        return other;
    }

}