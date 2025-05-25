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
package org.miaixz.bus.goalie.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 版本映射注解，整合 Spring 的 RequestMapping、ApiVersion 和 ClientVersion 功能， 用于定义 API 的请求路径、版本和客户端终端版本匹配规则。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping
@ApiVersion
@ClientVersion
public @interface VersionMapping {

    /**
     * RequestMapping 的名称别名
     *
     * @return 映射名称，默认为空字符串
     */
    @AliasFor(annotation = RequestMapping.class)
    String name() default Normal.EMPTY;

    /**
     * RequestMapping 的路径别名，与 path 属性等效
     *
     * @return 路径数组，默认为空数组
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    /**
     * RequestMapping 的路径别名，与 value 属性等效
     *
     * @return 路径数组，默认为空数组
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    /**
     * RequestMapping 的参数条件
     *
     * @return 请求参数条件数组，默认为空数组
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    /**
     * RequestMapping 的请求头条件
     *
     * @return 请求头条件数组，默认为空数组
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    /**
     * RequestMapping 的请求内容类型条件
     *
     * @return 消费的内容类型数组，默认为空数组
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    /**
     * RequestMapping 的响应内容类型条件
     *
     * @return 生产的内容类型数组，默认为空数组
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};

    /**
     * ApiVersion 的版本路径别名
     *
     * @return 版本路径，默认为空字符串
     */
    @AliasFor(annotation = ApiVersion.class, attribute = "value")
    String apiVersion() default Normal.EMPTY;

    /**
     * ClientVersion 的终端版本条件别名
     *
     * @return TerminalVersion 数组，默认为空数组
     */
    @AliasFor(annotation = ClientVersion.class, attribute = "value")
    TerminalVersion[] terminalVersion() default {};

    /**
     * ClientVersion 的字符串表达式别名，用于解析终端版本条件
     *
     * @return 字符串表达式数组，默认为空数组
     */
    @AliasFor(annotation = ClientVersion.class, attribute = "expression")
    String[] terminalExpression() default {};

}