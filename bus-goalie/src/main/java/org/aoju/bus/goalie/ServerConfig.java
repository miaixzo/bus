package org.aoju.bus.goalie;

import lombok.Data;

/**
 * 服务端配置
 *
 * @author Justubborn
 * @version 6.2.2
 * @since JDK 1.8+
 */
@Data
public class ServerConfig {

    private final Encrypt encrypt = new Encrypt();
    private final Decrypt decrypt = new Decrypt();
    private final Limit limit = new Limit();
    private String path;
    private int port;

    @Data
    public static class Encrypt {
        private boolean enabled;
        private String key;
        private String type;
        private String offset;
    }

    @Data
    public static class Decrypt {
        private boolean enabled;
        private String key;
        private String type;
        private String offset;
    }

    @Data
    public static class Limit {
        private boolean enabled;
    }

}
