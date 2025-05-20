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
 * bus.http
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.http {

    requires bus.core;
    requires bus.logger;

    requires static lombok;
    requires static jakarta.xml.soap;

    exports org.miaixz.bus.http;
    exports org.miaixz.bus.http.accord;
    exports org.miaixz.bus.http.accord.platform;
    exports org.miaixz.bus.http.bodys;
    exports org.miaixz.bus.http.cache;
    exports org.miaixz.bus.http.metric;
    exports org.miaixz.bus.http.metric.anget;
    exports org.miaixz.bus.http.metric.http;
    exports org.miaixz.bus.http.metric.proxy;
    exports org.miaixz.bus.http.metric.suffix;
    exports org.miaixz.bus.http.plugin.httpv;
    exports org.miaixz.bus.http.plugin.httpx;
    exports org.miaixz.bus.http.plugin.httpz;
    exports org.miaixz.bus.http.plugin.soap;
    exports org.miaixz.bus.http.secure;
    exports org.miaixz.bus.http.socket;

}