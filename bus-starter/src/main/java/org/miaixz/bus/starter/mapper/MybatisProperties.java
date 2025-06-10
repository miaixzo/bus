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
package org.miaixz.bus.starter.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import lombok.Getter;
import lombok.Setter;

/**
 * mybatis配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.MYBATIS)
public class MybatisProperties {

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /**
     * 扫描MyBatis接口的基本包
     */
    private String[] basePackage;
    /**
     * MyBatis xml配置文件的位置
     */
    private String configLocation;
    /**
     * MyBatis映射器文件的位置
     */
    private String[] mapperLocations;
    /**
     * 用于搜索类型别名的包(包的分隔符为",; \t\n")
     */
    private String typeAliasesPackage;
    /**
     * 用于过滤类型别名的超类 如果没有指定，MyBatis将从typeAliasesPackage中搜索的所有类作为类型别名处理
     */
    private Class<?> typeAliasesSuperType;
    /**
     * 包来搜索类型处理程序。(包的分隔符为",; \t\n")
     */
    private String typeHandlersPackage;
    /**
     * 指示是否对MyBatis xml配置文件进行presence检查
     */
    private boolean checkConfigLocation = false;
    /**
     * {@link org.mybatis.spring.SqlSessionTemplate}的执行模式
     */
    private ExecutorType executorType;
    /**
     * MyBatis配置的外部化属性
     */
    private Properties configurationProperties;
    /**
     * 用于自定义默认设置的Configuration对象 如果指定了{@link #configLocation}，则不使用此属性
     */
    @NestedConfigurationProperty
    private Configuration configuration;
    /**
     * 参数信息
     */
    private String params;
    /**
     * 识别列名中的SQL关键字
     */
    private String autoDelimitKeywords;
    /**
     * 分页合理化参数
     */
    private String reasonable;
    /**
     * 支持通过 Mapper 接口参数来传递分页参数
     */
    private String supportMethodsArguments;

    public Resource[] resolveMapperLocations() {
        List<Resource> resources = new ArrayList<>();
        if (this.mapperLocations != null) {
            for (String mapperLocation : this.mapperLocations) {
                resources.addAll(Arrays.asList(getResources(mapperLocation)));
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

}
