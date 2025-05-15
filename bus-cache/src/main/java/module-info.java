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
 * bus.cache
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.cache {

    requires java.desktop;
    requires java.sql;
    requires java.management;
    requires java.compiler;
    requires java.naming;

    requires bus.core;
    requires bus.logger;
    requires bus.extra;
    requires bus.setting;
    requires bus.proxy;

    requires static lombok;
    requires static jakarta.annotation;
    requires static spring.jdbc;
    requires static spring.expression;
    requires static com.zaxxer.hikari;
    requires static redis.clients.jedis;
    requires static xmemcached;
    requires static zookeeper;
    requires static hessian;
    requires static com.google.common;
    requires static com.google.guice;
    requires static curator.framework;
    requires static curator.recipes;
    requires static curator.client;

    exports org.miaixz.bus.cache;
    exports org.miaixz.bus.cache.magic;
    exports org.miaixz.bus.cache.magic.annotation;
    exports org.miaixz.bus.cache.metric;
    exports org.miaixz.bus.cache.provider;
    exports org.miaixz.bus.cache.serialize;
    exports org.miaixz.bus.cache.support;

}
