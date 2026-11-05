/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.starter.jdbc;

import lombok.Data;
import org.aoju.bus.spring.BusXConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * HikariCP 配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@ConfigurationProperties(prefix = BusXConfig.DATASOURCE)
public class JdbcProperties {

    /**
     * 数据源连接url
     */
    private String url;
    /**
     * 数据库账号
     */
    private String username;
    /**
     * 数据库密码
     */
    private String password;
    /**
     * 连接池类型
     */
    private String type;
    /**
     * 数据库连接池驱动
     */
    private String driverClassName;
    /**
     * 连接池名称
     */
    private String poolName;
    /**
     * 获取连接等待超时时间，单位毫秒，默认30000
     */
    private long connectionTimeout;
    /**
     * 连接有效性校验超时时间，单位毫秒，默认5000
     */
    private long validationTimeout;
    /**
     * 连接允许空闲的最长时间，单位毫秒，默认600000，0表示永不超时
     */
    private long idleTimeout;
    /**
     * 连接在池中的最大存活时间，单位毫秒，默认1800000，应小于数据库的连接超时
     */
    private long maxLifetime;
    /**
     * 保活时间，单位毫秒，用于空闲连接的ping探测，默认0表示禁用
     */
    private long keepaliveTime;
    /**
     * 连接泄漏检测阈值，单位毫秒，超过该时间未归还则记录警告，默认0表示禁用
     */
    private long leakDetectionThreshold;
    /**
     * 测试连接是否可用的SQL语句
     */
    private String connectionTestQuery;
    /**
     * 连接池中的最小空闲连接数
     */
    private int minimumIdle;
    /**
     * 最大连接池数量
     */
    private int maximumPoolSize;
    /**
     * 连接是否自动提交
     */
    private Boolean autoCommit;
    /**
     * 数据源加解密信息
     */
    private String privateKey;
    /**
     * 多数据源唯一标识
     */
    private String key;
    /**
     * 多数据源支持
     */
    private List<JdbcProperties> multi;

}
