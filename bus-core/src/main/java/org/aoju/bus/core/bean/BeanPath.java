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
package org.aoju.bus.core.bean;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.utils.*;

import java.util.*;

/**
 * Bean路径表达式，用于获取多层嵌套Bean中的字段值或Bean对象
 * 根据给定的表达式，查找Bean中对应的属性值对象。 表达式分为两种：
 * <ol>
 * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
 * <li>[]表达式，可以获取集合等对象中对应index的值</li>
 * </ol>
 * <p>
 * 表达式：
 *
 * <pre>
 * persion
 * persion.name
 * persons[3]
 * person.friends[5].name
 * ['person']['friends'][5]['name']
 * </pre>
 *
 * @author Kimi Liu
 * @version 3.5.2
 * @since JDK 1.8
 */
public class BeanPath {

    /**
     * 表达式边界符号数组
     */
    private static final char[] expChars = {Symbol.C_DOT, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT};
    protected List<String> patternParts;
    private boolean isStartWith$ = false;

    /**
     * 构造
     *
     * @param expression 表达式
     */
    public BeanPath(String expression) {
        init(expression);
    }

    /**
     * 解析Bean路径表达式为Bean模式
     * Bean表达式，用于获取多层嵌套Bean中的字段值或Bean对象
     * 根据给定的表达式，查找Bean中对应的属性值对象。 表达式分为两种：
     * <ol>
     * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
     * <li>[]表达式，可以获取集合等对象中对应index的值</li>
     * </ol>
     * <p>
     * 表达式栗子：
     *
     * <pre>
     * persion
     * persion.name
     * persons[3]
     * person.friends[5].name
     * ['person']['friends'][5]['name']
     * </pre>
     *
     * @param expression 表达式
     * @return {@link BeanPath}
     */
    public static BeanPath create(String expression) {
        return new BeanPath(expression);
    }

