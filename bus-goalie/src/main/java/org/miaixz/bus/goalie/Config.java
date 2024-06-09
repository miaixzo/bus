/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.goalie;

import lombok.Getter;
import lombok.Setter;

/**
 * 服务端配置
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
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
    public static final Integer MAX_INMEMORY_SIZE = 100 * 1024 * 1024;

    private final Encrypt encrypt = new Encrypt();
    private final Decrypt decrypt = new Decrypt();
    private final Limit limit = new Limit();
    private String path;
    private int port;

    @Getter
    @Setter
    public static class Encrypt {
        private boolean enabled;
        private String key;
        private String type;
        private String offset;
    }

    @Getter
    @Setter
    public static class Decrypt {
        private boolean enabled;
        private String key;
        private String type;
        private String offset;
    }

    @Getter
    @Setter
    public static class Limit {
        private boolean enabled;
    }

}
