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
 * bus.shade
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.shade {

    requires java.sql;
    requires java.desktop;

    requires bus.core;
    requires bus.logger;

    requires lombok;
    requires freemarker;
    requires org.apache.commons.compress;
    requires spring.boot.loader;

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
