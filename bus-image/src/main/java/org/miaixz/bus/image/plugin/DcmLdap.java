/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.plugin;

import java.io.Closeable;
import java.util.EnumSet;
import java.util.Hashtable;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfiguration;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.api.DicomConfiguration;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DcmLdap implements Closeable {

    private static final String DEFAULT_LDAP_URI = "ldap://localhost:389/dc=miaixz,dc=org";
    private static final String DEFAULT_BIND_DN = "cn=admin,dc=miaixz,dc=org";
    private static final String DEFAULT_PASSWORD = "secret";
    private final LdapDicomConfiguration conf;
    private String deviceName;
    private String deviceDesc;
    private String deviceType;
    private String aeTitle;
    private String aeDesc;
    private Connection conn;

    public DcmLdap(Hashtable<?, ?> env) throws InternalException {
        conf = new LdapDicomConfiguration(env);
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceDescription(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setAEDescription(String aeDesc) {
        this.aeDesc = aeDesc;
    }

    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public void close() {
        conf.close();
    }

    public void createNetworkAE() throws InternalException {
        Device device = new Device(deviceName != null ? deviceName : aeTitle.toLowerCase());
        device.setDescription(deviceDesc);
        if (deviceType != null) {
            device.setPrimaryDeviceTypes(deviceType);
        }
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        device.addApplicationEntity(ae);
        ae.setDescription(aeDesc);
        ae.addConnection(conn);
        conf.persist(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    public void addNetworkAE() throws InternalException {
        Device device = conf.findDevice(deviceName);
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        device.addApplicationEntity(ae);
        ae.setDescription(aeDesc);
        ae.addConnection(conn);
        conf.merge(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    public void removeNetworkAE() throws InternalException {
        ApplicationEntity ae = conf.findApplicationEntity(aeTitle);
        Device device = ae.getDevice();
        device.removeApplicationEntity(aeTitle);
        for (Connection conn : ae.getConnections()) {
            device.removeConnection(conn);
        }
        if (device.getApplicationAETitles().isEmpty())
            conf.removeDevice(device.getDeviceName(), EnumSet.of(DicomConfiguration.Option.REGISTER));
        else
            conf.merge(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

}
