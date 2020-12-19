/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.starter.mapper;

import lombok.Data;
import org.aoju.bus.starter.BusXExtend;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mybatis配置项
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
@Data
@ConfigurationProperties(prefix = BusXExtend.MYBATIS)
public class MybatisProperties {

    /**
     * Mapper包路径
     */
    private String basePackage;
    /**
     * XML路径
     */
    private String xmlLocation;
    /**
     * 映射类型别名
     */
    private String typeAliasesPackage;
    /**
     * 检查返回类型是否为Page
     */
    private String returnPage;
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
    /**
     * 记录时间,即created,modified
     */
    private boolean recordTime;

}
