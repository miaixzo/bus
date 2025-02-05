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
package org.miaixz.bus.image.metric.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.builtin.ldap.LdapBuilder;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfigurationExtension;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.nimble.codec.ImageWriterFactory;
import org.miaixz.bus.image.nimble.codec.ImageWriterFactory.ImageWriterParam;
import org.miaixz.bus.image.nimble.extend.ImageWriterExtension;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapImageWriterConfiguration extends LdapDicomConfigurationExtension {

    private static final String CN_IMAGE_WRITER_FACTORY = "cn=Image Writer Factory,";

    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        ImageWriterExtension ext = device.getDeviceExtension(ImageWriterExtension.class);
        if (ext != null)
            store(diffs, deviceDN, ext.getImageWriterFactory());
    }

    private String dnOf(String tsuid, String imageWritersDN) {
        return LdapBuilder.dnOf("dicomTransferSyntax", tsuid, imageWritersDN);
    }

    private void store(ConfigurationChanges diffs, String deviceDN, ImageWriterFactory factory) throws NamingException {
        String imageWritersDN = CN_IMAGE_WRITER_FACTORY + deviceDN;
        config.createSubcontext(imageWritersDN,
                LdapBuilder.attrs("dcmImageWriterFactory", "cn", "Image Writer Factory"));
        for (Entry<String, ImageWriterParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            ConfigurationChanges.ModifiedObject ldapObj1 = ConfigurationChanges.addModifiedObjectIfVerbose(diffs,
                    imageWritersDN, ConfigurationChanges.ChangeType.C);
            config.createSubcontext(dnOf(tsuid, imageWritersDN),
                    storeTo(ldapObj1, tsuid, entry.getValue(), new BasicAttributes(true)));
        }
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, String tsuid, ImageWriterParam param,
            Attributes attrs) {
        attrs.put("objectclass", "dcmImageWriter");
        attrs.put("dicomTransferSyntax", tsuid);
        attrs.put("dcmIIOFormatName", param.formatName);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmJavaClassName", param.className, null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmPatchJPEGLS", param.patchJPEGLS, null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmImageWriteParam", param.getImageWriteParams());
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN) throws NamingException, InternalException {
        String imageWritersDN = CN_IMAGE_WRITER_FACTORY + deviceDN;
        try {
            config.getAttributes(imageWritersDN);
        } catch (NameNotFoundException e) {
            return;
        }

        ImageWriterFactory factory = new ImageWriterFactory();
        NamingEnumeration<SearchResult> ne = config.search(imageWritersDN, "(objectclass=dcmImageWriter)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                factory.put(LdapBuilder.stringValue(attrs.get("dicomTransferSyntax"), null),
                        new ImageWriterParam(LdapBuilder.stringValue(attrs.get("dcmIIOFormatName"), null),
                                LdapBuilder.stringValue(attrs.get("dcmJavaClassName"), null),
                                LdapBuilder.stringValue(attrs.get("dcmPatchJPEGLS"), null),
                                LdapBuilder.stringArray(attrs.get("dcmImageWriteParam"))));
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
        device.addDeviceExtension(new ImageWriterExtension(factory));
    }

    @Override
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException {
        ImageWriterExtension prevExt = prev.getDeviceExtension(ImageWriterExtension.class);
        ImageWriterExtension ext = device.getDeviceExtension(ImageWriterExtension.class);
        if (ext == null && prevExt == null)
            return;

        String dn = CN_IMAGE_WRITER_FACTORY + deviceDN;
        if (ext == null) {
            config.destroySubcontextWithChilds(dn);
            ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
        } else if (prevExt == null) {
            store(diffs, deviceDN, ext.getImageWriterFactory());
        } else {
            merge(diffs, prevExt.getImageWriterFactory(), ext.getImageWriterFactory(), dn);
        }
    }

    private void merge(ConfigurationChanges diffs, ImageWriterFactory prev, ImageWriterFactory factory,
            String imageWritersDN) throws NamingException {
        for (Entry<String, ImageWriterParam> entry : prev.getEntries()) {
            String tsuid = entry.getKey();
            if (factory.get(tsuid) == null) {
                String dn = dnOf(tsuid, imageWritersDN);
                config.destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Entry<String, ImageWriterParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            String dn = dnOf(tsuid, imageWritersDN);
            ImageWriterParam prevParam = prev.get(tsuid);
            if (prevParam == null) {
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges.addModifiedObject(diffs, dn,
                        ConfigurationChanges.ChangeType.C);
                config.createSubcontext(dn, storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj), tsuid,
                        entry.getValue(), new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges.addModifiedObject(diffs, dn,
                        ConfigurationChanges.ChangeType.U);
                config.modifyAttributes(dn, storeDiffs(ldapObj, prevParam, entry.getValue(), new ArrayList<>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, ImageWriterParam prevParam,
            ImageWriterParam param, List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmIIOFormatName", prevParam.formatName, param.formatName, null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmJavaClassName", prevParam.className, param.className, null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmPatchJPEGLS", prevParam.patchJPEGLS, param.patchJPEGLS, null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmImageWriteParam", prevParam.getImageWriteParams(),
                param.getImageWriteParams());
        return mods;
    }

}
