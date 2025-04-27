/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper;

import java.util.Properties;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.entity.Property;

/**
 * 使用提供的 Property 可以在纯 Java 或者 Spring(mybatis-spring-1.3.0+) 模式中使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Config extends Configuration {

    private MapperBuilder mapperBuilder;

    /**
     * 直接注入 MapperBuilder
     *
     * @param mapperBuilder 逻辑处理类
     */
    public void setMapperBuilder(MapperBuilder mapperBuilder) {
        this.mapperBuilder = mapperBuilder;
    }

    /**
     * 使用属性方式配置
     *
     * @param properties 属性
     */
    public void setMapperProperties(Properties properties) {
        if (this.mapperBuilder == null) {
            this.mapperBuilder = new MapperBuilder();
        }
        this.mapperBuilder.setProperties(properties);
    }

    /**
     * 使用 {@link Property} 配置
     *
     * @param property 配置
     */
    public void setConfig(Property property) {
        if (mapperBuilder == null) {
            mapperBuilder = new MapperBuilder();
        }
        mapperBuilder.setConfig(property);
    }

    @Override
    public void addMappedStatement(MappedStatement ms) {
        try {
            super.addMappedStatement(ms);
            // 没有任何配置时，使用默认配置
            if (this.mapperBuilder == null) {
                this.mapperBuilder = new MapperBuilder();
            }
            this.mapperBuilder.processMappedStatement(ms);
        } catch (IllegalArgumentException e) {
            // 这里的异常是导致 Spring 启动死循环的关键位置，为了避免后续会吞异常，这里直接输出
            Logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
