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
package org.miaixz.bus.pay;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Context configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Context implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852292391971L;

    /**
     * Application ID.
     */
    private String appId;
    /**
     * Corresponds to the appKey/apiKey of each platform.
     */
    private String appKey;
    /**
     * Corresponds to the appSecret of each platform.
     */
    private String appSecret;
    /**
     * Application domain name, which will be used in callbacks.
     */
    private String domain;
    /**
     * Whether it is in certificate mode.
     */
    private boolean certMode;
    /**
     * Custom scope content for the authorization platform.
     */
    private List<String> scopes;

    /**
     * p12 in the API certificate.
     */
    private String p12;
    /**
     * key.pem in the API certificate.
     */
    private String privateKey;
    /**
     * cert.pem in the API certificate.
     */
    private String publicKey;

    /**
     * Extra parameters.
     */
    private Object exParams;

    /**
     * DES key.
     */
    private String desKey;

    /**
     * Merchant ID.
     */
    private String mchId;
    /**
     * Service provider application ID.
     */
    private String slAppId;
    /**
     * Service provider merchant ID.
     */
    private String slMchId;
    /**
     * Service provider merchant key.
     */
    private String partnerKey;
    /**
     * Chain merchant ID.
     */
    private String groupMchId;
    /**
     * Authorized transaction institution code.
     */
    private String agentMchId;
    /**
     * WeChat platform certificate path.
     */
    private String certPath;

}
