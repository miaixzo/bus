/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
