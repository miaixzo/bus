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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.miaixz.bus.goalie.support.JsonProvider;
import org.miaixz.bus.goalie.support.XmlProvider;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * 上下文传参
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
public class Context {

    /**
     * 交换内容
     */
    private static final String $ = "_context";

    /**
     * 请求参数
     */
    private Map<String, String> requestMap;

    private Map<String, Part> filePartMap;

    private Assets assets;

    private Format format = Format.json;

    private Channel channel = Channel.web;

    private String token;

    private boolean needDecrypt = false;

    private long startTime;

    public static Context get(ServerWebExchange exchange) {
        Context context = exchange.getAttribute(Context.$);

        return Optional.ofNullable(context).orElseGet(() -> {
            Context empty = new Context();
            exchange.getAttributes().put(Context.$, empty);
            return empty;
        });
    }

    public static Context get(ServerRequest request) {
        return (Context) request.attribute(Context.$).orElseGet(() -> {
            Context empty = new Context();
            request.attributes().put(Context.$, empty);
            return empty;
        });
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum Format {
        xml(new XmlProvider(), MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")),
        json(new JsonProvider(), MediaType.APPLICATION_JSON),
        pdf,
        binary;
        private Provider provider;

        private MediaType mediaType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum Channel {
        web("1", 0),
        app("2", 1),
        ding("3", 1),
        wechat("4", 1),
        other("5", 0);

        private String value;
        private Integer tokenType;

        public static Channel getChannel(String value) {
            return Arrays.stream(Channel.values())
                    .filter(c -> c.getValue().equals(value))
                    .findFirst()
                    .orElse(Channel.other);
        }
    }

}
