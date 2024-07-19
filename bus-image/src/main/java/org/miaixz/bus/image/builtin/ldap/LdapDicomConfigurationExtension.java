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
package org.miaixz.bus.image.builtin.ldap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import java.security.cert.CertificateException;
import java.util.List;

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

    protected void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException {
    }

    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, InternalException {
    }

    protected void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, Device prev, Device device, List<ModificationItem> mods) {
    }

    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException, InternalException {
    }

    protected void storeTo(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity ae, Attributes attrs) {
    }

    protected void storeChilds(ConfigurationChanges diffs, String aeDN, ApplicationEntity ae) {
    }

    protected void loadFrom(ApplicationEntity ae, Attributes attrs)
            throws NamingException {
    }

    protected void loadChilds(ApplicationEntity ae, String aeDN) throws NamingException, InternalException {
    }

    protected void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity a, ApplicationEntity b,
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
