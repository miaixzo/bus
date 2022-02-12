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
package org.aoju.bus.starter.druid;

import lombok.Data;
import org.aoju.bus.starter.BusXExtend;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Druid 监控配置项
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
@Data
@ConfigurationProperties(prefix = BusXExtend.DRUID)
public class DruidMonitorProperties {

    /**
     * 监控信息显示页面
     */
    private String DruidStatView;
    /**
     * 监控拦截器
     */
    private String DruidWebStatFilter;
    /**
     * IP白名单
     */
    private String allow;

    /**
     * IP黑名单
     */
    private String deny;
    /**
     * 登录账号
     */
    private String loginUsername;
    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 是否能够重置数据
     */
    private String resetEnable;

    /**
     * 添加不需要忽略的格式信息
     */
    private String exclusions;

}
