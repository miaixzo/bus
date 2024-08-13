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
package org.miaixz.bus.shade.screw;

import java.io.Serializable;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.shade.screw.engine.EngineConfig;
import org.miaixz.bus.shade.screw.process.ProcessConfig;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 配置入口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Config implements Serializable {

    /**
     * 组织
     */
    private String organization;
    /**
     * url
     */
    private String organizationUrl;
    /**
     * 标题
     */
    private String title;
    /**
     * 版本号
     */
    private String version;
    /**
     * 描述
     */
    private String description;
    /**
     * 数据源，这里直接使用@see{@link DataSource}接口，好处就，可以使用任何数据源
     */
    private DataSource dataSource;
    /**
     * 生成配置
     */
    private ProcessConfig produceConfig;
    /**
     * 引擎配置，关于数据库文档生成相关配置
     */
    private EngineConfig engineConfig;

    /**
     * 构造函数
     *
     * @param title         {@link String} 标题
     * @param organization  {@link String} 机构
     * @param version       {@link String} 版本
     * @param description   {@link String} 描述
     * @param dataSource    {@link DataSource} 数据源
     * @param produceConfig {@link ProcessConfig} 生成配置
     * @param engineConfig  {@link EngineConfig} 生成配置
     */
    private Config(String organization, String organizationUrl, String title, String version, String description,
            DataSource dataSource, ProcessConfig produceConfig, EngineConfig engineConfig) {
        Assert.notNull(dataSource, "DataSource can not be empty!");
        Assert.notNull(engineConfig, "EngineConfig can not be empty!");
        this.title = title;
        this.organizationUrl = organizationUrl;
        this.organization = organization;
        this.version = version;
        this.description = description;
        this.dataSource = dataSource;
        this.engineConfig = engineConfig;
        this.produceConfig = produceConfig;
    }

}
