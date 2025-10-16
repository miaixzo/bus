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
package org.miaixz.bus.mapper.parsing;

import java.util.function.Supplier;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Caching;

/**
 * An interface for SQL scripts, providing a simple wrapper for XML-based SQL to facilitate usage.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SqlScript {

    /**
     * Creates and caches an SQL script.
     *
     * @param providerContext The execution method context.
     * @param sqlScript       The XML SQL script implementation.
     * @return The cache key.
     */
    static String caching(ProviderContext providerContext, SqlScript sqlScript) {
        TableMeta entity = MapperFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
        return Caching.cache(
                providerContext,
                entity,
                () -> String.format(
                        "<script>\n%s\n</script>",
                        SqlScriptWrapper.wrapSqlScript(providerContext, entity, sqlScript).getSql(entity)));
    }

    /**
     * Creates and caches an SQL script using an {@link EasySqlScript}.
     *
     * @param providerContext The execution method context.
     * @param sqlScript       The {@link EasySqlScript} implementation.
     * @return The cache key.
     */
    static String caching(ProviderContext providerContext, EasySqlScript sqlScript) {
        TableMeta entity = MapperFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
        return Caching.cache(
                providerContext,
                entity,
                () -> String.format(
                        "<script>\n%s\n</script>",
                        SqlScriptWrapper.wrapSqlScript(providerContext, entity, sqlScript).getSql(entity)));
    }

    /**
     * Generates an XML structure wrapped in a {@code <where>} tag.
     *
     * @param content The content within the tag.
     * @return The XML structure wrapped in a {@code <where>} tag.
     */
    default String where(LRSupplier content) {
        return String.format("\n<where>%s\n</where> ", content.getWithLR());
    }

    /**
     * Generates the corresponding SQL, supporting dynamic tags.
     *
     * @param entity The entity class information.
     * @return The XML SQL script.
     */
    String getSql(TableMeta entity);

    /**
     * Generates an XML structure wrapped in a {@code <choose>} tag.
     *
     * @param content The content within the tag.
     * @return The XML structure wrapped in a {@code <choose>} tag.
     */
    default String choose(LRSupplier content) {
        return String.format("\n<choose>%s\n</choose> ", content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in an {@code <otherwise>} tag.
     *
     * @param content The content within the tag.
     * @return The XML structure wrapped in an {@code <otherwise>} tag.
     */
    default String otherwise(LRSupplier content) {
        return String.format("\n<otherwise>%s\n</otherwise> ", content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <set>} tag.
     *
     * @param content The content within the tag.
     * @return The XML structure wrapped in a {@code <set>} tag.
     */
    default String set(LRSupplier content) {
        return String.format("\n<set>%s\n</set> ", content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in an {@code <if>} tag.
     *
     * @param test    The test condition for the {@code <if>} tag.
     * @param content The content within the tag.
     * @return The XML structure wrapped in an {@code <if>} tag.
     */
    default String ifTest(String test, LRSupplier content) {
        return String.format("<if test=\"%s\">%s\n</if> ", test, content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a tag, allowing for null parameters.
     *
     * @param content The content within the tag.
     * @return The XML structure wrapped in a tag.
     */
    default String ifParameterNotNull(LRSupplier content) {
        return String.format("<if test=\"_parameter != null\">%s\n</if> ", content.getWithLR());
    }

    /**
     * Adds a non-null validation for a parameter.
     *
     * @param message The prompt message.
     * @return The validation code snippet.
     */
    default String parameterNotNull(String message) {
        return variableNotNull("_parameter", message);
    }

    /**
     * Adds a validation that a boolean parameter is true.
     *
     * @param variable The parameter (a boolean value).
     * @param message  The prompt message.
     * @return The validation code snippet.
     */
    default String variableIsTrue(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@isTrue(" + variable + ", '" + message + "')}\n";
    }

    /**
     * Adds a validation that a boolean parameter is false.
     *
     * @param variable The parameter (a boolean value).
     * @param message  The prompt message.
     * @return The validation code snippet.
     */
    default String variableIsFalse(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@isFalse(" + variable + ", '" + message + "')}\n";
    }

    /**
     * Adds a non-null validation for a parameter.
     *
     * @param variable The parameter.
     * @param message  The prompt message.
     * @return The validation code snippet.
     */
    default String variableNotNull(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@notNull(" + variable + ", '" + message + "')}\n";
    }

    /**
     * Adds a non-empty validation for a parameter.
     *
     * @param variable The parameter.
     * @param message  The prompt message.
     * @return The validation code snippet.
     */
    default String variableNotEmpty(String variable, String message) {
        return "\n${@org.miaixz.bus.core.lang.Assert@notEmpty(" + variable + ", '" + message + "')}\n";
    }

    /**
     * Generates an XML structure wrapped in a {@code <when>} tag.
     *
     * @param test    The test condition for the {@code <when>} tag.
     * @param content The content within the tag.
     * @return The XML structure wrapped in a {@code <when>} tag.
     */
    default String whenTest(String test, LRSupplier content) {
        return String.format("\n<when test=\"%s\">%s\n</when> ", test, content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <trim>} tag.
     *
     * @param prefix          The prefix.
     * @param suffix          The suffix.
     * @param prefixOverrides The content to override prefixes.
     * @param suffixOverrides The content to override suffixes.
     * @param content         The content within the tag.
     * @return The XML structure wrapped in a {@code <trim>} tag.
     */
    default String trim(
            String prefix,
            String suffix,
            String prefixOverrides,
            String suffixOverrides,
            LRSupplier content) {
        return String.format(
                "\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ",
                prefix,
                prefixOverrides,
                suffixOverrides,
                suffix,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <trim>} tag (with prefix overrides only).
     *
     * @param prefix          The prefix.
     * @param suffix          The suffix.
     * @param prefixOverrides The content to override prefixes.
     * @param content         The content within the tag.
     * @return The XML structure wrapped in a {@code <trim>} tag.
     */
    default String trimPrefixOverrides(String prefix, String suffix, String prefixOverrides, LRSupplier content) {
        return String.format(
                "\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ",
                prefix,
                prefixOverrides,
                suffix,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <trim>} tag (with suffix overrides only).
     *
     * @param prefix          The prefix.
     * @param suffix          The suffix.
     * @param suffixOverrides The content to override suffixes.
     * @param content         The content within the tag.
     * @return The XML structure wrapped in a {@code <trim>} tag.
     */
    default String trimSuffixOverrides(String prefix, String suffix, String suffixOverrides, LRSupplier content) {
        return String.format(
                "\n<trim prefix=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ",
                prefix,
                suffixOverrides,
                suffix,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <foreach>} tag.
     *
     * @param collection The object to iterate over.
     * @param item       The name of the item variable.
     * @param content    The content within the tag.
     * @return The XML structure wrapped in a {@code <foreach>} tag.
     */
    default String foreach(String collection, String item, LRSupplier content) {
        return String.format(
                "\n<foreach collection=\"%s\" item=\"%s\">%s\n</foreach> ",
                collection,
                item,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <foreach>} tag (with a separator).
     *
     * @param collection The object to iterate over.
     * @param item       The name of the item variable.
     * @param separator  The separator to use between items.
     * @param content    The content within the tag.
     * @return The XML structure wrapped in a {@code <foreach>} tag.
     */
    default String foreach(String collection, String item, String separator, LRSupplier content) {
        return String.format(
                "\n<foreach collection=\"%s\" item=\"%s\" separator=\"%s\">%s\n</foreach> ",
                collection,
                item,
                separator,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <foreach>} tag (with opening and closing symbols).
     *
     * @param collection The object to iterate over.
     * @param item       The name of the item variable.
     * @param separator  The separator to use between items.
     * @param open       The opening symbol.
     * @param close      The closing symbol.
     * @param content    The content within the tag.
     * @return The XML structure wrapped in a {@code <foreach>} tag.
     */
    default String foreach(
            String collection,
            String item,
            String separator,
            String open,
            String close,
            LRSupplier content) {
        return String.format(
                "\n<foreach collection=\"%s\" item=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">%s\n</foreach> ",
                collection,
                item,
                open,
                close,
                separator,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <foreach>} tag (with an index).
     *
     * @param collection The object to iterate over.
     * @param item       The name of the item variable.
     * @param separator  The separator to use between items.
     * @param open       The opening symbol.
     * @param close      The closing symbol.
     * @param index      The name of the index variable (for lists, it's the index; for maps, it's the key).
     * @param content    The content within the tag.
     * @return The XML structure wrapped in a {@code <foreach>} tag.
     */
    default String foreach(
            String collection,
            String item,
            String separator,
            String open,
            String close,
            String index,
            LRSupplier content) {
        return String.format(
                "\n<foreach collection=\"%s\" item=\"%s\" index=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">%s\n</foreach> ",
                collection,
                item,
                index,
                open,
                close,
                separator,
                content.getWithLR());
    }

    /**
     * Generates an XML structure wrapped in a {@code <bind>} tag.
     *
     * @param name  The variable name.
     * @param value The variable value.
     * @return The XML structure wrapped in a {@code <bind>} tag.
     */
    default String bind(String name, String value) {
        return String.format("\n<bind name=\"%s\" value=\"%s\"/>", name, value);
    }

    /**
     * A functional interface for supplying a string, ensuring it starts with a newline character.
     */
    interface LRSupplier extends Supplier<String> {

        /**
         * Gets the string, ensuring it starts with a newline character.
         *
         * @return The string with a leading newline if necessary.
         */
        default String getWithLR() {
            String txt = get();
            if (!txt.isEmpty() && txt.charAt(0) == Symbol.LF.charAt(0)) {
                return txt;
            }
            return Symbol.LF + txt;
        }

    }

}
