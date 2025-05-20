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
package org.miaixz.bus.mapper.mapping;

import java.util.function.Supplier;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Caching;

/**
 * SQL 脚本接口，对 XML 形式 SQL 进行简单封装，便于使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SqlScript {

    /**
     * 创建 SQL 并缓存
     *
     * @param providerContext 执行方法上下文
     * @param sqlScript       XML SQL 脚本实现
     * @return 缓存 key
     */
    static String caching(ProviderContext providerContext, SqlScript sqlScript) {
        MapperTable entity = MapperFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
        return Caching.cache(providerContext, entity, () -> String.format("<script>\n%s\n</script>",
                SqlScriptWrapper.wrapSqlScript(providerContext, entity, sqlScript).getSql(entity)));
    }

    /**
     * 创建 SQL 并缓存
     *
     * @param providerContext 执行方法上下文
     * @param sqlScript       XML SQL 脚本实现
     * @return 缓存 key
     */
    static String caching(ProviderContext providerContext, SqlScript2 sqlScript) {
        MapperTable entity = MapperFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
        return Caching.cache(providerContext, entity, () -> String.format("<script>\n%s\n</script>",
                SqlScriptWrapper.wrapSqlScript(providerContext, entity, sqlScript).getSql(entity)));
    }

    /**
     * 生成 where 标签包装的 XML 结构
     *
     * @param content 标签中的内容
     * @return where 标签包装的 XML 结构
     */
    default String where(LRSupplier content) {
        return String.format("\n<where>%s\n</where> ", content.getWithLR());
    }

    /**
     * 生成对应的 SQL，支持动态标签
     *
     * @param entity 实体类信息
     * @return XML SQL 脚本
     */
    String getSql(MapperTable entity);

    /**
     * 生成 choose 标签包装的 XML 结构
     *
     * @param content 标签中的内容
     * @return choose 标签包装的 XML 结构
     */
    default String choose(LRSupplier content) {
        return String.format("\n<choose>%s\n</choose> ", content.getWithLR());
    }

    /**
     * 生成 otherwise 标签包装的 XML 结构
     *
     * @param content 标签中的内容
     * @return otherwise 标签包装的 XML 结构
     */
    default String otherwise(LRSupplier content) {
        return String.format("\n<otherwise>%s\n</otherwise> ", content.getWithLR());
    }

    /**
     * 生成 set 标签包装的 XML 结构
     *
     * @param content 标签中的内容
     * @return set 标签包装的 XML 结构
     */
    default String set(LRSupplier content) {
        return String.format("\n<set>%s\n</set> ", content.getWithLR());
    }

    /**
     * 生成 if 标签包装的 XML 结构
     *
     * @param test    if 的判断条件
     * @param content 标签中的内容
     * @return if 标签包装的 XML 结构
     */
    default String ifTest(String test, LRSupplier content) {
        return String.format("<if test=\"%s\">%s\n</if> ", test, content.getWithLR());
    }

    /**
     * 生成标签包装的 XML 结构，允许参数为空
     *
     * @param content 标签中的内容
     * @return 标签包装的 XML 结构
     */
    default String ifParameterNotNull(LRSupplier content) {
        return String.format("<if test=\"_parameter != null\">%s\n</if> ", content.getWithLR());
    }

    /**
     * 增加参数非空校验
     *
     * @param message 提示信息
     * @return 校验代码片段
     */
    default String parameterNotNull(String message) {
        return variableNotNull("_parameter", message);
    }

    /**
     * 增加参数为 true 的校验
     *
     * @param variable 参数，值为 boolean
     * @param message  提示信息
     * @return 校验代码片段
     */
    default String variableIsTrue(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@isTrue(" + variable + ", '" + message + "')}\n";
    }

    /**
     * 增加参数为 false 的校验
     *
     * @param variable 参数，值为 boolean
     * @param message  提示信息
     * @return 校验代码片段
     */
    default String variableIsFalse(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@isFalse(" + variable + ", '" + message + "')}\n";
    }

    /**
     * 增加参数非空的校验
     *
     * @param variable 参数
     * @param message  提示信息
     * @return 校验代码片段
     */
    default String variableNotNull(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@notNull(" + variable + ", '" + message + "')}\n";
    }

    /**
     * 增加参数非空的校验
     *
     * @param variable 参数
     * @param message  提示信息
     * @return 校验代码片段
     */
    default String variableNotEmpty(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@notEmpty(" + variable + ", '" + message + "')}\n";
    }

    /**
     * 生成 when 标签包装的 XML 结构
     *
     * @param test    when 的判断条件
     * @param content 标签中的内容
     * @return when 标签包装的 XML 结构
     */
    default String whenTest(String test, LRSupplier content) {
        return String.format("\n<when test=\"%s\">%s\n</when> ", test, content.getWithLR());
    }

    /**
     * 生成 trim 标签包装的 XML 结构
     *
     * @param prefix          前缀
     * @param suffix          后缀
     * @param prefixOverrides 前缀替换内容
     * @param suffixOverrides 后缀替换内容
     * @param content         标签中的内容
     * @return trim 标签包装的 XML 结构
     */
    default String trim(String prefix, String suffix, String prefixOverrides, String suffixOverrides,
            LRSupplier content) {
        return String.format(
                "\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ",
                prefix, prefixOverrides, suffixOverrides, suffix, content.getWithLR());
    }

    /**
     * 生成 trim 标签包装的 XML 结构（仅前缀替换）
     *
     * @param prefix          前缀
     * @param suffix          后缀
     * @param prefixOverrides 前缀替换内容
     * @param content         标签中的内容
     * @return trim 标签包装的 XML 结构
     */
    default String trimPrefixOverrides(String prefix, String suffix, String prefixOverrides, LRSupplier content) {
        return String.format("\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ", prefix,
                prefixOverrides, suffix, content.getWithLR());
    }

    /**
     * 生成 trim 标签包装的 XML 结构（仅后缀替换）
     *
     * @param prefix          前缀
     * @param suffix          后缀
     * @param suffixOverrides 后缀替换内容
     * @param content         标签中的内容
     * @return trim 标签包装的 XML 结构
     */
    default String trimSuffixOverrides(String prefix, String suffix, String suffixOverrides, LRSupplier content) {
        return String.format("\n<trim prefix=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ", prefix,
                suffixOverrides, suffix, content.getWithLR());
    }

    /**
     * 生成 foreach 标签包装的 XML 结构
     *
     * @param collection 遍历的对象
     * @param item       对象名
     * @param content    标签中的内容
     * @return foreach 标签包装的 XML 结构
     */
    default String foreach(String collection, String item, LRSupplier content) {
        return String.format("\n<foreach collection=\"%s\" item=\"%s\">%s\n</foreach> ", collection, item,
                content.getWithLR());
    }

    /**
     * 生成 foreach 标签包装的 XML 结构（带分隔符）
     *
     * @param collection 遍历的对象
     * @param item       对象名
     * @param separator  连接符
     * @param content    标签中的内容
     * @return foreach 标签包装的 XML 结构
     */
    default String foreach(String collection, String item, String separator, LRSupplier content) {
        return String.format("\n<foreach collection=\"%s\" item=\"%s\" separator=\"%s\">%s\n</foreach> ", collection,
                item, separator, content.getWithLR());
    }

    /**
     * 生成 foreach 标签包装的 XML 结构（带开闭符号）
     *
     * @param collection 遍历的对象
     * @param item       对象名
     * @param separator  连接符
     * @param open       开始符号
     * @param close      结束符号
     * @param content    标签中的内容
     * @return foreach 标签包装的 XML 结构
     */
    default String foreach(String collection, String item, String separator, String open, String close,
            LRSupplier content) {
        return String.format(
                "\n<foreach collection=\"%s\" item=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">%s\n</foreach> ",
                collection, item, open, close, separator, content.getWithLR());
    }

    /**
     * 生成 foreach 标签包装的 XML 结构（带索引）
     *
     * @param collection 遍历的对象
     * @param item       对象名
     * @param separator  连接符
     * @param open       开始符号
     * @param close      结束符号
     * @param index      索引名（list 为索引，map 为 key）
     * @param content    标签中的内容
     * @return foreach 标签包装的 XML 结构
     */
    default String foreach(String collection, String item, String separator, String open, String close, String index,
            LRSupplier content) {
        return String.format(
                "\n<foreach collection=\"%s\" item=\"%s\" index=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">%s\n</foreach> ",
                collection, item, index, open, close, separator, content.getWithLR());
    }

    /**
     * 生成 bind 标签包装的 XML 结构
     *
     * @param name  变量名
     * @param value 变量值
     * @return bind 标签包装的 XML 结构
     */
    default String bind(String name, String value) {
        return String.format("\n<bind name=\"%s\" value=\"%s\"/>", name, value);
    }

    /**
     * 确保字符串前有换行符的 Supplier 接口
     */
    interface LRSupplier extends Supplier<String> {

        /**
         * 获取带换行符的字符串
         *
         * @return 确保字符串前面有换行符
         */
        default String getWithLR() {
            String txt = get();
            if (!txt.isEmpty() && txt.charAt(0) == Symbol.LF.charAt(0)) {
                return txt;
            }
            return Symbol.LF + txt;
        }

    }

    /**
     * 支持简便写法的 SQL 脚本接口
     */
    interface SqlScript2 extends SqlScript {

        /**
         * 默认实现，委托给 getSql(entity, util)
         *
         * @param entity 实体类信息
         * @return XML SQL 脚本
         */
        @Override
        default String getSql(MapperTable entity) {
            return getSql(entity, this);
        }

        /**
         * 生成对应的 SQL，支持动态标签
         *
         * @param entity 实体类信息
         * @param util   当前对象的引用，便于在 lambda 中使用当前对象的方法
         * @return XML SQL 脚本
         */
        String getSql(MapperTable entity, SqlScript util);

    }

}