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
 * bus.shade
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.shade {

    requires java.sql;
    requires java.desktop;

    requires bus.core;
    requires bus.logger;

    requires static lombok;
    requires static freemarker;
    requires static org.apache.commons.compress;
    requires static spring.boot.loader;

    exports org.miaixz.bus.shade.beans;
    exports org.miaixz.bus.shade.safety;
    exports org.miaixz.bus.shade.safety.algorithm;
    exports org.miaixz.bus.shade.safety.archive;
    exports org.miaixz.bus.shade.safety.boot;
    exports org.miaixz.bus.shade.safety.boot.jar;
    exports org.miaixz.bus.shade.safety.complex;
    exports org.miaixz.bus.shade.safety.provider;
    exports org.miaixz.bus.shade.safety.streams;
    exports org.miaixz.bus.shade.screw;
    exports org.miaixz.bus.shade.screw.dialect;
    exports org.miaixz.bus.shade.screw.dialect.cachedb;
    exports org.miaixz.bus.shade.screw.dialect.db2;
    exports org.miaixz.bus.shade.screw.dialect.h2;
    exports org.miaixz.bus.shade.screw.dialect.mariadb;
    exports org.miaixz.bus.shade.screw.dialect.mysql;
    exports org.miaixz.bus.shade.screw.dialect.oracle;
    exports org.miaixz.bus.shade.screw.dialect.postgresql;
    exports org.miaixz.bus.shade.screw.dialect.sqlserver;
    exports org.miaixz.bus.shade.screw.engine;
    exports org.miaixz.bus.shade.screw.execute;
    exports org.miaixz.bus.shade.screw.mapping;
    exports org.miaixz.bus.shade.screw.metadata;
    exports org.miaixz.bus.shade.screw.process;

}