    private static Object getFieldValue(Object bean, String expression) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }

        if (StringUtils.contains(expression, ':')) {
            // [start:end:step] 模式
            final List<String> parts = StringUtils.splitTrim(expression, ':');
            int start = Integer.parseInt(parts.get(0));
            int end = Integer.parseInt(parts.get(1));
            int step = 1;
            if (3 == parts.size()) {
                step = Integer.parseInt(parts.get(2));
            }
            if (bean instanceof Collection) {
                return CollUtils.sub((Collection<?>) bean, start, end, step);
            } else if (ArrayUtils.isArray(bean)) {
                return ArrayUtils.sub(bean, start, end, step);
            }
        } else if (StringUtils.contains(expression, ',')) {
            // [num0,num1,num2...]模式或者['key0','key1']模式
            final List<String> keys = StringUtils.splitTrim(expression, ',');
            if (bean instanceof Collection) {
                return CollUtils.getAny((Collection<?>) bean, Convert.convert(int[].class, keys));
            } else if (ArrayUtils.isArray(bean)) {
                return ArrayUtils.getAny(bean, Convert.convert(int[].class, keys));
            } else {
                final String[] unwrapedKeys = new String[keys.size()];
                for (int i = 0; i < unwrapedKeys.length; i++) {
                    unwrapedKeys[i] = StringUtils.unWrap(keys.get(i), '\'');
                }
                if (bean instanceof Map) {
                    // 只支持String为key的Map
                    MapUtils.getAny((Map<String, ?>) bean, unwrapedKeys);
                } else {
                    final Map<String, Object> map = BeanUtils.beanToMap(bean);
                    MapUtils.getAny(map, unwrapedKeys);
                }
            }
        } else {
            // 数字或普通字符串
            return BeanUtils.getFieldValue(bean, expression);
        }

        return null;
    }

    /**
     * 对于非表达式去除单引号
     *
     * @param expression 表达式
     * @return 表达式
     */
    private static String unWrapIfPossible(CharSequence expression) {
        if (StringUtils.containsAny(expression, " = ", " > ", " < ", " like ", ",")) {
            return expression.toString();
        }
        return StringUtils.unWrap(expression, '\'');
    }

    /**
     * 获取Bean中对应表达式的值
     *
     * @param bean Bean对象或Map或List等
     * @return 值，如果对应值不存在，则返回null
     */
    public Object get(Object bean) {
        return get(this.patternParts, bean, false);
    }

    /**
     * 设置表达式指定位置（或filed对应）的值
     * 若表达式指向一个List则设置其坐标对应位置的值，若指向Map则put对应key的值，Bean则设置字段的值
     * 注意：
     *
     * <pre>
     * 1. 如果为List，如果下标不大于List长度，则替换原有值，否则追加值
     * 2. 如果为数组，如果下标不大于数组长度，则替换原有值，否则追加值
     * </pre>
     *
     * @param bean  Bean、Map或List
     * @param value 值
     */
    public void set(Object bean, Object value) {
        set(bean, this.patternParts, value);
    }

    /**
     * 设置表达式指定位置（或filed对应）的值
     * 若表达式指向一个List则设置其坐标对应位置的值，若指向Map则put对应key的值，Bean则设置字段的值
     * 注意：
     *
     * <pre>
     * 1. 如果为List，如果下标不大于List长度，则替换原有值，否则追加值
     * 2. 如果为数组，如果下标不大于数组长度，则替换原有值，否则追加值
     * </pre>
     *
     * @param bean         Bean、Map或List
     * @param patternParts 表达式块列表
     * @param value        值
     */
    private void set(Object bean, List<String> patternParts, Object value) {
        Object subBean = get(patternParts, bean, true);
        if (null == subBean) {
            set(bean, patternParts.subList(0, patternParts.size() - 1), new HashMap<>());
            //set中有可能做过转换，因此此处重新获取bean
            subBean = get(patternParts, bean, true);
        }
        BeanUtils.setFieldValue(subBean, patternParts.get(patternParts.size() - 1), value);
    }

    /**
     * 获取Bean中对应表达式的值
     *
     * @param patternParts 表达式分段列表
     * @param bean         Bean对象或Map或List等
     * @param ignoreLast   是否忽略最后一个值，忽略最后一个值则用于set，否则用于read
     * @return 值，如果对应值不存在，则返回null
     */
    private Object get(List<String> patternParts, Object bean, boolean ignoreLast) {
        int length = patternParts.size();
        if (ignoreLast) {
            length--;
        }
        Object subBean = bean;
        boolean isFirst = true;
        String patternPart;
        for (int i = 0; i < length; i++) {
            patternPart = patternParts.get(i);
            subBean = getFieldValue(subBean, patternPart);
            if (null == subBean) {
                // 支持表达式的第一个对象为Bean本身（若用户定义表达式$开头，则不做此操作）
                if (isFirst && false == this.isStartWith$ && BeanUtils.isMatchName(bean, patternPart, true)) {
                    subBean = bean;
                    isFirst = false;
                } else {
                    return null;
                }
            }
        }
        return subBean;
    }

    /**
     * 初始化
     *
     * @param expression 表达式
     */
    private void init(String expression) {
        List<String> localPatternParts = new ArrayList<>();
        int length = expression.length();

        final TextUtils builder = new TextUtils();
        char c;
        boolean isNumStart = false;// 下标标识符开始
        for (int i = 0; i < length; i++) {
            c = expression.charAt(i);
            if (0 == i && '$' == c) {
                // 忽略开头的$符，表示当前对象
                isStartWith$ = true;
                continue;
            }

            if (ArrayUtils.contains(expChars, c)) {
                // 处理边界符号
                if (Symbol.C_BRACKET_RIGHT == c) {
                    // 中括号（数字下标）结束
                    if (false == isNumStart) {
                        throw new IllegalArgumentException(StringUtils.format("Bad expression '{}':{}, we find ']' but no '[' !", expression, i));
                    }
                    isNumStart = false;
                    // 中括号结束加入下标
                    if (builder.length() > 0) {
                        localPatternParts.add(unWrapIfPossible(builder));
                    }
                    builder.reset();
                } else {
                    if (isNumStart) {
                        // 非结束中括号情况下发现起始中括号报错（中括号未关闭）
                        throw new IllegalArgumentException(StringUtils.format("Bad expression '{}':{}, we find '[' but no ']' !", expression, i));
                    } else if (Symbol.C_BRACKET_LEFT == c) {
                        // 数字下标开始
                        isNumStart = true;
                    }
                    // 每一个边界符之前的表达式是一个完整的KEY，开始处理KEY
                    if (builder.length() > 0) {
                        localPatternParts.add(unWrapIfPossible(builder));
                    }
                    builder.reset();
                }
            } else {
                // 非边界符号，追加字符
                builder.append(c);
            }
        }

        // 末尾边界符检查
        if (isNumStart) {
            throw new IllegalArgumentException(StringUtils.format("Bad expression '{}':{}, we find '[' but no ']' !", expression, length - 1));
        } else {
            if (builder.length() > 0) {
                localPatternParts.add(unWrapIfPossible(builder));
            }
        }

        // 不可变List
        this.patternParts = Collections.unmodifiableList(localPatternParts);
    }

}
