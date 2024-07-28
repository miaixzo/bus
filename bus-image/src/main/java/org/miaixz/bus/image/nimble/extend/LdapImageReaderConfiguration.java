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
package org.miaixz.bus.image.nimble.extend;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.builtin.ldap.LdapBuilder;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfigurationExtension;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.nimble.codec.ImageReaderFactory;
import org.miaixz.bus.image.nimble.codec.ImageReaderFactory.ImageReaderParam;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapImageReaderConfiguration extends LdapDicomConfigurationExtension {

    private static final String CN_IMAGE_READER_FACTORY = "cn=Image Reader Factory,";

    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext != null)
            store(diffs, deviceDN, ext.getImageReaderFactory());
    }

    private String dnOf(String tsuid, String imageReadersDN) {
        return LdapBuilder.dnOf("dicomTransferSyntax", tsuid, imageReadersDN);
    }

    private void store(ConfigurationChanges diffs, String deviceDN, ImageReaderFactory factory) throws NamingException {
        String imageReadersDN = CN_IMAGE_READER_FACTORY + deviceDN;
        config.createSubcontext(imageReadersDN,
                LdapBuilder.attrs("dcmImageReaderFactory", "cn", "Image Reader Factory"));
        for (Entry<String, ImageReaderParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            String dn = dnOf(tsuid, imageReadersDN);
            ConfigurationChanges.ModifiedObject ldapObj1 = ConfigurationChanges.addModifiedObjectIfVerbose(diffs, dn,
                    ConfigurationChanges.ChangeType.C);
            config.createSubcontext(dn, storeTo(ldapObj1, tsuid, entry.getValue(), new BasicAttributes(true)));
        }
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, String tsuid, ImageReaderParam param,
            Attributes attrs) {
        attrs.put("objectclass", "dcmImageReader");
        attrs.put("dicomTransferSyntax", tsuid);
        attrs.put("dcmIIOFormatName", param.formatName);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmJavaClassName", param.className, null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmPatchJPEGLS", param.patchJPEGLS, null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmImageReadParam", param.getImageReadParams());
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN) throws NamingException, InternalException {
        String imageReadersDN = CN_IMAGE_READER_FACTORY + deviceDN;
        try {
            config.getAttributes(imageReadersDN);
        } catch (NameNotFoundException e) {
            return;
        }

        ImageReaderFactory factory = new ImageReaderFactory();
        NamingEnumeration<SearchResult> ne = config.search(imageReadersDN, "(objectclass=dcmImageReader)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                factory.put(LdapBuilder.stringValue(attrs.get("dicomTransferSyntax"), null),
                        new ImageReaderParam(LdapBuilder.stringValue(attrs.get("dcmIIOFormatName"), null),
                                LdapBuilder.stringValue(attrs.get("dcmJavaClassName"), null),
                                LdapBuilder.stringValue(attrs.get("dcmPatchJPEGLS"), null),
                                LdapBuilder.stringArray(attrs.get("dcmImageReadParam"))));
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
        device.addDeviceExtension(new ImageReaderExtension(factory));
    }

    @Override
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException {
        ImageReaderExtension prevExt = prev.getDeviceExtension(ImageReaderExtension.class);
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext == null && prevExt == null)
            return;

        String dn = CN_IMAGE_READER_FACTORY + deviceDN;
        if (ext == null) {
            config.destroySubcontextWithChilds(dn);
            ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
        } else if (prevExt == null) {
            store(diffs, deviceDN, ext.getImageReaderFactory());
        } else {
            merge(diffs, prevExt.getImageReaderFactory(), ext.getImageReaderFactory(), dn);
        }
    }

    private void merge(ConfigurationChanges diffs, ImageReaderFactory prev, ImageReaderFactory factory,
            String imageReadersDN) throws NamingException {
        for (Entry<String, ImageReaderParam> entry : prev.getEntries()) {
            String tsuid = entry.getKey();
            if (factory.get(tsuid) == null) {
                String dn = dnOf(tsuid, imageReadersDN);
                config.destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Entry<String, ImageReaderParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            String dn = dnOf(tsuid, imageReadersDN);
            ImageReaderParam prevParam = prev.get(tsuid);
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

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, ImageReaderParam prevParam,
            ImageReaderParam param, List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmIIOFormatName", prevParam.formatName, param.formatName, null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmJavaClassName", prevParam.className, param.className, null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmPatchJPEGLS", prevParam.patchJPEGLS, param.patchJPEGLS, null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmImageReadParam", prevParam.getImageReadParams(),
                param.getImageReadParams());
        return mods;
    }

}
