package org.aoju.bus.spring.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties(prefix = "server.storage")
public class StorageProperties {

    String endpoint;
    String provider;
    String groupName;
    String accessKey;
    String secretKey;
    String urlprefix;
    String servers;
    long connectTimeout = 3000;
    int maxThreads = 50;
    boolean privated;


}
