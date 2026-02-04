/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    requires lombok;
    requires jakarta.annotation;
    requires spring.jdbc;
    requires spring.expression;
    requires com.zaxxer.hikari;
    requires redis.clients.jedis;
    requires xmemcached;
    requires zookeeper;
    requires hessian;
    requires com.google.common;
    requires curator.framework;
    requires curator.recipes;
    requires curator.client;
    requires com.github.benmanes.caffeine;

    exports org.miaixz.bus.cache;
    exports org.miaixz.bus.cache.magic;
    exports org.miaixz.bus.cache.magic.annotation;
    exports org.miaixz.bus.cache.metric;
    exports org.miaixz.bus.cache.reader;
    exports org.miaixz.bus.cache.support;
    exports org.miaixz.bus.cache.support.metrics;
    exports org.miaixz.bus.cache.support.serialize;

}
