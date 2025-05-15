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
 * bus.mapper
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.mapper {

    requires java.desktop;

    requires bus.core;
    requires bus.logger;

    requires static org.mybatis;
    requires static jakarta.persistence;

    exports org.miaixz.bus.mapper;
    exports org.miaixz.bus.mapper.additional.aggregation;
    exports org.miaixz.bus.mapper.additional.delete;
    exports org.miaixz.bus.mapper.additional.dialect.oracle;
    exports org.miaixz.bus.mapper.additional.idlist;
    exports org.miaixz.bus.mapper.additional.insert;
    exports org.miaixz.bus.mapper.additional.select;
    exports org.miaixz.bus.mapper.additional.update.batch;
    exports org.miaixz.bus.mapper.additional.update.differ;
    exports org.miaixz.bus.mapper.additional.update.force;
    exports org.miaixz.bus.mapper.additional.upsert;
    exports org.miaixz.bus.mapper.annotation;
    exports org.miaixz.bus.mapper.builder;
    exports org.miaixz.bus.mapper.builder.resolve;
    exports org.miaixz.bus.mapper.common;
    exports org.miaixz.bus.mapper.common.basic;
    exports org.miaixz.bus.mapper.common.basic.delete;
    exports org.miaixz.bus.mapper.common.basic.insert;
    exports org.miaixz.bus.mapper.common.basic.select;
    exports org.miaixz.bus.mapper.common.basic.update;
    exports org.miaixz.bus.mapper.common.condition;
    exports org.miaixz.bus.mapper.common.ids;
    exports org.miaixz.bus.mapper.common.rowbounds;
    exports org.miaixz.bus.mapper.common.special;
    exports org.miaixz.bus.mapper.common.sqlserver;
    exports org.miaixz.bus.mapper.criteria;
    exports org.miaixz.bus.mapper.entity;
    exports org.miaixz.bus.mapper.handler;
    exports org.miaixz.bus.mapper.provider;
    exports org.miaixz.bus.mapper.support;

}