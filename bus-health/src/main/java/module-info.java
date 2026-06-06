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
 * bus.health
 *
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.health {

    requires java.desktop;
    requires java.management;

    requires bus.core;
    requires bus.logger;
    requires bus.setting;

    requires lombok;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    opens org.miaixz.bus.health to com.sun.jna;
    opens org.miaixz.bus.health.linux to com.sun.jna;
    opens org.miaixz.bus.health.mac to com.sun.jna;
    opens org.miaixz.bus.health.windows to com.sun.jna;
    opens org.miaixz.bus.health.builtin.gpu to com.sun.jna;

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
    exports org.miaixz.bus.health.unix.shared.driver;
    exports org.miaixz.bus.health.unix.shared.hardware;
    exports org.miaixz.bus.health.unix.shared.jna;
    exports org.miaixz.bus.health.unix.aix.driver;
    exports org.miaixz.bus.health.unix.aix.driver.perfstat;
    exports org.miaixz.bus.health.unix.aix.hardware;
    exports org.miaixz.bus.health.unix.aix.software;
    exports org.miaixz.bus.health.unix.bsd;
    exports org.miaixz.bus.health.unix.freebsd;
    exports org.miaixz.bus.health.unix.freebsd.driver;
    exports org.miaixz.bus.health.unix.freebsd.driver.disk;
    exports org.miaixz.bus.health.unix.freebsd.hardware;
    exports org.miaixz.bus.health.unix.freebsd.software;
    exports org.miaixz.bus.health.unix.openbsd;
    exports org.miaixz.bus.health.unix.openbsd.driver.disk;
    exports org.miaixz.bus.health.unix.openbsd.hardware;
    exports org.miaixz.bus.health.unix.openbsd.software;
    exports org.miaixz.bus.health.unix.solaris;
    exports org.miaixz.bus.health.unix.solaris.driver;
    exports org.miaixz.bus.health.unix.solaris.driver.disk;
    exports org.miaixz.bus.health.unix.solaris.driver.kstat;
    exports org.miaixz.bus.health.unix.solaris.hardware;
    exports org.miaixz.bus.health.unix.solaris.software;
    exports org.miaixz.bus.health.windows;
    exports org.miaixz.bus.health.windows.driver;
    exports org.miaixz.bus.health.windows.driver.perfmon;
    exports org.miaixz.bus.health.windows.driver.registry;
    exports org.miaixz.bus.health.windows.driver.wmi;
    exports org.miaixz.bus.health.windows.hardware;
    exports org.miaixz.bus.health.windows.jna;
    exports org.miaixz.bus.health.windows.software;
    exports org.miaixz.bus.health.builtin.gpu;

}
