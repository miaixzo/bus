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
package org.miaixz.bus.image.metric.hl7.ldap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfiguration;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
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

    public void storeTo(ConfigurationChanges.ModifiedObject ldapObj, HL7Application hl7App, String deviceDN, Attributes attrs) {
    }

    public void storeChilds(ConfigurationChanges diffs, String appDN, HL7Application hl7App) throws NamingException {
    }

    public void loadFrom(HL7Application hl7App, Attributes attrs) throws NamingException {
    }

    public void loadChilds(HL7Application hl7App, String appDN) throws NamingException, InternalException {
    }

    public void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, HL7Application a, HL7Application b, List<ModificationItem> mods) {
    }

    public void mergeChilds(ConfigurationChanges diffs, HL7Application prev, HL7Application hl7App, String appDN)
            throws NamingException {
    }

}
