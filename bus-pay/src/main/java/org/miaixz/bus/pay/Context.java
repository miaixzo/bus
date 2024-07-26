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
package org.miaixz.bus.pay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 上下文配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Context implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 应用编号
     */
    private String appId;
    /**
     * 对应各平台的appKey/apiKey
     */
    private String appKey;
    /**
     * 对应各平台的appSecret
     */
    private String appSecret;
    /**
     * 应用域名，回调中会使用此参数
     */
    private String domain;
    /**
     * 是否为证书模式
     */
    private boolean certMode;
    /**
     * 自定义授权平台的 scope 内容
     */
    private List<String> scopes;

    /**
     * API 证书中的 p12
     */
    private String p12;
    /**
     * API 证书中的 key.pem
     */
    private String privateKey;
    /**
     * API 证书中的 cert.pem
     */
    private String publicKey;

    private Object exParams;

    private String desKey;

    /**
     * 商户号
     */
    private String mchId;
    /**
     * 服务商应用编号
     */
    private String slAppId;
    /**
     * 服务商商户号
     */
    private String slMchId;
    /**
     * 服务商商户密钥
     */
    private String partnerKey;
    /**
     * 连锁商户号
     */
    private String groupMchId;
    /**
     * 授权交易机构代码
     */
    private String agentMchId;
    /**
     * 微信平台证书
     */
    private String certPath;

}
