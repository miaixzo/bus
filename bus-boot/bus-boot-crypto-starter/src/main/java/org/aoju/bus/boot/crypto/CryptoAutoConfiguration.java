package org.aoju.bus.boot.crypto;


import org.aoju.bus.spring.crypto.CryptoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Crypto 自动配置
 */
@Configuration
@Import(value = {CryptoConfiguration.class})
public class CryptoAutoConfiguration {

}
