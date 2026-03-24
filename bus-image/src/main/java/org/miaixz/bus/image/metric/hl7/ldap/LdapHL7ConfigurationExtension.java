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
package org.miaixz.bus.image.metric.hl7.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfiguration;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class LdapHL7ConfigurationExtension {

    protected LdapHL7Configuration config;

    public LdapHL7Configuration getHL7Configuration() {
        return config;
    }

    public void setHL7Configuration(LdapHL7Configuration config) {
        if (config != null && this.config != null)
            throw new IllegalStateException("already owned by other HL7 Configuration");
        this.config = config;
    }

    public LdapDicomConfiguration getDicomConfiguration() {
        return config != null ? config.getDicomConfiguration() : null;
    }

    public void storeTo(
            ConfigurationChanges.ModifiedObject ldapObj,
            HL7Application hl7App,
            String deviceDN,
            Attributes attrs) {
    }

    public void storeChilds(ConfigurationChanges diffs, String appDN, HL7Application hl7App) throws NamingException {
    }

    public void loadFrom(HL7Application hl7App, Attributes attrs) throws NamingException {
    }

    public void loadChilds(HL7Application hl7App, String appDN) throws NamingException, InternalException {
    }

    public void storeDiffs(
            ConfigurationChanges.ModifiedObject ldapObj,
            HL7Application a,
            HL7Application b,
            List<ModificationItem> mods) {
    }

    public void mergeChilds(ConfigurationChanges diffs, HL7Application prev, HL7Application hl7App, String appDN)
            throws NamingException {
    }

}
