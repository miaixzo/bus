package org.aoju.bus.spring.crypto;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.crypto.Mode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
        private Mode type;
    }

    @Data
    @ConfigurationProperties(prefix = "request.crypto.decrypt")
    public class Decrypt {

        private String key;
        private Mode type;
    }

}
