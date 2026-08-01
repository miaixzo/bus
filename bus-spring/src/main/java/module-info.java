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
 * bus.spring
 *
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.spring {

    requires java.desktop;
    requires java.management;

    requires bus.cache;
    requires bus.core;
    requires bus.extra;
    requires bus.logger;
    requires bus.validate;

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
    requires static spring.web;
    requires static spring.webmvc;

    exports org.miaixz.bus.spring;
    exports org.miaixz.bus.spring.annotation;
    exports org.miaixz.bus.spring.autoproxy;
    exports org.miaixz.bus.spring.banner;
    exports org.miaixz.bus.spring.boot;
    exports org.miaixz.bus.spring.env;
    exports org.miaixz.bus.spring.http;
    exports org.miaixz.bus.spring.listener;
    exports org.miaixz.bus.spring.metrics;
    exports org.miaixz.bus.spring.options;

}
