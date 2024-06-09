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
package org.miaixz.bus.spring;

import org.miaixz.bus.core.lang.Symbol;

/**
 * 全局扩展配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BusXConfig {

    /**
     * Spring配置
     */
    public static final String SPRING = "spring";
    /**
     * 扩展配置
     */
    public static final String EXTEND = "extend";
    /**
     * 数据源配置
     */
    public static final String DATASOURCE = SPRING + Symbol.DOT + "datasource";
    /**
     * 缓存配置
     */
    public static final String CACHE = EXTEND + Symbol.DOT + "cache";
    /**
     * 配置中心
     */
    public static final String BRIDGE = EXTEND + Symbol.DOT + "bridge";
    /**
     * 跨域支持
     */
    public static final String CORS = EXTEND + Symbol.DOT + "cors";
    /**
     * Druid监控
     */
    public static final String DRUID = EXTEND + Symbol.DOT + "druid";
    /**
     * Druid监控
     */
    public static final String DUBBO = EXTEND + Symbol.DOT + "dubbo";
    /**
     * 路由配置
     */
    public static final String GOALIE = EXTEND + Symbol.DOT + "goalie";
    /**
     * 国际化支持
     */
    public static final String I18N = EXTEND + Symbol.DOT + "i18n";
    /**
     * 图像解析
     */
    public static final String IMAGE = EXTEND + Symbol.DOT + "image";
    /**
     * 限流支持
     */
    public static final String LIMITER = EXTEND + Symbol.DOT + "limiter";
    /**
     * Mybatis/Mapper
     */
    public static final String MYBATIS = EXTEND + Symbol.DOT + "mybatis";
    /**
     * 消息通知
     */
    public static final String NOTIFY = EXTEND + Symbol.DOT + "notify";
    /**
     * 授权登陆
     */
    public static final String OAUTH = EXTEND + Symbol.DOT + "oauth";
    /**
     * 文件预览
     */
    public static final String OFFICE = EXTEND + Symbol.DOT + "office";
    /**
     * 数据脱敏
     */
    public static final String SENSITIVE = EXTEND + Symbol.DOT + "sensitive";
    /**
     * socket
     */
    public static final String SOCKET = EXTEND + Symbol.DOT + "socket";
    /**
     * 存储设置
     */
    public static final String STORAGE = EXTEND + Symbol.DOT + "storage";
    /**
     * XSS/重复读取失效
     */
    public static final String WRAPPER = EXTEND + Symbol.DOT + "wrapper";
    /**
     * 工作/临时目录等
     */
    public static final String WORK = EXTEND + Symbol.DOT + "work";
    /**
     * Elastic支持
     */
    public static final String ELASTIC = EXTEND + Symbol.DOT + "elastic";

}
