package org.aoju.bus.boot.storage;


import org.aoju.bus.spring.storage.StorageConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Druid 自动配置
 */
@Configuration
@Import(value = {StorageConfiguration.class})
public class StorageAutoConfiguration {

}
