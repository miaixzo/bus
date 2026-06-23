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

    requires bus.auth;
    requires bus.base;
    requires bus.cache;
    requires bus.core;
    requires bus.cortex;
    requires bus.crypto;
    requires bus.extra;
    requires bus.health;
    requires bus.http;
    requires bus.image;
    requires bus.limiter;
    requires bus.logger;
    requires bus.metrics;
    requires bus.mapper;
    requires bus.notify;
    requires bus.office;
    requires bus.pay;
    requires bus.proxy;
    requires bus.sensitive;
    requires bus.setting;
    requires bus.socket;
    requires bus.storage;
    requires bus.tempus;
    requires bus.tracer;
    requires bus.validate;
    requires bus.vortex;

    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.persistence;
    requires static jakarta.servlet;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static spring.boot.jdbc;
    requires static spring.boot.mongodb;
    requires static spring.boot.webmvc;
    requires static spring.aop;
    requires static spring.beans;
    requires static spring.context;
    requires static spring.core;
    requires static spring.expression;
    requires static spring.jdbc;
    requires static spring.web;
    requires static spring.webflux;
    requires static spring.webmvc;
    requires static com.alibaba.fastjson2;
    requires static com.fasterxml.jackson.annotation;
    requires static tools.jackson.core;
    requires static tools.jackson.databind;
    requires static com.google.gson;
    requires static com.zaxxer.hikari;
    requires static curator.client;
    requires static curator.framework;
    requires static dubbo;
    requires static elasticsearch.java;
    requires static elasticsearch.rest5.client;
    requires static io.vertx.core;
    requires static io.vertx.web;
    requires static micrometer.core;
    requires static org.aspectj.weaver;
    requires static org.mongodb.driver.core;
    requires static org.mybatis;
    requires static org.mybatis.spring;
    requires static org.slf4j;
    requires static reactor.core;
    requires static reactor.netty.http;
    requires static org.apache.httpcomponents.client5.httpclient5;
    requires static org.apache.httpcomponents.core5.httpcore5;

    exports org.miaixz.bus.spring;
    exports org.miaixz.bus.spring.annotation;
    exports org.miaixz.bus.spring.autoproxy;
    exports org.miaixz.bus.spring.banner;
    exports org.miaixz.bus.spring.boot;
    exports org.miaixz.bus.spring.env;
    exports org.miaixz.bus.spring.http;
    exports org.miaixz.bus.spring.listener;
    exports org.miaixz.bus.spring.metrics;
    exports org.miaixz.bus.starter.annotation;
    exports org.miaixz.bus.starter.cache;
    exports org.miaixz.bus.starter.cors;
    exports org.miaixz.bus.starter.cortex;
    exports org.miaixz.bus.starter.dubbo;
    exports org.miaixz.bus.starter.elastic;
    exports org.miaixz.bus.starter.vortex;
    exports org.miaixz.bus.starter.health;
    exports org.miaixz.bus.starter.i18n;
    exports org.miaixz.bus.starter.image;
    exports org.miaixz.bus.starter.jdbc;
    exports org.miaixz.bus.starter.limiter;
    exports org.miaixz.bus.starter.metrics;
    exports org.miaixz.bus.starter.mapper;
    exports org.miaixz.bus.starter.mongo;
    exports org.miaixz.bus.starter.notify;
    exports org.miaixz.bus.starter.auth;
    exports org.miaixz.bus.starter.office;
    exports org.miaixz.bus.starter.pay;
    exports org.miaixz.bus.starter.sensitive;
    exports org.miaixz.bus.starter.socket;
    exports org.miaixz.bus.starter.storage;
    exports org.miaixz.bus.starter.tempus;
    exports org.miaixz.bus.starter.tracer;
    exports org.miaixz.bus.starter.validate;
    exports org.miaixz.bus.starter.wrapper;
    exports org.miaixz.bus.starter.zookeeper;
    exports org.miaixz.bus.spring.options;

}
