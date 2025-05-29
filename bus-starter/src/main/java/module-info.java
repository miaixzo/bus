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

    requires lombok;
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires jakarta.servlet;
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.jdbc;
    requires spring.web;
    requires spring.webflux;
    requires spring.webmvc;
    requires undertow.core;
    requires undertow.servlet;
    requires undertow.websockets.jsr;
    requires com.alibaba.fastjson2;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.google.gson;
    requires com.zaxxer.hikari;
    requires curator.client;
    requires curator.framework;
    requires dubbo;
    requires elasticsearch.java;
    requires elasticsearch.rest.client;
    requires io.vertx.core;
    requires io.vertx.web;
    requires org.aspectj.weaver;
    requires org.jboss.logging;
    requires org.mongodb.driver.core;
    requires org.mybatis;
    requires org.mybatis.spring;
    requires org.slf4j;
    requires reactor.core;
    requires reactor.netty.http;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpasyncclient;

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