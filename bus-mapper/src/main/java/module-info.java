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
 * bus.mapper
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.mapper {

    requires java.compiler;
    requires java.desktop;

    requires bus.core;
    requires bus.logger;

    requires lombok;
    requires jakarta.persistence;
    requires org.mybatis;
    requires org.mybatis.spring;

    exports org.miaixz.bus.mapper;
    exports org.miaixz.bus.mapper.annotation;
    exports org.miaixz.bus.mapper.binding;
    exports org.miaixz.bus.mapper.builder;
    exports org.miaixz.bus.mapper.criteria;
    exports org.miaixz.bus.mapper.dialect;
    exports org.miaixz.bus.mapper.handler;
    exports org.miaixz.bus.mapper.parsing;
    exports org.miaixz.bus.mapper.provider;
    exports org.miaixz.bus.mapper.binding.basic;
    exports org.miaixz.bus.mapper.binding.batch;
    exports org.miaixz.bus.mapper.binding.condition;
    exports org.miaixz.bus.mapper.binding.cursor;
    exports org.miaixz.bus.mapper.binding.function;
    exports org.miaixz.bus.mapper.binding.list;
    exports org.miaixz.bus.mapper.binding.logical;
    exports org.miaixz.bus.mapper.support.audit;
    exports org.miaixz.bus.mapper.support.keygen;
    exports org.miaixz.bus.mapper.support.operation;
    exports org.miaixz.bus.mapper.support.paging;
    exports org.miaixz.bus.mapper.support.tenant;
    exports org.miaixz.bus.mapper.support.populate;
    exports org.miaixz.bus.mapper.support.visible;
    exports org.miaixz.bus.mapper.support.prefix;

}
