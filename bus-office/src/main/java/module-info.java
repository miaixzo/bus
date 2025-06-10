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
