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
package org.miaixz.bus.image.metric.hl7.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.builtin.ldap.LdapBuilder;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfigurationExtension;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.metric.hl7.api.HL7Configuration;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;
import org.miaixz.bus.image.metric.hl7.net.HL7ApplicationInfo;
import org.miaixz.bus.image.metric.hl7.net.HL7DeviceExtension;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapHL7Configuration extends LdapDicomConfigurationExtension implements HL7Configuration {

    static final String[] HL7_ATTRS = { "dicomDeviceName", "hl7ApplicationName", "hl7OtherApplicationName",
            "dicomDescription", "dicomApplicationCluster", "dicomInstalled", "dicomNetworkConnectionReference" };
    private static final String CN_UNIQUE_HL7_APPLICATION_NAMES_REGISTRY = "cn=Unique HL7 Application Names Registry,";
    private final List<LdapHL7ConfigurationExtension> extensions = new ArrayList<>();
    private String appNamesRegistryDN;

    public void addHL7ConfigurationExtension(LdapHL7ConfigurationExtension ext) {
        ext.setHL7Configuration(this);
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(LdapHL7ConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        ext.setHL7Configuration(null);
        return true;
    }

    @Override
    public boolean registerHL7Application(String name) throws InternalException {
        try {
            registerHL7App(name);
            return true;
        } catch (AlreadyExistsException e) {
            return false;
        }
    }

    private String registerHL7App(String name) throws InternalException {
        ensureAppNamesRegistryExists();
        try {
            String dn = hl7appDN(name, appNamesRegistryDN);
            config.createSubcontext(dn, LdapBuilder.attrs("hl7UniqueApplicationName", "hl7ApplicationName", name));
            return dn;
        } catch (NameAlreadyBoundException e) {
            throw new AlreadyExistsException("HL7 Application '" + name + "' already exists");
        } catch (NamingException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void unregisterHL7Application(String name) throws InternalException {
        if (appNamesRegistryExists())
            try {
                config.destroySubcontext(hl7appDN(name, appNamesRegistryDN));
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
                throw new InternalException(e);
            }
    }

    private void ensureAppNamesRegistryExists() throws InternalException {
        if (appNamesRegistryDN != null)
            return;

        config.ensureConfigurationExists();
        String dn = CN_UNIQUE_HL7_APPLICATION_NAMES_REGISTRY + config.getConfigurationDN();
        try {
            if (!config.exists(dn))
                config.createSubcontext(dn, LdapBuilder.attrs("hl7UniqueApplicationNamesRegistryRoot", "cn",
                        "Unique HL7 Application Names Registry"));
        } catch (NamingException e) {
            throw new InternalException(e);
        }
        appNamesRegistryDN = dn;
    }

    private boolean appNamesRegistryExists() throws InternalException {
        if (appNamesRegistryDN != null)
            return true;

        if (!config.configurationExists())
            return false;

        String dn = CN_UNIQUE_HL7_APPLICATION_NAMES_REGISTRY + config.getConfigurationDN();
        try {
            if (!config.exists(dn))
                return false;
        } catch (NamingException e) {
            throw new InternalException(e);
        }

        appNamesRegistryDN = dn;
        return true;
    }

    @Override
    public String[] listRegisteredHL7ApplicationNames() throws InternalException {
        if (!appNamesRegistryExists())
            return Normal.EMPTY_STRING_ARRAY;

        return config.list(appNamesRegistryDN, "(objectclass=hl7UniqueApplicationName)", "hl7ApplicationName");
    }

    @Override
    public HL7Application findHL7Application(String name) throws InternalException {
        Device device = config.findDevice("(&(objectclass=hl7Application)(hl7ApplicationName=" + name + "))", name);
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext.getHL7Application(name);
    }

    @Override
    public synchronized HL7ApplicationInfo[] listHL7AppInfos(HL7ApplicationInfo keys) throws InternalException {
        if (!config.configurationExists())
            return new HL7ApplicationInfo[0];

        ArrayList<HL7ApplicationInfo> results = new ArrayList<>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            String deviceName = keys.getDeviceName();
            ne = config.search(deviceName, HL7_ATTRS, toFilter(keys));
            Map<String, Connection> connCache = new HashMap<>();
            while (ne.hasMore()) {
                HL7ApplicationInfo hl7AppInfo = new HL7ApplicationInfo();
                SearchResult ne1 = ne.next();
                loadFrom(hl7AppInfo, ne1.getAttributes(),
                        deviceName != null ? deviceName : LdapBuilder.cutDeviceName(ne1.getName()), connCache);
                results.add(hl7AppInfo);
            }
        } catch (NameNotFoundException e) {
            return new HL7ApplicationInfo[0];
        } catch (NamingException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
        return results.toArray(new HL7ApplicationInfo[results.size()]);
    }

    private void loadFrom(HL7ApplicationInfo hl7AppInfo, Attributes attrs, String deviceName,
            Map<String, Connection> connCache) throws NamingException, InternalException {
        hl7AppInfo.setDeviceName(deviceName);
        hl7AppInfo.setHl7ApplicationName(LdapBuilder.stringValue(attrs.get("hl7ApplicationName"), null));
        hl7AppInfo.setHl7OtherApplicationName(LdapBuilder.stringArray(attrs.get("hl7OtherApplicationName")));
        hl7AppInfo.setDescription(LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        hl7AppInfo.setApplicationClusters(LdapBuilder.stringArray(attrs.get("dicomApplicationCluster")));
        hl7AppInfo.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
        for (String connDN : LdapBuilder.stringArray(attrs.get("dicomNetworkConnectionReference")))
            hl7AppInfo.getConnections().add(config.findConnection(connDN, connCache));
    }

    private String toFilter(HL7ApplicationInfo keys) {
        if (keys == null)
            return "(objectclass=hl7Application)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=hl7Application)");
        appendFilter("hl7ApplicationName", keys.getHl7ApplicationName(), sb);
        appendFilter("hl7OtherApplicationName", keys.getHl7ApplicationName(), sb);
        appendFilter("dicomApplicationCluster", keys.getApplicationClusters(), sb);
        sb.append(")");
        return sb.toString();
    }

    private void appendFilter(String attrid, String value, StringBuilder sb) {
        if (value == null)
            return;

        sb.append('(').append(attrid).append('=').append(value).append(')');
    }

    private void appendFilter(String attrid, String[] values, StringBuilder sb) {
        if (values.length == 0)
            return;

        if (values.length == 1) {
            appendFilter(attrid, values[0], sb);
            return;
        }

        sb.append("(|");
        for (String value : values)
            appendFilter(attrid, value, sb);
        sb.append(")");
    }

    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (HL7Application hl7App : hl7Ext.getHL7Applications())
            store(diffs, hl7App, deviceDN);
    }

    private void store(ConfigurationChanges diffs, HL7Application hl7App, String deviceDN) throws NamingException {
        String appDN = hl7appDN(hl7App.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges.addModifiedObject(diffs, appDN,
                ConfigurationChanges.ChangeType.C);
        config.createSubcontext(appDN, storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj), hl7App,
                deviceDN, new BasicAttributes(true)));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeChilds(ConfigurationChanges.nullifyIfNotVerbose(diffs, diffs), appDN, hl7App);
    }

    private String hl7appDN(String name, String deviceDN) {
        return LdapBuilder.dnOf("hl7ApplicationName", name, deviceDN);
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, HL7Application hl7App, String deviceDN,
            Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "hl7Application"));
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "hl7ApplicationName", hl7App.getApplicationName(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "hl7AcceptedSendingApplication",
                hl7App.getAcceptedSendingApplications());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "hl7OtherApplicationName", hl7App.getOtherApplicationNames());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "hl7AcceptedMessageType", hl7App.getAcceptedMessageTypes());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "hl7DefaultCharacterSet", hl7App.getHL7DefaultCharacterSet(),
                "ASCII");
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "hl7SendingCharacterSet", hl7App.getHL7SendingCharacterSet(),
                "ASCII");
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "hl7OptionalMSHField", hl7App.getOptionalMSHFields());
        LdapBuilder.storeConnRefs(ldapObj, attrs, hl7App.getConnections(), deviceDN);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", hl7App.getDescription(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomApplicationCluster", hl7App.getApplicationClusters());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", hl7App.getInstalled(), null);
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeTo(ldapObj, hl7App, deviceDN, attrs);
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN) throws NamingException, InternalException {
        NamingEnumeration<SearchResult> ne = config.search(deviceDN, "(objectclass=hl7Application)");
        try {
            if (!ne.hasMore())
                return;

            HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
            device.addDeviceExtension(hl7Ext);
            do {
                hl7Ext.addHL7Application(loadHL7Application(ne.next(), deviceDN, device));
            } while (ne.hasMore());
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private HL7Application loadHL7Application(SearchResult sr, String deviceDN, Device device)
            throws NamingException, InternalException {
        Attributes attrs = sr.getAttributes();
        HL7Application hl7app = new HL7Application(LdapBuilder.stringValue(attrs.get("hl7ApplicationName"), null));
        loadFrom(hl7app, attrs);
        for (String connDN : LdapBuilder.stringArray(attrs.get("dicomNetworkConnectionReference")))
            hl7app.addConnection(LdapBuilder.findConnection(connDN, deviceDN, device));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.loadChilds(hl7app, sr.getNameInNamespace());
        return hl7app;
    }

    private void loadFrom(HL7Application hl7app, Attributes attrs) throws NamingException {
        hl7app.setAcceptedSendingApplications(LdapBuilder.stringArray(attrs.get("hl7AcceptedSendingApplication")));
        hl7app.setOtherApplicationNames(LdapBuilder.stringArray(attrs.get("hl7OtherApplicationName")));
        hl7app.setAcceptedMessageTypes(LdapBuilder.stringArray(attrs.get("hl7AcceptedMessageType")));
        hl7app.setHL7DefaultCharacterSet(LdapBuilder.stringValue(attrs.get("hl7DefaultCharacterSet"), "ASCII"));
        hl7app.setHL7SendingCharacterSet(LdapBuilder.stringValue(attrs.get("hl7SendingCharacterSet"), "ASCII"));
        hl7app.setOptionalMSHFields(LdapBuilder.intArray(attrs.get("hl7OptionalMSHField")));
        hl7app.setDescription(LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        hl7app.setApplicationClusters(LdapBuilder.stringArray(attrs.get("dicomApplicationCluster")));
        hl7app.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.loadFrom(hl7app, attrs);
    }

    @Override
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException {
        HL7DeviceExtension prevHL7Ext = prev.getDeviceExtension(HL7DeviceExtension.class);
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);

        if (prevHL7Ext != null)
            for (String appName : prevHL7Ext.getHL7ApplicationNames()) {
                if (hl7Ext == null || !hl7Ext.containsHL7Application(appName)) {
                    config.destroySubcontextWithChilds(hl7appDN(appName, deviceDN));
                    ConfigurationChanges.addModifiedObject(diffs, hl7appDN(appName, deviceDN),
                            ConfigurationChanges.ChangeType.D);
                }
            }

        if (hl7Ext == null)
            return;

        for (HL7Application hl7app : hl7Ext.getHL7Applications()) {
            String appName = hl7app.getApplicationName();
            if (prevHL7Ext == null || !prevHL7Ext.containsHL7Application(appName)) {
                store(diffs, hl7app, deviceDN);
            } else
                merge(diffs, prevHL7Ext.getHL7Application(appName), hl7app, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, HL7Application prev, HL7Application app, String deviceDN)
            throws NamingException {
        String appDN = hl7appDN(app.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges.addModifiedObject(diffs, appDN,
                ConfigurationChanges.ChangeType.U);
        config.modifyAttributes(appDN, storeDiffs(ldapObj, prev, app, deviceDN, new ArrayList<>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.mergeChilds(diffs, prev, app, appDN);
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, HL7Application a,
            HL7Application b, String deviceDN, List<ModificationItem> mods) {
        LdapBuilder.storeDiff(ldapObj, mods, "hl7AcceptedSendingApplication", a.getAcceptedSendingApplications(),
                b.getAcceptedSendingApplications());
        LdapBuilder.storeDiff(ldapObj, mods, "hl7OtherApplicationName", a.getOtherApplicationNames(),
                b.getOtherApplicationNames());
        LdapBuilder.storeDiff(ldapObj, mods, "hl7AcceptedMessageType", a.getAcceptedMessageTypes(),
                b.getAcceptedMessageTypes());
        LdapBuilder.storeDiffObject(ldapObj, mods, "hl7DefaultCharacterSet", a.getHL7DefaultCharacterSet(),
                b.getHL7DefaultCharacterSet(), "ASCII");
        LdapBuilder.storeDiffObject(ldapObj, mods, "hl7SendingCharacterSet", a.getHL7SendingCharacterSet(),
                b.getHL7SendingCharacterSet(), "ASCII");
        LdapBuilder.storeDiff(ldapObj, mods, "hl7OptionalMSHField", a.getOptionalMSHFields(), b.getOptionalMSHFields());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference", a.getConnections(), b.getConnections(),
                deviceDN);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomDescription", a.getDescription(), b.getDescription(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomApplicationCluster", a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomInstalled", a.getInstalled(), b.getInstalled(), null);
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeDiffs(ldapObj, a, b, mods);
        return mods;
    }

    @Override
    protected void register(Device device, List<String> dns) throws InternalException {
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (String name : hl7Ext.getHL7ApplicationNames()) {
            if (!name.equals("*"))
                dns.add(registerHL7App(name));
        }
    }

    @Override
    protected void registerDiff(Device prev, Device device, List<String> dns) throws InternalException {
        HL7DeviceExtension prevHL7Ext = prev.getDeviceExtension(HL7DeviceExtension.class);
        if (prevHL7Ext == null) {
            register(device, dns);
            return;
        }

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (String name : hl7Ext.getHL7ApplicationNames()) {
            if (!name.equals("*") && prevHL7Ext.getHL7Application(name) == null)
                dns.add(registerHL7App(name));
        }
    }

    @Override
    protected void markForUnregister(Device prev, Device device, List<String> dns) {
        HL7DeviceExtension prevHL7Ext = prev.getDeviceExtension(HL7DeviceExtension.class);
        if (prevHL7Ext == null)
            return;

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        for (String name : prevHL7Ext.getHL7ApplicationNames()) {
            if (!name.equals("*") && (hl7Ext == null || hl7Ext.getHL7Application(name) == null))
                dns.add(hl7appDN(name, appNamesRegistryDN));
        }
    }

    @Override
    protected void markForUnregister(String deviceDN, List<String> dns) throws NamingException, InternalException {
        if (!appNamesRegistryExists())
            return;

        NamingEnumeration<SearchResult> ne = config.search(deviceDN, "(objectclass=hl7Application)",
                Normal.EMPTY_STRING_ARRAY);
        try {
            while (ne.hasMore()) {
                String rdn = ne.next().getName();
                if (!rdn.equals("hl7ApplicationName=*"))
                    dns.add(rdn + ',' + appNamesRegistryDN);
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

}
