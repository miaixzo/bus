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
 * bus.starter
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.starter {

    requires java.datatransfer;
    requires java.desktop;
    requires java.management;

    requires bus.base;
    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.goalie;
    requires bus.health;
    requires bus.http;
    requires bus.image;
    requires bus.limiter;
    requires bus.logger;
    requires bus.mapper;
    requires bus.notify;
    requires bus.oauth;
    requires bus.office;
    requires bus.pager;
    requires bus.pay;
    requires bus.proxy;
    requires bus.sensitive;
    requires bus.setting;
    requires bus.socket;
    requires bus.storage;
    requires bus.validate;

    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.persistence;
    requires static jakarta.servlet;
    requires static spring.aop;
    requires static spring.beans;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static spring.context;
    requires static spring.core;
    requires static spring.jdbc;
    requires static spring.web;
    requires static spring.webflux;
    requires static spring.webmvc;
    requires static undertow.core;
    requires static undertow.servlet;
    requires static undertow.websockets.jsr;
    requires static com.alibaba.fastjson2;
    requires static com.fasterxml.jackson.annotation;
    requires static com.fasterxml.jackson.databind;
    requires static com.fasterxml.jackson.datatype.jsr310;
    requires static com.google.gson;
    requires static com.zaxxer.hikari;
    requires static curator.client;
    requires static curator.framework;
    requires static dubbo;
    requires static elasticsearch.java;
    requires static elasticsearch.rest.client;
    requires static io.vertx.core;
    requires static io.vertx.web;
    requires static org.aspectj.weaver;
    requires static org.jboss.logging;
    requires static org.mongodb.driver.core;
    requires static org.mybatis;
    requires static org.mybatis.spring;
    requires static org.slf4j;
    requires static reactor.core;
    requires static reactor.netty.http;
    requires static org.apache.httpcomponents.httpclient;
    requires static org.apache.httpcomponents.httpcore;
    requires static org.apache.httpcomponents.httpasyncclient;

    exports org.miaixz.bus.spring;
    exports org.miaixz.bus.spring.annotation;
    exports org.miaixz.bus.spring.autoproxy;
    exports org.miaixz.bus.spring.banner;
    exports org.miaixz.bus.spring.boot;
    exports org.miaixz.bus.spring.boot.statics;
    exports org.miaixz.bus.spring.env;
    exports org.miaixz.bus.spring.listener;
    exports org.miaixz.bus.spring.undertow;
    exports org.miaixz.bus.spring.web;
    exports org.miaixz.bus.starter.annotation;
    exports org.miaixz.bus.starter.bridge;
    exports org.miaixz.bus.starter.cache;
    exports org.miaixz.bus.starter.cors;
    exports org.miaixz.bus.starter.dubbo;
    exports org.miaixz.bus.starter.elastic;
    exports org.miaixz.bus.starter.goalie;
    exports org.miaixz.bus.starter.health;
    exports org.miaixz.bus.starter.i18n;
    exports org.miaixz.bus.starter.image;
    exports org.miaixz.bus.starter.jdbc;
    exports org.miaixz.bus.starter.limiter;
    exports org.miaixz.bus.starter.mapper;
    exports org.miaixz.bus.starter.mongo;
    exports org.miaixz.bus.starter.notify;
    exports org.miaixz.bus.starter.oauth;
    exports org.miaixz.bus.starter.office;
    exports org.miaixz.bus.starter.pay;
    exports org.miaixz.bus.starter.sensitive;
    exports org.miaixz.bus.starter.socket;
    exports org.miaixz.bus.starter.storage;
    exports org.miaixz.bus.starter.tracer;
    exports org.miaixz.bus.starter.validate;
    exports org.miaixz.bus.starter.wrapper;
    exports org.miaixz.bus.starter.zookeeper;

}