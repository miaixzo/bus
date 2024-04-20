/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.text.replacer;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Replacer;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.Lookups;
import org.miaixz.bus.core.text.Matchers;
import org.miaixz.bus.core.text.TextBuilder;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.Serializable;
import java.util.*;

/**
 * 按值替换字符串中的变量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivacyReplacer implements Replacer<Object>, Serializable {

    /**
     * 默认变量前缀
     */
    public static final Matchers DEFAULT_PREFIX = Matchers.stringMatcher(Symbol.DOLLAR + Symbol.BRACE_LEFT);
    /**
     * 默认变量后缀
     */
    public static final Matchers DEFAULT_SUFFIX = Matchers.stringMatcher(Symbol.BRACE_RIGHT);
    /**
     * 默认值分隔符
     */
    public static final Matchers DEFAULT_VALUE_DELIMITER = Matchers.stringMatcher(Symbol.COLON + Symbol.MINUS);
    private static final long serialVersionUID = 1L;
    /**
     * 存储转义字符
     */
    private char escapeChar;
    /**
     * 存储变量前缀
     */
    private Matchers prefixMatcher;
    /**
     * 存储变量后缀
     */
    private Matchers suffixMatcher;
    /**
     * 存储默认变量值分隔符
     */
    private Matchers valueDelimiterMatcher;
    /**
     * 变量解析被委托给VariableResolver的实现程序
     */
    private Lookups<?> variableResolver;
    /**
     * 标记是否启用变量名中的替换.
     */
    private boolean enableSubstitutionInVariables;
    /**
     * 是否应该保留转义 默认false;
     */
    private boolean preserveEscapes = false;

    /**
     * 默认值为变量前缀和后缀以及转义字符.
     */
    public PrivacyReplacer() {
        this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化它 对变量使用默认值
     * 前缀和后缀以及转义字符
     *
     * @param <V>      映射中值的类型
     * @param valueMap 带有变量值的映射可能为null
     */
    public <V> PrivacyReplacer(final Map<String, V> valueMap) {
        this(Lookups.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化它 使用默认转义字符
     *
     * @param <V>      映射中值的类型
     * @param valueMap 带有变量值的映射可能为null
     * @param prefix   变量的前缀,而不是null
     * @param suffix   变量的后缀,而不是null
     */
    public <V> PrivacyReplacer(final Map<String, V> valueMap, final String prefix, final String suffix) {
        this(Lookups.mapLookup(valueMap), prefix, suffix, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param <V>      映射中值的类型
     * @param valueMap 带有变量值的映射可能为null
     * @param prefix   变量的前缀,而不是null
     * @param suffix   变量的后缀,而不是null
     * @param escape   转义字符
     */
    public <V> PrivacyReplacer(final Map<String, V> valueMap, final String prefix, final String suffix,
                               final char escape) {
        this(Lookups.mapLookup(valueMap), prefix, suffix, escape);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param <V>       映射中值的类型
     * @param valueMap  带有变量值的映射可能为null
     * @param prefix    变量的前缀,而不是null
     * @param suffix    变量的后缀,而不是null
     * @param escape    转义字符
     * @param delimiter 变量默认值分隔符可以为空
     */
    public <V> PrivacyReplacer(final Map<String, V> valueMap, final String prefix, final String suffix,
                               final char escape, final String delimiter) {
        this(Lookups.mapLookup(valueMap), prefix, suffix, escape, delimiter);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver 变量解析器可以为空
     */
    public PrivacyReplacer(final Lookups<?> resolver) {
        this(resolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver 变量解析器可以为空
     * @param prefix   变量的前缀,而不是null
     * @param suffix   变量的后缀,而不是null
     * @param escape   转义字符
     */
    public PrivacyReplacer(final Lookups<?> resolver, final String prefix, final String suffix,
                           final char escape) {
        this.setVariableResolver(resolver);
        this.setVariablePrefix(prefix);
        this.setVariableSuffix(suffix);
        this.setEscapeChar(escape);
        this.setValueDelimiterMatcher(DEFAULT_VALUE_DELIMITER);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver  变量解析器可以为空
     * @param prefix    变量的前缀,而不是null
     * @param suffix    变量的后缀,而不是null
     * @param escape    转义字符
     * @param delimiter 变量默认值分隔符可以为空
     */
    public PrivacyReplacer(final Lookups<?> resolver, final String prefix, final String suffix,
                           final char escape, final String delimiter) {
        this.setVariableResolver(resolver);
        this.setVariablePrefix(prefix);
        this.setVariableSuffix(suffix);
        this.setEscapeChar(escape);
        this.setValueDelimiter(delimiter);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver      变量解析器,可以为空
     * @param prefixMatcher 前缀解析器,而不是null
     * @param suffixMatcher 后缀解析器,而不是null
     * @param escape        转义转义字符
     */
    public PrivacyReplacer(
            final Lookups<?> resolver, final Matchers prefixMatcher, final Matchers suffixMatcher,
            final char escape) {
        this(resolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver      变量解析器,可以为空
     * @param prefixMatcher 前缀解析器,而不是null
     * @param suffixMatcher 后缀解析器,而不是null
     * @param escape        转义转义字符
     * @param delimiter     变量默认值分隔符可以为空
     */
    public PrivacyReplacer(
            final Lookups<?> resolver, final Matchers prefixMatcher, final Matchers suffixMatcher,
            final char escape, final Matchers delimiter) {
        this.setVariableResolver(resolver);
        this.setVariablePrefixMatcher(prefixMatcher);
        this.setVariableSuffixMatcher(suffixMatcher);
        this.setEscapeChar(escape);
        this.setValueDelimiterMatcher(delimiter);
    }

    /**
     * 替换给定源对象中出现的所有变量
     * 它们在映射中的匹配值
     *
     * @param <V>      映射中值的类型
     * @param source   包含要替换的变量的源文本
     * @param valueMap 映射的值可能为空
     * @return 替换操作的结果
     */
    public static <V> String replace(final Object source, final Map<String, V> valueMap) {
        return new PrivacyReplacer(valueMap).replace(source);
    }

    /**
     * 将给定源对象中出现的所有变量替换
     * 它们在映射中匹配的值 这种方法允许指定自定义变量前缀和后缀
     *
     * @param <V>      映射中值的类型
     * @param source   包含要替换的变量的源文本
     * @param valueMap 映射的值可能为空
     * @param prefix   变量的前缀,而不是null
     * @param suffix   变量的后缀,而不是null
     * @return 替换操作的结果
     */
    public static <V> String replace(final Object source, final Map<String, V> valueMap, final String prefix, final String suffix) {
        return new PrivacyReplacer(valueMap, prefix, suffix).replace(source);
    }

    /**
     * 用匹配的变量替换给定源对象中出现的所有变量
     *
     * @param source 包含要替换的变量的源文本
     * @param value  带值的属性可以为空
     * @return 替换操作的结果
     */
    public static String replace(final Object source, final Properties value) {
        if (null == value) {
            return source.toString();
        }
        final Map<String, String> valueMap = new HashMap<>();
        final Enumeration<?> propNames = value.propertyNames();
        while (propNames.hasMoreElements()) {
            final String propName = (String) propNames.nextElement();
            final String propValue = value.getProperty(propName);
            valueMap.put(propName, propValue);
        }
        return PrivacyReplacer.replace(source, valueMap);
    }

    /**
     * 替换给定源对象中出现的所有变量
     * 它们在系统属性中匹配的值
     *
     * @param source 包含要替换的变量的源文本
     * @return 返回替换操作的结果
     */
    public static String replaceSystemProperties(final Object source) {
        return new PrivacyReplacer(Lookups.systemPropertiesLookup()).replace(source);
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    @Override
    public String replace(final Object source) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder().append(source);
        substitute(buf, 0, buf.length());
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public String replace(final TextBuilder source) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(source.length()).append(source);
        substitute(buf, 0, buf.length());
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     * 从使用给定源字符串作为模板的解析器
     *
     * @param source 获取要替换的字符串
     * @return 替换操作的结果
     */
    public String replace(final String source) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(source);
        if (substitute(buf, 0, source.length()) == false) {
            return source;
        }
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     * 从使用给定源数组作为模板的解析器
     * 该方法不改变数组
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 返回替换操作的结果
     */
    public String replace(final char[] source) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(source.length).append(source);
        substitute(buf, 0, source.length);
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符串
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 偏移数组中的起始偏移必须有效
     * @return 替换操作的结果
     */
    public String replace(final String source, final int offset, final int length) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        if (substitute(buf, 0, length) == false) {
            return source.substring(offset, offset + length);
        }
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 替换操作的结果
     */
    public String replace(final char[] source, final int offset, final int length) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }

    /**
     * 用匹配值替换所有出现的变量
     * 从使用给定源缓冲区作为模板的解析器
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public String replace(final StringBuffer source) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(source.length()).append(source);
        substitute(buf, 0, buf.length());
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 源缓冲区用作模板,没有更改
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 返回替换操作的结果
     */
    public String replace(final StringBuffer source, final int offset, final int length) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 源缓冲区用作模板,没有更改
     * @return 返回替换操作的结果
     */
    public String replace(final CharSequence source) {
        if (null == source) {
            return null;
        }
        return replace(source, 0, source.length());
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 替换操作的结果
     */
    public String replace(final CharSequence source, final int offset, final int length) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 替换操作的结果
     */
    public String replace(final TextBuilder source, final int offset, final int length) {
        if (null == source) {
            return null;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public boolean replaceIn(final StringBuffer source) {
        if (null == source) {
            return false;
        }
        return replaceIn(source, 0, source.length());
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 替换操作的结果
     */
    public boolean replaceIn(final StringBuffer source, final int offset, final int length) {
        if (null == source) {
            return false;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        if (substitute(buf, 0, length) == false) {
            return false;
        }
        source.replace(offset, offset + length, buf.toString());
        return true;
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public boolean replaceIn(final StringBuilder source) {
        if (null == source) {
            return false;
        }
        return replaceIn(source, 0, source.length());
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 替换操作的结果
     */
    public boolean replaceIn(final StringBuilder source, final int offset, final int length) {
        if (null == source) {
            return false;
        }
        final TextBuilder buf = new TextBuilder(length).append(source, offset, length);
        if (substitute(buf, 0, length) == false) {
            return false;
        }
        source.replace(offset, offset + length, buf.toString());
        return true;
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public boolean replaceIn(final TextBuilder source) {
        if (null == source) {
            return false;
        }
        return substitute(source, 0, source.length());
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 替换操作的结果
     */
    public boolean replaceIn(final TextBuilder source, final int offset, final int length) {
        if (null == source) {
            return false;
        }
        return substitute(source, offset, length);
    }

    /**
     * 替换变量的内部方法
     *
     * @param buffer 要替换为的字符串生成器,而不是null
     * @param offset 构造器中的起始偏移量必须有效
     * @param length 要处理的构建器中的长度必须是有效的
     * @return true/false
     */
    protected boolean substitute(final TextBuilder buffer, final int offset, final int length) {
        return substitute(buffer, offset, length, null) > 0;
    }

    /**
     * 多层插值的递归处理程序
     *
     * @param buffer         要替换为的字符串生成器,而不是null
     * @param offset         构造器中的起始偏移量必须有效
     * @param length         要处理的构建器中的长度必须是有效的
     * @param priorVariables 保存被替换变量的堆栈可以为空
     * @return 发生的长度更改, 除非priorVariables在int时为null 表示布尔标志,表示是否发生了更改
     */
    private int substitute(final TextBuilder buffer, final int offset, final int length, List<String> priorVariables) {
        final Matchers pfxMatcher = getVariablePrefixMatcher();
        final Matchers suffMatcher = getVariableSuffixMatcher();
        final char escape = getEscapeChar();
        final Matchers valueDelimMatcher = getValueDelimiterMatcher();
        final boolean substitutionInVariablesEnabled = isEnableSubstitutionInVariables();

        final boolean top = null == priorVariables;
        boolean altered = false;
        int lengthChange = 0;
        char[] chars = buffer.buffer;
        int bufEnd = offset + length;
        int pos = offset;
        while (pos < bufEnd) {
            final int startMatchLen = pfxMatcher.isMatch(chars, pos, offset,
                    bufEnd);
            if (startMatchLen == 0) {
                pos++;
            } else {
                if (pos > offset && chars[pos - 1] == escape) {
                    if (preserveEscapes) {
                        pos++;
                        continue;
                    }
                    buffer.deleteCharAt(pos - 1);
                    chars = buffer.buffer;
                    lengthChange--;
                    altered = true;
                    bufEnd--;
                } else {
                    final int startPos = pos;
                    pos += startMatchLen;
                    int endMatchLen = 0;
                    int nestedVarCount = 0;
                    while (pos < bufEnd) {
                        if (substitutionInVariablesEnabled
                                && (endMatchLen = pfxMatcher.isMatch(chars,
                                pos, offset, bufEnd)) != 0) {
                            nestedVarCount++;
                            pos += endMatchLen;
                            continue;
                        }

                        endMatchLen = suffMatcher.isMatch(chars, pos, offset,
                                bufEnd);
                        if (endMatchLen == 0) {
                            pos++;
                        } else {
                            if (nestedVarCount == 0) {
                                String varNameExpr = new String(chars, startPos
                                        + startMatchLen, pos - startPos
                                        - startMatchLen);
                                if (substitutionInVariablesEnabled) {
                                    final TextBuilder bufName = new TextBuilder(varNameExpr);
                                    substitute(bufName, 0, bufName.length());
                                    varNameExpr = bufName.toString();
                                }
                                pos += endMatchLen;
                                final int endPos = pos;

                                String varName = varNameExpr;
                                String varDefaultValue = null;

                                if (null != valueDelimMatcher) {
                                    final char[] varNameExprChars = varNameExpr.toCharArray();
                                    int valueDelimiterMatchLen = 0;
                                    for (int i = 0; i < varNameExprChars.length; i++) {
                                        if (!substitutionInVariablesEnabled
                                                && pfxMatcher.isMatch(varNameExprChars, i, i, varNameExprChars.length) != 0) {
                                            break;
                                        }
                                        if ((valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars, i)) != 0) {
                                            varName = varNameExpr.substring(0, i);
                                            varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                                            break;
                                        }
                                    }
                                }

                                if (null == priorVariables) {
                                    priorVariables = new ArrayList<>();
                                    priorVariables.add(new String(chars,
                                            offset, length));
                                }

                                checkCyclicSubstitution(varName, priorVariables);
                                priorVariables.add(varName);

                                String varValue = resolveVariable(varName, buffer,
                                        startPos, endPos);
                                if (null == varValue) {
                                    varValue = varDefaultValue;
                                }
                                if (null != varValue) {
                                    final int varLen = varValue.length();
                                    buffer.replace(startPos, endPos, varValue);
                                    altered = true;
                                    int change = substitute(buffer, startPos,
                                            varLen, priorVariables);
                                    change = change
                                            + varLen - (endPos - startPos);
                                    pos += change;
                                    bufEnd += change;
                                    lengthChange += change;
                                    chars = buffer.buffer;
                                }

                                priorVariables
                                        .remove(priorVariables.size() - 1);
                                break;
                            }
                            nestedVarCount--;
                            pos += endMatchLen;
                        }
                    }
                }
            }
        }
        if (top) {
            return altered ? 1 : 0;
        }
        return lengthChange;
    }

    /**
     * 检查指定的变量是否已经在变量的堆栈
     *
     * @param varName        要检查的变量名
     * @param priorVariables 先验变量列表
     */
    private void checkCyclicSubstitution(final String varName, final List<String> priorVariables) {
        if (priorVariables.contains(varName) == false) {
            return;
        }
        final TextBuilder buf = new TextBuilder(Normal._256);
        buf.append("Infinite loop in property interpolation of ");
        buf.append(priorVariables.remove(0));
        buf.append(": ");
        buf.appendWithSeparators(priorVariables, "->");
        throw new IllegalStateException(buf.toString());
    }

    /**
     * 解析变量值的内部方法
     * {@link #getVariableResolver()},其中变量名作为键
     *
     * @param variableName 变量名,而不是null
     * @param buf          是发生替换的缓冲区,而不是null
     * @param startPos     变量的起始位置,包括前缀,有效
     * @param endPos       变量的结束位置,包括后缀,有效
     * @return 返回变量的值, 如果变量未知, 则null
     */
    protected String resolveVariable(final String variableName, final TextBuilder buf, final int startPos, final int endPos) {
        final Lookups<?> resolver = getVariableResolver();
        if (null == resolver) {
            return null;
        }
        return resolver.lookup(variableName);
    }

    /**
     * 返回转义字符.
     *
     * @return 用于转义变量引用的字符
     */
    public char getEscapeChar() {
        return this.escapeChar;
    }

    /**
     * 设置转义字符
     * 如果该字符放在源中的变量引用之前,这个变量将被忽略
     *
     * @param escapeCharacter 转义字符(0表示禁用转义)
     */
    public void setEscapeChar(final char escapeCharacter) {
        this.escapeChar = escapeCharacter;
    }

    /**
     * 获取当前使用的变量前缀匹配器
     *
     * @return 正在使用的前缀匹配器
     */
    public Matchers getVariablePrefixMatcher() {
        return prefixMatcher;
    }

    /**
     * 设置当前使用的变量前缀匹配器
     *
     * @param prefixMatcher 前缀匹配器,null被忽略
     * @return this, 以启用链接
     */
    public PrivacyReplacer setVariablePrefixMatcher(final Matchers prefixMatcher) {
        if (null == prefixMatcher) {
            throw new IllegalArgumentException("Variable prefix matcher must not be null!");
        }
        this.prefixMatcher = prefixMatcher;
        return this;
    }

    /**
     * 设置要使用的变量前缀
     *
     * @param prefix 要使用的前缀字符
     * @return this, 以启用链接
     */
    public PrivacyReplacer setVariablePrefix(final char prefix) {
        return setVariablePrefixMatcher(Matchers.charMatcher(prefix));
    }

    /**
     * 设置要使用的变量前缀
     *
     * @param prefix 变量的前缀,而不是null
     * @return this, 以启用链接
     */
    public PrivacyReplacer setVariablePrefix(final String prefix) {
        if (null == prefix) {
            throw new IllegalArgumentException("Variable prefix must not be null!");
        }
        return setVariablePrefixMatcher(Matchers.stringMatcher(prefix));
    }

    /**
     * 获取当前使用的变量后缀匹配器
     *
     * @return 正在使用的后缀匹配器
     */
    public Matchers getVariableSuffixMatcher() {
        return suffixMatcher;
    }


    /**
     * 设置当前使用的变量后缀匹配器
     *
     * @param suffixMatcher 后缀匹配器,null被忽略
     * @return this, 以启用链接
     */
    public PrivacyReplacer setVariableSuffixMatcher(final Matchers suffixMatcher) {
        if (null == suffixMatcher) {
            throw new IllegalArgumentException("Variable suffix matcher must not be null!");
        }
        this.suffixMatcher = suffixMatcher;
        return this;
    }

    /**
     * 设置要使用的变量后缀
     *
     * @param suffix 要使用的后缀字符
     * @return this, 以启用链接
     */
    public PrivacyReplacer setVariableSuffix(final char suffix) {
        return setVariableSuffixMatcher(Matchers.charMatcher(suffix));
    }

    /**
     * 设置要使用的变量后缀
     *
     * @param suffix 变量的后缀,而不是null
     * @return this, 以启用链接
     */
    public PrivacyReplacer setVariableSuffix(final String suffix) {
        if (null == suffix) {
            throw new IllegalArgumentException("Variable suffix must not be null!");
        }
        return setVariableSuffixMatcher(Matchers.stringMatcher(suffix));
    }

    public Matchers getValueDelimiterMatcher() {
        return valueDelimiterMatcher;
    }

    public PrivacyReplacer setValueDelimiterMatcher(final Matchers valueDelimiterMatcher) {
        this.valueDelimiterMatcher = valueDelimiterMatcher;
        return this;
    }

    public PrivacyReplacer setValueDelimiter(final char valueDelimiter) {
        return setValueDelimiterMatcher(Matchers.charMatcher(valueDelimiter));
    }

    /**
     * 设置要使用的变量默认值分隔符
     *
     * @param valueDelimiter 要使用的变量默认值分隔符字符串可以为null或空
     * @return this, 以启用链接
     */
    public PrivacyReplacer setValueDelimiter(final String valueDelimiter) {
        if (StringKit.isEmpty(valueDelimiter)) {
            setValueDelimiterMatcher(null);
            return this;
        }
        return setValueDelimiterMatcher(Matchers.stringMatcher(valueDelimiter));
    }

    public Lookups<?> getVariableResolver() {
        return this.variableResolver;
    }

    public void setVariableResolver(final Lookups<?> variableResolver) {
        this.variableResolver = variableResolver;
    }

    public boolean isEnableSubstitutionInVariables() {
        return enableSubstitutionInVariables;
    }

    public void setEnableSubstitutionInVariables(
            final boolean enableSubstitutionInVariables) {
        this.enableSubstitutionInVariables = enableSubstitutionInVariables;
    }

    public boolean isPreserveEscapes() {
        return preserveEscapes;
    }

    public void setPreserveEscapes(final boolean preserveEscapes) {
        this.preserveEscapes = preserveEscapes;
    }

}
