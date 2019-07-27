package org.aoju.bus.spring.crypto;

import org.aoju.bus.spring.crypto.advice.RequestBodyAdvice;
import org.aoju.bus.spring.crypto.advice.ResponseBodyAdvice;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {CryptoProperties.class})
@Import({RequestBodyAdvice.class, ResponseBodyAdvice.class})
public class CryptoConfiguration {

}
