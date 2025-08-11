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

import java.util.Map;
import java.util.Optional;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * 上下文传参类，用于存储和传递请求相关的上下文信息
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Context {

    /**
     * 上下文在 ServerWebExchange 或 ServerRequest 属性中的键名
     */
    private static final String $ = "_context";

    /**
     * 请求参数，存储键值对形式的参数
     */
    private Map<String, String> requestMap;

    /**
     * 文件上传参数，存储文件部分的映射
     */
    private Map<String, Part> filePartMap;

    /**
     * 资产信息，具体内容由 Assets 类定义
     */
    private Assets assets;

    /**
     * 数据格式，默认使用 JSON 格式
     */
    private Format format = Format.JSON;

    /**
     * 请求渠道，默认使用 web 渠道
     */
    private Channel channel = Channel.WEB;

    /**
     * 令牌，用于身份验证或会话管理
     */
    private String token;

    /**
     * 是否需要解密请求数据，默认为 false
     */
    private boolean needDecrypt = false;

    /**
     * 请求开始时间，用于性能监控或日志记录
     */
    private long startTime;

    /**
     * 从 ServerWebExchange 获取或初始化上下文对象
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @return 上下文对象，若不存在则创建新的空上下文
     */
    public static Context get(ServerWebExchange exchange) {
        Context context = exchange.getAttribute(Context.$);
        return Optional.ofNullable(context).orElseGet(() -> {
            Context empty = new Context();
            exchange.getAttributes().put(Context.$, empty);
            return empty;
        });
    }

    /**
     * 从 ServerRequest 获取或初始化上下文对象
     *
     * @param request 当前的 ServerRequest 对象
     * @return 上下文对象，若不存在则创建新的空上下文
     */
    public static Context get(ServerRequest request) {
        return (Context) request.attribute(Context.$).orElseGet(() -> {
            Context empty = new Context();
            request.attributes().put(Context.$, empty);
            return empty;
        });
    }

}