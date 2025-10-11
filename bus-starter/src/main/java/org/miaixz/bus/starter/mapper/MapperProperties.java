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

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Configuration properties for MyBatis Mapper.
 * <p>
 * This class provides a way to configure MyBatis and the Mapper framework through Spring Boot's property mechanism
 * (e.g., {@code application.yml}).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.MAPPER)
public class MapperProperties {

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /**
     * Base packages to scan for MyBatis mapper interfaces.
     * <p>
     * You can specify more than one package by using a comma or semicolon as a separator.
     * </p>
     */
    private String[] basePackage;

    /**
     * Location of the MyBatis XML configuration file.
     */
    private String configLocation;

    /**
     * Locations of MyBatis mapper XML files.
     */
    private String[] mapperLocations;

    /**
     * Packages to search for type aliases. (Package separators are ",; \t\n")
     */
    private String typeAliasesPackage;

    /**
     * The superclass for filtering type aliases. If this is not specified, all classes found in
     * {@code typeAliasesPackage} will be treated as type aliases.
     */
    private Class<?> typeAliasesSuperType;

    /**
     * Packages to search for type handlers. (Package separators are ",; \t\n")
     */
    private String typeHandlersPackage;

    /**
     * Indicates whether to check for the presence of the MyBatis XML configuration file.
     */
    private boolean checkConfigLocation = false;

    /**
     * The execution mode for the {@link org.mybatis.spring.SqlSessionTemplate}.
     */
    private ExecutorType executorType;

    /**
     * Externalized properties for MyBatis configuration.
     */
    private Properties configurationProperties;

    /**
     * A {@link Configuration} object for customizing default settings. This property is not used if
     * {@link #configLocation} is specified.
     */
    @NestedConfigurationProperty
    private Configuration configuration;

    /**
     * General parameter settings, often used for plugins. For example, for PageHelper:
     * {@code helperDialect=mysql,reasonable=true,supportMethodsArguments=true,params=count=countSql}
     */
    private String params;

    /**
     * Automatically delimits SQL keywords in column names.
     */
    private String autoDelimitKeywords;

    /**
     * Enables pagination parameter rationalization. When enabled, if pageNum < 1, it will query page 1. If pageNum >
     * total pages, it will query the last page.
     */
    private String reasonable;

    /**
     * Whether to support passing pagination parameters through Mapper interface method arguments.
     */
    private String supportMethodsArguments;

    /**
     * Resolves the mapper locations specified in the properties into an array of {@link Resource} objects.
     *
     * @return An array of resolved {@link Resource} objects.
     */
    public Resource[] resolveMapperLocations() {
        List<Resource> resources = new ArrayList<>();
        if (this.mapperLocations != null) {
            for (String mapperLocation : this.mapperLocations) {
                resources.addAll(Arrays.asList(getResources(mapperLocation)));
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * Retrieves resources from a given location pattern.
     *
     * @param location The location pattern (e.g., "classpath*:com/my/mapper/*.xml").
     * @return An array of {@link Resource} objects.
     */
    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

}
