/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.starter.wrapper;

import lombok.Data;
import org.aoju.bus.starter.BusXExtend;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 配置信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@ConfigurationProperties(prefix = BusXExtend.WRAPPER)
public class WrapperProperties {

    private int order;
    private String name = "extend-wrapper";
    /**
     * 指示已启用注册
     */
    private Boolean enabled = true;

    /**
     * API自动补全路由前缀
     */
    private boolean autoPrefix = true;
    /**
     * 扫描controller接口的基本包
     * Controller 所在包的 Ant 路径规则
     * 主要目的是，给该 Controller 设置指定的前缀
     */
    private String[] basePackages;
    /**
     * 为此注册设置初始化参数。调用此方法将替换任何现有的初始化参数
     */
    private Map<String, String> initParameters = new LinkedHashMap<>();
    /**
     * 筛选器要注册的servlet名称,这将替换以前指定的任何servlet名称
     */
    private Set<String> servletNames = new LinkedHashSet<>();
    /**
     * 过滤器将注册到的ServletRegistrationBeans
     */
    private Set<ServletRegistrationBean<?>> servletRegistrationBeans = new LinkedHashSet<>();

}
