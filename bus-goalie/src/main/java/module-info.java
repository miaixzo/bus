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
/**
 * bus.goalie
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.goalie {

    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.http;
    requires bus.logger;

    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.servlet;
    requires static spring.core;
    requires static spring.beans;
    requires static spring.web;
    requires static spring.webmvc;
    requires static spring.webflux;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static reactor.core;
    requires static reactor.netty.http;
    requires static reactor.netty.core;
    requires static io.netty.handler;
    requires static org.reactivestreams;
    requires static com.google.common;

    exports org.miaixz.bus.goalie;
    exports org.miaixz.bus.goalie.annotation;
    exports org.miaixz.bus.goalie.filter;
    exports org.miaixz.bus.goalie.handler;
    exports org.miaixz.bus.goalie.magic;
    exports org.miaixz.bus.goalie.provider;
    exports org.miaixz.bus.goalie.registry;

}
