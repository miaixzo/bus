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
 * bus.base
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.base {

    requires bus.core;
    requires bus.logger;
    requires bus.mapper;
    requires bus.validate;

    requires lombok;
    requires jakarta.persistence;
    requires jakarta.servlet;
    requires spring.beans;
    requires spring.web;
    requires spring.context;
    requires spring.webmvc;
    requires spring.boot.autoconfigure;

    exports org.miaixz.bus.base.advice;
    exports org.miaixz.bus.base.entity;
    exports org.miaixz.bus.base.mapper;
    exports org.miaixz.bus.base.service;
    exports org.miaixz.bus.base.spring;

}
