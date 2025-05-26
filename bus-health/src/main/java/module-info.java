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
 * bus.health
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.health {

    requires java.desktop;
    requires java.management;
    requires bus.core;
    requires bus.logger;
    requires bus.setting;

    requires static lombok;
    requires static com.sun.jna;
    requires static com.sun.jna.platform;

    opens org.miaixz.bus.health to com.sun.jna;
    opens org.miaixz.bus.health.linux to com.sun.jna;
    opens org.miaixz.bus.health.mac to com.sun.jna;
    opens org.miaixz.bus.health.windows to com.sun.jna;
    opens org.miaixz.bus.health.unix to com.sun.jna;
    
    exports org.miaixz.bus.health;
    exports org.miaixz.bus.health.builtin;
    exports org.miaixz.bus.health.builtin.hardware;
    exports org.miaixz.bus.health.builtin.hardware.common;
    exports org.miaixz.bus.health.builtin.jna;
    exports org.miaixz.bus.health.builtin.software;
    exports org.miaixz.bus.health.builtin.software.common;
    exports org.miaixz.bus.health.linux;
    exports org.miaixz.bus.health.linux.driver;
    exports org.miaixz.bus.health.linux.driver.proc;
    exports org.miaixz.bus.health.linux.hardware;
    exports org.miaixz.bus.health.linux.jna;
    exports org.miaixz.bus.health.linux.software;
    exports org.miaixz.bus.health.mac;
    exports org.miaixz.bus.health.mac.driver;
    exports org.miaixz.bus.health.mac.driver.disk;
    exports org.miaixz.bus.health.mac.driver.net;
    exports org.miaixz.bus.health.mac.hardware;
    exports org.miaixz.bus.health.mac.jna;
    exports org.miaixz.bus.health.mac.software;
    exports org.miaixz.bus.health.unix.driver;
    exports org.miaixz.bus.health.unix.hardware;
    exports org.miaixz.bus.health.unix.jna;
    exports org.miaixz.bus.health.unix.platform.aix.driver;
    exports org.miaixz.bus.health.unix.platform.aix.driver.perfstat;
    exports org.miaixz.bus.health.unix.platform.aix.hardware;
    exports org.miaixz.bus.health.unix.platform.aix.software;
    exports org.miaixz.bus.health.unix.platform.freebsd;
    exports org.miaixz.bus.health.unix.platform.freebsd.driver;
    exports org.miaixz.bus.health.unix.platform.freebsd.driver.disk;
    exports org.miaixz.bus.health.unix.platform.freebsd.hardware;
    exports org.miaixz.bus.health.unix.platform.freebsd.software;
    exports org.miaixz.bus.health.unix.platform.openbsd;
    exports org.miaixz.bus.health.unix.platform.openbsd.driver.disk;
    exports org.miaixz.bus.health.unix.platform.openbsd.hardware;
    exports org.miaixz.bus.health.unix.platform.openbsd.software;
    exports org.miaixz.bus.health.unix.platform.solaris;
    exports org.miaixz.bus.health.unix.platform.solaris.driver;
    exports org.miaixz.bus.health.unix.platform.solaris.driver.disk;
    exports org.miaixz.bus.health.unix.platform.solaris.driver.kstat;
    exports org.miaixz.bus.health.unix.platform.solaris.hardware;
    exports org.miaixz.bus.health.unix.platform.solaris.software;
    exports org.miaixz.bus.health.windows;
    exports org.miaixz.bus.health.windows.driver;
    exports org.miaixz.bus.health.windows.driver.perfmon;
    exports org.miaixz.bus.health.windows.driver.registry;
    exports org.miaixz.bus.health.windows.driver.wmi;
    exports org.miaixz.bus.health.windows.hardware;
    exports org.miaixz.bus.health.windows.jna;
    exports org.miaixz.bus.health.windows.software;

}
