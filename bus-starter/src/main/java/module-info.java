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
 * Module: {@code bus.starter}
 *
 * <p>
 * Provides Spring Boot auto-configuration for the Bus ecosystem.
 *
 * <p>
 * Includes conditional configuration, configuration properties, default beans, startup conventions, and integration
 * wiring for the optional Bus modules and their supported third-party services.
 *
 * @author Kimi Liu
 */
module bus.starter {

    requires java.sql;

    requires bus.auth;
    requires bus.base;
    requires bus.cache;
    requires bus.core;
    requires bus.cortex;
    requires bus.crypto;
    requires bus.extra;
    requires bus.fabric;
    requires bus.health;
    requires bus.image;
    requires bus.limiter;
    requires bus.logger;
    requires bus.mapper;
    requires bus.metrics;
    requires bus.notify;
    requires bus.office;
    requires bus.pay;
    requires bus.proxy;
    requires bus.sensitive;
    requires bus.spring;
    requires bus.storage;
    requires bus.tempus;
    requires bus.tracer;
    requires bus.validate;
    requires bus.vortex;

    requires static com.zaxxer.hikari;
    requires static curator.client;
    requires static curator.framework;
    requires static dubbo;
    requires static elasticsearch.java;
    requires static elasticsearch.rest5.client;
    requires static io.netty.transport;
    requires static jakarta.annotation;
    requires static jakarta.persistence;
    requires static jakarta.servlet;
    requires static lombok;
    requires static micrometer.core;
    requires static org.aspectj.weaver;
    requires static org.apache.httpcomponents.client5.httpclient5;
    requires static org.mongodb.driver.core;
    requires static org.mybatis;
    requires static org.mybatis.spring;
    requires static reactor.core;
    requires static reactor.netty.http;
    requires static spring.beans;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static spring.boot.health;
    requires static spring.boot.jdbc;
    requires static spring.boot.mongodb;
    requires static spring.boot.webmvc;
    requires static spring.context;
    requires static spring.core;
    requires static spring.jdbc;
    requires static spring.web;
    requires static spring.webflux;
    requires static spring.webmvc;

    exports org.miaixz.bus.starter;
    exports org.miaixz.bus.starter.annotation;
    exports org.miaixz.bus.starter.auth;
    exports org.miaixz.bus.starter.cache;
    exports org.miaixz.bus.starter.context;
    exports org.miaixz.bus.starter.cors;
    exports org.miaixz.bus.starter.cortex;
    exports org.miaixz.bus.starter.dubbo;
    exports org.miaixz.bus.starter.elastic;
    exports org.miaixz.bus.starter.fabric;
    exports org.miaixz.bus.starter.health;
    exports org.miaixz.bus.starter.i18n;
    exports org.miaixz.bus.starter.image;
    exports org.miaixz.bus.starter.jdbc;
    exports org.miaixz.bus.starter.json;
    exports org.miaixz.bus.starter.limiter;
    exports org.miaixz.bus.starter.mapper;
    exports org.miaixz.bus.starter.metrics;
    exports org.miaixz.bus.starter.mongo;
    exports org.miaixz.bus.starter.notify;
    exports org.miaixz.bus.starter.office;
    exports org.miaixz.bus.starter.pay;
    exports org.miaixz.bus.starter.sensitive;
    exports org.miaixz.bus.starter.storage;
    exports org.miaixz.bus.starter.tempus;
    exports org.miaixz.bus.starter.tracer;
    exports org.miaixz.bus.starter.validate;
    exports org.miaixz.bus.starter.vortex;
    exports org.miaixz.bus.starter.wrapper;
    exports org.miaixz.bus.starter.wrapper.advice;
    exports org.miaixz.bus.starter.wrapper.binding;
    exports org.miaixz.bus.starter.wrapper.body;
    exports org.miaixz.bus.starter.wrapper.converter;
    exports org.miaixz.bus.starter.wrapper.routing;
    exports org.miaixz.bus.starter.zookeeper;

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
