/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.text;


import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.utils.StringUtils;

import java.util.*;

/**
 * 按值替换字符串中的变量.
 *
 * @author Kimi Liu
 * @version 3.6.3
 * @since JDK 1.8
 */
public class Substitute {

    /**
     * 默认变量前缀.
     */
    public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher(Symbol.DOLLAR + Symbol.DELIM_LEFT);
    /**
     * 默认变量后缀.
     */
    public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher(Symbol.DELIM_RIGHT);
    /**
     * 默认值分隔符.
     */
    public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(Symbol.COLON + Symbol.DASHED);

    /**
     * 存储转义字符.
     */
    private char escapeChar;
    /**
     * 存储变量前缀.
     */
    private StrMatcher prefixMatcher;
    /**
     * 存储变量后缀.
     */
    private StrMatcher suffixMatcher;
    /**
     * 存储默认变量值分隔符
     */
    private StrMatcher valueDelimiterMatcher;
    /**
     * 变量解析被委托给VariableResolver的实现程序.
     */
    private StrLookup<?> variableResolver;
    /**
     * 标记是否启用变量名中的替换.
     */
    private boolean enableSubstitutionInVariables;
    /**
     * 是否应该保留转义。默认false;
     */
    private boolean preserveEscapes = false;

    /**
     * 默认值为变量前缀和后缀以及转义字符.
     */
    public Substitute() {
        this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化它。对变量使用默认值
     * 前缀和后缀以及转义字符。
     *
     * @param <V>      映射中值的类型
     * @param valueMap 带有变量值的映射可能为null
     */
    public <V> Substitute(final Map<String, V> valueMap) {
        this(StrLookup.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化它。使用默认转义字符。
     *
     * @param <V>      映射中值的类型
     * @param valueMap 带有变量值的映射可能为null
     * @param prefix   变量的前缀，而不是null
     * @param suffix   变量的后缀，而不是null
     */
    public <V> Substitute(final Map<String, V> valueMap, final String prefix, final String suffix) {
        this(StrLookup.mapLookup(valueMap), prefix, suffix, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param <V>      映射中值的类型
     * @param valueMap 带有变量值的映射可能为null
     * @param prefix   变量的前缀，而不是null
     * @param suffix   变量的后缀，而不是null
     * @param escape   转义字符
     */
    public <V> Substitute(final Map<String, V> valueMap, final String prefix, final String suffix,
                          final char escape) {
        this(StrLookup.mapLookup(valueMap), prefix, suffix, escape);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param <V>       映射中值的类型
     * @param valueMap  带有变量值的映射可能为null
     * @param prefix    变量的前缀，而不是null
     * @param suffix    变量的后缀，而不是null
     * @param escape    转义字符
     * @param delimiter 变量默认值分隔符可以为空
     */
    public <V> Substitute(final Map<String, V> valueMap, final String prefix, final String suffix,
                          final char escape, final String delimiter) {
        this(StrLookup.mapLookup(valueMap), prefix, suffix, escape, delimiter);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver 变量解析器可以为空
     */
    public Substitute(final StrLookup<?> resolver) {
        this(resolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, Symbol.C_DOLLAR);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver 变量解析器可以为空
     * @param prefix   变量的前缀，而不是null
     * @param suffix   变量的后缀，而不是null
     * @param escape   转义字符
     */
    public Substitute(final StrLookup<?> resolver, final String prefix, final String suffix,
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
     * @param prefix    变量的前缀，而不是null
     * @param suffix    变量的后缀，而不是null
     * @param escape    转义字符
     * @param delimiter 变量默认值分隔符可以为空
     */
    public Substitute(final StrLookup<?> resolver, final String prefix, final String suffix,
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
     * @param resolver      变量解析器，可以为空
     * @param prefixMatcher 前缀解析器，而不是null
     * @param suffixMatcher 后缀解析器，而不是null
     * @param escape        转义转义字符
     */
    public Substitute(
            final StrLookup<?> resolver, final StrMatcher prefixMatcher, final StrMatcher suffixMatcher,
            final char escape) {
        this(resolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER);
    }

    /**
     * 创建一个新实例并初始化.
     *
     * @param resolver      变量解析器，可以为空
     * @param prefixMatcher 前缀解析器，而不是null
     * @param suffixMatcher 后缀解析器，而不是null
     * @param escape        转义转义字符
     * @param delimiter     变量默认值分隔符可以为空
     */
    public Substitute(
            final StrLookup<?> resolver, final StrMatcher prefixMatcher, final StrMatcher suffixMatcher,
            final char escape, final StrMatcher delimiter) {
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
        return new Substitute(valueMap).replace(source);
    }

    /**
     * 将给定源对象中出现的所有变量替换
     * 它们在映射中匹配的值。这种方法允许指定自定义变量前缀和后缀
     *
     * @param <V>      映射中值的类型
     * @param source   包含要替换的变量的源文本
     * @param valueMap 映射的值可能为空
     * @param prefix   变量的前缀，而不是null
     * @param suffix   变量的后缀，而不是null
     * @return 替换操作的结果
     */
    public static <V> String replace(final Object source, final Map<String, V> valueMap, final String prefix, final String suffix) {
        return new Substitute(valueMap, prefix, suffix).replace(source);
    }

    /**
     * 用匹配的变量替换给定源对象中出现的所有变量
     *
     * @param source 包含要替换的变量的源文本
     * @param value  带值的属性可以为空
     * @return 替换操作的结果
     */
    public static String replace(final Object source, final Properties value) {
        if (value == null) {
            return source.toString();
        }
        final Map<String, String> valueMap = new HashMap<String, String>();
        final Enumeration<?> propNames = value.propertyNames();
        while (propNames.hasMoreElements()) {
            final String propName = (String) propNames.nextElement();
            final String propValue = value.getProperty(propName);
            valueMap.put(propName, propValue);
        }
        return Substitute.replace(source, valueMap);
    }

    /**
     * 替换给定源对象中出现的所有变量
     * 它们在系统属性中匹配的值。
     *
     * @param source 包含要替换的变量的源文本
     * @return 返回替换操作的结果
     */
    public static String replaceSystemProperties(final Object source) {
        return new Substitute(StrLookup.systemPropertiesLookup()).replace(source);
    }

    /**
     * 用匹配的值替换所有出现的变量
     * 从使用给定源字符串作为模板的解析器。
     *
     * @param source 获取要替换的字符串
     * @return 替换操作的结果
     */
    public String replace(final String source) {
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(source);
        if (substitute(buf, 0, source.length()) == false) {
            return source;
        }
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
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
        if (substitute(buf, 0, length) == false) {
            return source.substring(offset, offset + length);
        }
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     * 从使用给定源数组作为模板的解析器。
     * 该方法不改变数组。
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 返回替换操作的结果
     */
    public String replace(final char[] source) {
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(source.length).append(source);
        substitute(buf, 0, source.length);
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
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
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
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(source.length()).append(source);
        substitute(buf, 0, buf.length());
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 源缓冲区用作模板，没有更改
     * @param offset 偏移数组中的起始偏移必须有效
     * @param length 要处理的数组中的长度必须是有效的
     * @return 返回替换操作的结果
     */
    public String replace(final StringBuffer source, final int offset, final int length) {
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 源缓冲区用作模板，没有更改
     * @return 返回替换操作的结果
     */
    public String replace(final CharSequence source) {
        if (source == null) {
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
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }


    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public String replace(final StrBuilder source) {
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(source.length()).append(source);
        substitute(buf, 0, buf.length());
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
    public String replace(final StrBuilder source, final int offset, final int length) {
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
        substitute(buf, 0, length);
        return buf.toString();
    }

    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public String replace(final Object source) {
        if (source == null) {
            return null;
        }
        final StrBuilder buf = new StrBuilder().append(source);
        substitute(buf, 0, buf.length());
        return buf.toString();
    }


    /**
     * 用匹配的值替换所有出现的变量
     *
     * @param source 获取要替换的字符数组的源代码
     * @return 替换操作的结果
     */
    public boolean replaceIn(final StringBuffer source) {
        if (source == null) {
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
        if (source == null) {
            return false;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
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
        if (source == null) {
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
        if (source == null) {
            return false;
        }
        final StrBuilder buf = new StrBuilder(length).append(source, offset, length);
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
    public boolean replaceIn(final StrBuilder source) {
        if (source == null) {
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
    public boolean replaceIn(final StrBuilder source, final int offset, final int length) {
        if (source == null) {
            return false;
        }
        return substitute(source, offset, length);
    }

    /**
     * 替换变量的内部方法
     *
     * @param buffer 要替换为的字符串生成器，而不是null
     * @param offset 构造器中的起始偏移量必须有效
     * @param length 要处理的构建器中的长度必须是有效的
     * @return true/false
     */
    protected boolean substitute(final StrBuilder buffer, final int offset, final int length) {
        return substitute(buffer, offset, length, null) > 0;
    }

    /**
     * 多层插值的递归处理程序
     *
     * @param buffer         要替换为的字符串生成器，而不是null
     * @param offset         构造器中的起始偏移量必须有效
     * @param length         要处理的构建器中的长度必须是有效的
     * @param priorVariables 保存被替换变量的堆栈可以为空
     * @return 发生的长度更改，除非priorVariables在int时为null 表示布尔标志，表示是否发生了更改
     */
    private int substitute(final StrBuilder buffer, final int offset, final int length, List<String> priorVariables) {
        final StrMatcher pfxMatcher = getVariablePrefixMatcher();
        final StrMatcher suffMatcher = getVariableSuffixMatcher();
        final char escape = getEscapeChar();
        final StrMatcher valueDelimMatcher = getValueDelimiterMatcher();
        final boolean substitutionInVariablesEnabled = isEnableSubstitutionInVariables();

        final boolean top = priorVariables == null;
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
                                    final StrBuilder bufName = new StrBuilder(varNameExpr);
                                    substitute(bufName, 0, bufName.length());
                                    varNameExpr = bufName.toString();
                                }
                                pos += endMatchLen;
                                final int endPos = pos;

                                String varName = varNameExpr;
                                String varDefaultValue = null;

                                if (valueDelimMatcher != null) {
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

                                if (priorVariables == null) {
                                    priorVariables = new ArrayList<>();
                                    priorVariables.add(new String(chars,
                                            offset, length));
                                }

                                checkCyclicSubstitution(varName, priorVariables);
                                priorVariables.add(varName);

                                String varValue = resolveVariable(varName, buffer,
                                        startPos, endPos);
                                if (varValue == null) {
                                    varValue = varDefaultValue;
                                }
                                if (varValue != null) {
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
        final StrBuilder buf = new StrBuilder(256);
        buf.append("Infinite loop in property interpolation of ");
        buf.append(priorVariables.remove(0));
        buf.append(": ");
        buf.appendWithSeparators(priorVariables, "->");
        throw new IllegalStateException(buf.toString());
    }

    /**
     * 解析变量值的内部方法
     * {@link #getVariableResolver()}，其中变量名作为键。
     *
     * @param variableName 变量名，而不是null
     * @param buf          是发生替换的缓冲区，而不是null
     * @param startPos     变量的起始位置，包括前缀，有效
     * @param endPos       变量的结束位置，包括后缀，有效
     * @return 返回变量的值，如果变量未知，则null
     */
    protected String resolveVariable(final String variableName, final StrBuilder buf, final int startPos, final int endPos) {
        final StrLookup<?> resolver = getVariableResolver();
        if (resolver == null) {
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
     * 如果该字符放在源中的变量引用之前，这个变量将被忽略。
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
    public StrMatcher getVariablePrefixMatcher() {
        return prefixMatcher;
    }

    /**
     * 设置当前使用的变量前缀匹配器。
     *
     * @param prefixMatcher 前缀匹配器，null被忽略
     * @return this，以启用链接
     */
    public Substitute setVariablePrefixMatcher(final StrMatcher prefixMatcher) {
        if (prefixMatcher == null) {
            throw new IllegalArgumentException("Variable prefix matcher must not be null!");
        }
        this.prefixMatcher = prefixMatcher;
        return this;
    }

    /**
     * 设置要使用的变量前缀
     *
     * @param prefix 要使用的前缀字符
     * @return this，以启用链接
     */
    public Substitute setVariablePrefix(final char prefix) {
        return setVariablePrefixMatcher(StrMatcher.charMatcher(prefix));
    }

    /**
     * 设置要使用的变量前缀
     *
     * @param prefix 变量的前缀，而不是null
     * @return this，以启用链接
     */
    public Substitute setVariablePrefix(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Variable prefix must not be null!");
        }
        return setVariablePrefixMatcher(StrMatcher.stringMatcher(prefix));
    }

    /**
     * 获取当前使用的变量后缀匹配器
     *
     * @return 正在使用的后缀匹配器
     */
    public StrMatcher getVariableSuffixMatcher() {
        return suffixMatcher;
    }


    /**
     * 设置当前使用的变量后缀匹配器
     *
     * @param suffixMatcher 后缀匹配器，null被忽略
     * @return this，以启用链接
     */
    public Substitute setVariableSuffixMatcher(final StrMatcher suffixMatcher) {
        if (suffixMatcher == null) {
            throw new IllegalArgumentException("Variable suffix matcher must not be null!");
        }
        this.suffixMatcher = suffixMatcher;
        return this;
    }

    /**
     * 设置要使用的变量后缀
     *
     * @param suffix 要使用的后缀字符
     * @return this，以启用链接
     */
    public Substitute setVariableSuffix(final char suffix) {
        return setVariableSuffixMatcher(StrMatcher.charMatcher(suffix));
    }

    /**
     * 设置要使用的变量后缀
     *
     * @param suffix 变量的后缀，而不是null
     * @return this，以启用链接
     */
    public Substitute setVariableSuffix(final String suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("Variable suffix must not be null!");
        }
        return setVariableSuffixMatcher(StrMatcher.stringMatcher(suffix));
    }

    public StrMatcher getValueDelimiterMatcher() {
        return valueDelimiterMatcher;
    }

    public Substitute setValueDelimiterMatcher(final StrMatcher valueDelimiterMatcher) {
        this.valueDelimiterMatcher = valueDelimiterMatcher;
        return this;
    }

    public Substitute setValueDelimiter(final char valueDelimiter) {
        return setValueDelimiterMatcher(StrMatcher.charMatcher(valueDelimiter));
    }

    /**
     * 设置要使用的变量默认值分隔符
     *
     * @param valueDelimiter 要使用的变量默认值分隔符字符串可以为null或空
     * @return this，以启用链接
     */
    public Substitute setValueDelimiter(final String valueDelimiter) {
        if (StringUtils.isEmpty(valueDelimiter)) {
            setValueDelimiterMatcher(null);
            return this;
        }
        return setValueDelimiterMatcher(StrMatcher.stringMatcher(valueDelimiter));
    }

    public StrLookup<?> getVariableResolver() {
        return this.variableResolver;
    }

    public void setVariableResolver(final StrLookup<?> variableResolver) {
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
