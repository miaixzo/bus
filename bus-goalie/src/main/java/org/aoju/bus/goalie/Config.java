package org.aoju.bus.goalie;

import lombok.Data;

/**
 * 服务端配置
 *
 * @author Justubborn
 * @version 6.2.9
 * @since JDK 1.8+
 */
@Data
public class Config {

    /**
     * 请求方法
     */
    public static final String METHOD = "method";
    /**
     * 版本信息
     */
    public static final String VERSION = "v";
    /**
     * 格式化数据
     */
    public static final String FORMAT = "format";
    /**
     * 签名信息
     */
    public static final String SIGN = "sign";

    /**
     * 授权信息
     */
    public static final String X_ACCESS_TOKEN = "X-Access-Token";

    /**
     * 访问来源
     */
    public static final String X_REMOTE_CHANNEL = "x_remote_channel";

    /**
     * 默认数据大小
     */
    public static final Integer MAX_INMEMORY_SIZE = 2 * 2048 * 4096;

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
