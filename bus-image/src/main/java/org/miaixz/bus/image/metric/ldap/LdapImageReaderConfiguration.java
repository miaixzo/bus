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
import org.miaixz.bus.image.nimble.codec.ImageReaderFactory;
import org.miaixz.bus.image.nimble.codec.ImageReaderFactory.ImageReaderParam;
import org.miaixz.bus.image.nimble.extend.ImageReaderExtension;

/**
 * Represents the LdapImageReaderConfiguration type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LdapImageReaderConfiguration extends LdapDicomConfigurationExtension {

    /**
     * The cn image reader factory value.
     */
    private static final String CN_IMAGE_READER_FACTORY = "cn=Image Reader Factory,";

    /**
     * Stores the childs.
     *
     * @param diffs    the diffs.
     * @param deviceDN the device dn.
     * @param device   the device.
     * @throws NamingException if the operation cannot be completed.
     */
    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext != null)
            store(diffs, deviceDN, ext.getImageReaderFactory());
    }

    /**
     * Executes the dn of operation.
     *
     * @param tsuid          the tsuid.
     * @param imageReadersDN the image readers dn.
     * @return the operation result.
     */
    private String dnOf(String tsuid, String imageReadersDN) {
        return LdapBuilder.dnOf("dicomTransferSyntax", tsuid, imageReadersDN);
    }

    /**
     * Executes the store operation.
     *
     * @param diffs    the diffs.
     * @param deviceDN the device dn.
     * @param factory  the factory.
     * @throws NamingException if the operation cannot be completed.
     */
    private void store(ConfigurationChanges diffs, String deviceDN, ImageReaderFactory factory) throws NamingException {
        String imageReadersDN = CN_IMAGE_READER_FACTORY + deviceDN;
        config.createSubcontext(
                imageReadersDN,
                LdapBuilder.attrs("dcmImageReaderFactory", "cn", "Image Reader Factory"));
        for (Entry<String, ImageReaderParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            String dn = dnOf(tsuid, imageReadersDN);
            ConfigurationChanges.ModifiedObject ldapObj1 = ConfigurationChanges
                    .addModifiedObjectIfVerbose(diffs, dn, ConfigurationChanges.ChangeType.C);
            config.createSubcontext(dn, storeTo(ldapObj1, tsuid, entry.getValue(), new BasicAttributes(true)));
        }
    }

    /**
     * Stores the to.
     *
     * @param ldapObj the ldap obj.
     * @param tsuid   the tsuid.
     * @param param   the param.
     * @param attrs   the attrs.
     * @return the operation result.
     */
    private Attributes storeTo(
            ConfigurationChanges.ModifiedObject ldapObj,
            String tsuid,
            ImageReaderParam param,
            Attributes attrs) {
        attrs.put("objectclass", "dcmImageReader");
        attrs.put("dicomTransferSyntax", tsuid);
        attrs.put("dcmIIOFormatName", param.formatName);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmJavaClassName", param.className, null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmPatchJPEGLS", param.patchJPEGLS, null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmImageReadParam", param.getImageReadParams());
        return attrs;
    }

    /**
     * Loads the childs.
     *
     * @param device   the device.
     * @param deviceDN the device dn.
     * @throws NamingException   if the operation cannot be completed.
     * @throws InternalException if the operation cannot be completed.
     */
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
                factory.put(
                        LdapBuilder.stringValue(attrs.get("dicomTransferSyntax"), null),
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

    /**
     * Executes the merge childs operation.
     *
     * @param diffs    the diffs.
     * @param prev     the prev.
     * @param device   the device.
     * @param deviceDN the device dn.
     * @throws NamingException if the operation cannot be completed.
     */
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

    /**
     * Executes the merge operation.
     *
     * @param diffs          the diffs.
     * @param prev           the prev.
     * @param factory        the factory.
     * @param imageReadersDN the image readers dn.
     * @throws NamingException if the operation cannot be completed.
     */
    private void merge(
            ConfigurationChanges diffs,
            ImageReaderFactory prev,
            ImageReaderFactory factory,
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
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges
                        .addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                config.createSubcontext(
                        dn,
                        storeTo(
                                ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                tsuid,
                                entry.getValue(),
                                new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges
                        .addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                config.modifyAttributes(dn, storeDiffs(ldapObj, prevParam, entry.getValue(), new ArrayList<>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    /**
     * Stores the diffs.
     *
     * @param ldapObj   the ldap obj.
     * @param prevParam the prev param.
     * @param param     the param.
     * @param mods      the mods.
     * @return the operation result.
     */
    private List<ModificationItem> storeDiffs(
            ConfigurationChanges.ModifiedObject ldapObj,
            ImageReaderParam prevParam,
            ImageReaderParam param,
            List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmIIOFormatName", prevParam.formatName, param.formatName, null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmJavaClassName", prevParam.className, param.className, null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmPatchJPEGLS", prevParam.patchJPEGLS, param.patchJPEGLS, null);
        LdapBuilder.storeDiff(
                ldapObj,
                mods,
                "dcmImageReadParam",
                prevParam.getImageReadParams(),
                param.getImageReadParams());
        return mods;
    }

}
