/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.crypto;

import lombok.Data;
import org.aoju.bus.core.consts.Charset;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
@Data
@EnableConfigurationProperties(value = {CryptoProperties.Encrypt.class, CryptoProperties.Decrypt.class})
@ConfigurationProperties(prefix = "request.crypto")
public class CryptoProperties {

    private String encoding = Charset.DEFAULT_UTF_8;

    private Encrypt encrypt;
    private Decrypt decrypt;

    // 调试模式
    private boolean debug = false;

    @Data
    @ConfigurationProperties(prefix = "request.crypto.encrypt")
    public class Encrypt {

        private String key;
        private String type;
    }

    @Data
    @ConfigurationProperties(prefix = "request.crypto.decrypt")
    public class Decrypt {

        private String key;
        private String type;
    }

}
