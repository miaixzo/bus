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
package org.miaixz.bus.goalie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 服务端配置类，用于存储和管理服务器相关的配置信息
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Config {

    /**
     * 请求方法参数名，用于标识请求的处理方法
     */
    public static final String METHOD = "method";

    /**
     * 版本信息参数名，用于指定接口版本
     */
    public static final String VERSION = "v";

    /**
     * 格式化数据参数名，用于指定响应数据格式
     */
    public static final String FORMAT = "format";

    /**
     * 签名信息参数名，用于验证请求签名
     */
    public static final String SIGN = "sign";

    /**
     * 授权信息头，存储访问令牌
     */
    public static final String X_ACCESS_TOKEN = "X-Access-Token";

    /**
     * 访问来源头，标识请求的渠道来源
     */
    public static final String X_REMOTE_CHANNEL = "x_remote_channel";

    /**
     * 默认最大内存数据大小，100MB，用于限制内存中处理的数据量
     */
    public static final Integer MAX_INMEMORY_SIZE = 100 * 1024 * 1024;

    /**
     * 加密配置，默认初始化
     */
    private final Encrypt encrypt = new Encrypt();

    /**
     * 解密配置，默认初始化
     */
    private final Decrypt decrypt = new Decrypt();

    /**
     * 限流配置，默认初始化
     */
    private final Limit limit = new Limit();

    /**
     * 安全配置，默认初始化
     */
    private final Security security = new Security();

    /**
     * 服务路径，指定服务器的访问路径
     */
    private String path;

    /**
     * 服务端口，指定服务器监听的端口号
     */
    private int port;

    /**
     * 加密配置类，定义加密相关参数
     */
    @Getter
    @Setter
    public static class Encrypt {
        /**
         * 是否启用加密
         */
        private boolean enabled;

        /**
         * 加密密钥
         */
        private String key;

        /**
         * 加密算法类型
         */
        private String type;

        /**
         * 加密偏移量（若适用）
         */
        private String offset;
    }

    /**
     * 解密配置类，定义解密相关参数
     */
    @Getter
    @Setter
    public static class Decrypt {
        /**
         * 是否启用解密
         */
        private boolean enabled;

        /**
         * 解密密钥
         */
        private String key;

        /**
         * 解密算法类型
         */
        private String type;

        /**
         * 解密偏移量（若适用）
         */
        private String offset;
    }

    /**
     * 限流配置类，定义流量限制相关参数
     */
    @Getter
    @Setter
    public static class Limit {
        /**
         * 是否启用限流
         */
        private boolean enabled;
    }

    /**
     * 安全配置类，定义安全相关参数
     */
    @Getter
    @Setter
    public static class Security {
        /**
         * 是否启用安全机制
         */
        private boolean enabled;

        /**
         * 是否启用模拟模式（用于测试或调试）
         */
        private boolean mock;
    }

}