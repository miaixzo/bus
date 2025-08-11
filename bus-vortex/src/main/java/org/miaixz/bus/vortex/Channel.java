/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * 请求渠道枚举，定义不同的请求来源及其属性。
 * <p>
 * 该枚举类用于标识请求的来源渠道（如 WEB、APP、钉钉、微信等），并为每个渠道关联一个字符串值和令牌类型。 每个枚举值通过构造函数初始化其属性，并提供静态方法 {@link #get(String)}
 * 用于根据字符串值获取对应的枚举实例。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Channel {

    /**
     * WEB 请求，表示通过浏览器或网页发起的请求。
     */
    WEB("1", 0),

    /**
     * APP 请求，表示通过移动应用程序发起的请求。
     */
    APP("2", 1),

    /**
     * 钉钉请求，表示通过钉钉平台发起的请求。
     */
    DINGTALK("3", 1),

    /**
     * 微信请求，表示通过微信平台发起的请求。
     */
    WECHAT("4", 1),

    /**
     * 其他请求，表示无法归类到特定渠道的请求，作为默认回退值。
     */
    OTHER("5", 0);

    /**
     * 渠道的字符串值，用于唯一标识渠道。
     */
    private String value;

    /**
     * 令牌类型，用于区分不同渠道的令牌处理方式。
     * <p>
     * 值为 0 表示不需要特殊令牌处理，值为 1 表示需要特定的令牌处理逻辑。
     * </p>
     */
    private Integer type;

    /**
     * 根据渠道值获取对应的渠道枚举实例。
     * <p>
     * 该方法通过给定的字符串值查找匹配的枚举实例。如果未找到匹配的渠道，则返回 {@link #OTHER} 作为默认值。
     * </p>
     *
     * @param value 渠道的字符串值
     * @return 匹配的 {@link Channel} 枚举实例，若无匹配则返回 {@link #OTHER}
     */
    public static Channel get(String value) {
        return Arrays.stream(Channel.values()).filter(c -> c.getValue().equals(value)).findFirst()
                .orElse(Channel.OTHER);
    }

}