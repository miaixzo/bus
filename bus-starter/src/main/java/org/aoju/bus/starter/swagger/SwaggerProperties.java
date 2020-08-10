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
 ********************************************************************************/
package org.aoju.bus.starter.swagger;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.starter.BusXExtend;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * swagger配置项
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Data
@ConfigurationProperties(prefix = BusXExtend.SWAGGER)
public class SwaggerProperties {

    /**
     * swagger会解析的包路径
     **/
    private String basePackage = Normal.EMPTY;

    /**
     * 标题
     **/
    private String title = Normal.EMPTY;

    /**
     * 描述
     **/
    private String description = Normal.EMPTY;

    /**
     * 版本
     **/
    private String version = Normal.EMPTY;

    /**
     * 许可证
     **/
    private String license = Normal.EMPTY;

    /**
     * 许可证URL
     **/
    private String licenseUrl = Normal.EMPTY;

    /**
     * 服务条款URL
     **/
    private String termsOfServiceUrl = Normal.EMPTY;

    /**
     * host信息
     **/
    private String host = Normal.EMPTY;

    /**
     * 联系人信息
     */
    private Contact contact = new Contact();

    @Data
    @NoArgsConstructor
    public static class Contact {

        /**
         * 联系人
         **/
        private String name = Normal.EMPTY;

        /**
         * 联系人url
         **/
        private String url = Normal.EMPTY;

        /**
         * 联系人email
         **/
        private String email = Normal.EMPTY;

    }

}
