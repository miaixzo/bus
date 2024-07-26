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
package org.miaixz.bus.starter.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Druid 配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.DATASOURCE)
public class JdbcProperties {

    /**
     * 数据源名称
     */
    private String name;
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
     * 配置监控统计拦截的
     */
    private String filters;
    /**
     * 连接池中的最小空闲连接数
     */
    private String minIdle;
    /**
     * 最小空闲时间，默认30分钟
     */
    private String minEvictableIdleTimeMillis;
    /**
     * 最大空闲时间，默认7小时
     */
    private String maxEvictableIdleTimeMillis;
    /**
     * 测试连接是否可用的SQL语句
     */
    private String validationQuery;
    /**
     * 是否缓存preparedStatement
     */
    private boolean poolPreparedStatements;
    /**
     * 要启用PSCache
     */
    private String maxOpenPreparedStatements;
    /**
     * 检测需要关闭的空闲连接周期
     */
    private String timeBetweenEvictionRunsMillis;
    /**
     * 最大连接池数量
     */
    private int maxActive;
    /**
     * 初始化时建立连接的个数
     */
    private int initialSize;
    /**
     * 获取连接等待超时的时间
     */
    private int maxWait;
    /**
     * 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测 如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效
     */
    private boolean testWhileIdle;
    /**
     * 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
     */
    private boolean testOnBorrow;
    /**
     * 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
     */
    private boolean testOnReturn;
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
