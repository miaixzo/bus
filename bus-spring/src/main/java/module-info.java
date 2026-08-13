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
 * Module: {@code bus.spring}
 *
 * <p>
 * Provides shared integration utilities for Spring Framework and Spring Boot applications.
 *
 * <p>
 * Includes annotations, AOP and bean utilities, startup and banner hooks, conditional configuration, JDBC helpers, web
 * advice, converters, interceptors, argument resolvers, request wrappers, and routing support.
 *
 * @author Kimi Liu
 */
module bus.spring {

    requires java.desktop;
    requires java.management;
    requires java.sql;

    requires bus.core;
    requires bus.extra;
    requires bus.logger;

    requires static lombok;
    requires static jakarta.persistence;
    requires static jakarta.servlet;
    requires static spring.aop;
    requires static spring.beans;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static spring.boot.webmvc;
    requires static spring.context;
    requires static spring.core;
    requires static spring.jdbc;
    requires static spring.web;
    requires static spring.webmvc;
    requires static org.aspectj.weaver;

    exports org.miaixz.bus.spring;
    exports org.miaixz.bus.spring.annotation;
    exports org.miaixz.bus.spring.aop;
    exports org.miaixz.bus.spring.bean;
    exports org.miaixz.bus.spring.boot.banner;
    exports org.miaixz.bus.spring.boot.condition;
    exports org.miaixz.bus.spring.boot.startup;
    exports org.miaixz.bus.spring.jdbc;
    exports org.miaixz.bus.spring.web;
    exports org.miaixz.bus.spring.web.advice;
    exports org.miaixz.bus.spring.web.converter;
    exports org.miaixz.bus.spring.web.interceptor;
    exports org.miaixz.bus.spring.web.resolver;
    exports org.miaixz.bus.spring.web.routing;
    exports org.miaixz.bus.spring.web.wrapper;

    opens org.miaixz.bus.spring.boot to spring.boot, spring.core;
    opens org.miaixz.bus.spring.boot.environment to spring.boot, spring.core;
    opens org.miaixz.bus.spring.boot.listener to spring.boot, spring.core;
    opens org.miaixz.bus.spring.boot.startup to spring.boot;

}
