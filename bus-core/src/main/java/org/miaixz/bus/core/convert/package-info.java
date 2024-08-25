/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
/**
 * 万能类型转换器以及各种类型转换的实现类，其中Convert为转换器入口，提供各种toXXX方法和convert方法 转换器是典型的策略模式应用，可自定义转换策略。提供了常用类型的转换策略，自定义转换接口包括：
 * <ul>
 * <li>{@link org.miaixz.bus.core.convert.Converter}，标准转换接口，通过类型匹配策略后调用使用。</li>
 * <li>{@link org.miaixz.bus.core.convert.MatcherConverter}，带有match方法的Converter，通过自身匹配判断调用转换。</li>
 * </ul>
 *
 * <p>
 * 公共的转换器封装有两种：
 * <ul>
 * <li>{@link org.miaixz.bus.core.convert.RegisterConverter}，提供预定义的转换规则，也可以注册自定义转换规则。</li>
 * <li>{@link org.miaixz.bus.core.convert.CompositeConverter}，复合转换器，封装基于注册的、特别转换（泛型转换）等规则，实现万能转换。</li>
 * </ul>
 *
 * <p>
 * 转换器是典型的策略模式应用，通过实现{@link org.miaixz.bus.core.convert.Converter} 接口， 自定义转换策略。提供了常用类型的转换策略。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.convert;
