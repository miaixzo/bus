package org.aoju.bus.goalie;

import lombok.Data;

/**
 * 服务端配置
 *
 * @author Justubborn
 * @since 2020/12/17
 */
@Data
public class ServerConfig {
    private String path;
    private int port;
    private final Encrypt encrypt = new Encrypt();
    private final Decrypt decrypt = new Decrypt();
    private final Limit limit = new Limit();

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
