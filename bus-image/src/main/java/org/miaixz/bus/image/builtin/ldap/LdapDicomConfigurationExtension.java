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
package org.miaixz.bus.image.builtin.ldap;

import java.security.cert.CertificateException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapDicomConfigurationExtension {

    protected LdapDicomConfiguration config;

    public LdapDicomConfiguration getDicomConfiguration() {
        return config;
    }

    public void setDicomConfiguration(LdapDicomConfiguration config) {
        if (config != null && this.config != null)
            throw new IllegalStateException("already owned by other Dicom Configuration");
        this.config = config;
    }

    protected void storeTo(ConfigurationChanges.ModifiedObject ldapObj, Device device, Attributes attrs) {
    }

    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device)
            throws NamingException, InternalException {
    }

    protected void loadFrom(Device device, Attributes attrs) throws NamingException, CertificateException {
    }

    protected void loadChilds(Device device, String deviceDN) throws NamingException, InternalException {
    }

    protected void storeDiffs(
            ConfigurationChanges.ModifiedObject ldapObj,
            Device prev,
            Device device,
            List<ModificationItem> mods) {
    }

    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException, InternalException {
    }

    protected void storeTo(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity ae, Attributes attrs) {
    }

    protected void storeChilds(ConfigurationChanges diffs, String aeDN, ApplicationEntity ae) {
    }

    protected void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
    }

    protected void loadChilds(ApplicationEntity ae, String aeDN) throws NamingException, InternalException {
    }

    protected void storeDiffs(
            ConfigurationChanges.ModifiedObject ldapObj,
            ApplicationEntity a,
            ApplicationEntity b,
            List<ModificationItem> mods) {
    }

    protected void mergeChilds(ConfigurationChanges diffs, ApplicationEntity prev, ApplicationEntity ae, String aeDN) {
    }

    protected void register(Device device, List<String> dns) throws InternalException {
    }

    protected void registerDiff(Device prev, Device device, List<String> dns) throws InternalException {
    }

    protected void markForUnregister(Device prev, Device device, List<String> dns) {
    }

    protected void markForUnregister(String deviceDN, List<String> dns) throws NamingException, InternalException {
    }

}
