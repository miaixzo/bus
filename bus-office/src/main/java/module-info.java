/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * bus.office
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.office {

    requires java.sql;
    requires java.desktop;

    requires bus.core;
    requires bus.logger;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires ofdrw.converter;
    requires ofdrw.layout;
    requires ofdrw.font;
    requires ofdrw.reader;

    exports org.miaixz.bus.office;
    exports org.miaixz.bus.office.builtin;
    exports org.miaixz.bus.office.csv;
    exports org.miaixz.bus.office.excel;
    exports org.miaixz.bus.office.excel.cell;
    exports org.miaixz.bus.office.excel.cell.editors;
    exports org.miaixz.bus.office.excel.cell.setters;
    exports org.miaixz.bus.office.excel.cell.values;
    exports org.miaixz.bus.office.excel.reader;
    exports org.miaixz.bus.office.excel.sax;
    exports org.miaixz.bus.office.excel.sax.handler;
    exports org.miaixz.bus.office.excel.shape;
    exports org.miaixz.bus.office.excel.style;
    exports org.miaixz.bus.office.excel.writer;
    exports org.miaixz.bus.office.excel.xyz;
    exports org.miaixz.bus.office.ofd;
    exports org.miaixz.bus.office.word;

}
