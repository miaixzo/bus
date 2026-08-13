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
 * Module: {@code bus.mapper}
 *
 * <p>
 * Provides object-relational mapping and SQL generation for data-access layers.
 *
 * <p>
 * Includes mapping annotations, query criteria, binding and batch operations, SQL builders, database dialects, runtime
 * metadata, auditing, pagination, schema management, tenant isolation, key generation, and visibility control.
 *
 * @author Kimi Liu
 */
module bus.mapper {

    requires java.compiler;
    requires java.desktop;
    requires java.sql;

    requires bus.core;
    requires bus.logger;

    requires static lombok;
    requires static jakarta.persistence;
    requires static org.mybatis;
    requires static org.mybatis.spring;

    exports org.miaixz.bus.mapper;
    exports org.miaixz.bus.mapper.annotation;
    exports org.miaixz.bus.mapper.behavior;
    exports org.miaixz.bus.mapper.binding;
    exports org.miaixz.bus.mapper.binding.basic;
    exports org.miaixz.bus.mapper.binding.batch;
    exports org.miaixz.bus.mapper.binding.condition;
    exports org.miaixz.bus.mapper.binding.cursor;
    exports org.miaixz.bus.mapper.binding.function;
    exports org.miaixz.bus.mapper.binding.list;
    exports org.miaixz.bus.mapper.binding.logical;
    exports org.miaixz.bus.mapper.builder;
    exports org.miaixz.bus.mapper.criteria;
    exports org.miaixz.bus.mapper.dialect;
    exports org.miaixz.bus.mapper.feature.affix;
    exports org.miaixz.bus.mapper.feature.audit;
    exports org.miaixz.bus.mapper.feature.identifier;
    exports org.miaixz.bus.mapper.feature.keygen;
    exports org.miaixz.bus.mapper.feature.operation;
    exports org.miaixz.bus.mapper.feature.paging;
    exports org.miaixz.bus.mapper.feature.populate;
    exports org.miaixz.bus.mapper.feature.schema;
    exports org.miaixz.bus.mapper.feature.tenant;
    exports org.miaixz.bus.mapper.feature.visible;
    exports org.miaixz.bus.mapper.handler;
    exports org.miaixz.bus.mapper.parsing;
    exports org.miaixz.bus.mapper.provider;
    exports org.miaixz.bus.mapper.runtime;

}
