/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * bus.starter
 *
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.starter {

    requires java.datatransfer;
    requires java.desktop;
    requires java.management;
    requires java.sql;

    requires bus.core;
    requires bus.extra;
    requires bus.logger;
    requires bus.spring;
    requires static bus.auth;
    requires static bus.base;
    requires static bus.cache;
    requires static bus.cortex;
    requires static bus.crypto;
    requires static bus.fabric;
    requires static bus.health;
    requires static bus.image;
    requires static bus.limiter;
    requires static bus.mapper;
    requires static bus.metrics;
    requires static bus.notify;
    requires static bus.office;
    requires static bus.pay;
    requires static bus.proxy;
    requires static bus.sensitive;
    requires static bus.storage;
    requires static bus.tempus;
    requires static bus.tracer;
    requires static bus.validate;
    requires static bus.vortex;

    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.persistence;
    requires static jakarta.servlet;
    requires static spring.aop;
    requires static spring.beans;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static spring.boot.health;
    requires static spring.boot.jdbc;
    requires static spring.boot.mongodb;
    requires static spring.boot.webmvc;
    requires static spring.context;
    requires static spring.core;
    requires static spring.expression;
    requires static spring.jdbc;
    requires static spring.web;
    requires static spring.webflux;
    requires static spring.webmvc;
    requires static com.zaxxer.hikari;
    requires static curator.client;
    requires static curator.framework;
    requires static dubbo;
    requires static elasticsearch.java;
    requires static elasticsearch.rest5.client;
    requires static io.vertx.core;
    requires static io.vertx.web;
    requires static io.netty.transport;
    requires static micrometer.core;
    requires static org.apache.httpcomponents.client5.httpclient5;
    requires static org.apache.httpcomponents.core5.httpcore5;
    requires static org.aspectj.weaver;
    requires static org.mongodb.driver.core;
    requires static org.mybatis;
    requires static org.mybatis.spring;
    requires static org.slf4j;
    requires static reactor.core;
    requires static reactor.netty.core;
    requires static reactor.netty.http;

    exports org.miaixz.bus.starter.annotation;

    opens org.miaixz.bus.starter to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.auth to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.cache to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.context to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.cors to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.cortex to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.dubbo to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.elastic to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.fabric to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.health to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.i18n to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.image to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.jdbc to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.json to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.limiter to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.mapper to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.metrics to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.mongo to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.notify to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.office to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.pay to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.sensitive to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.storage to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.tempus to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.tracer to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.validate to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.vortex to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.wrapper to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.wrapper.advice to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.wrapper.binding to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.wrapper.body to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.wrapper.converter to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.wrapper.routing to spring.beans, spring.boot, spring.context, spring.core;
    opens org.miaixz.bus.starter.zookeeper to spring.beans, spring.boot, spring.context, spring.core;

}
